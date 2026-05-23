# Agent 平台架构对比与优化分析

> 基于 OpenCode、Codex CLI、Claude Code、OpenClaw、HermesAgent、QwenCode 六大开源实现，对本项目 agent-platform 的 Skills 调用、会话记录、记忆处理等核心模块进行对比分析，提出优化建议。

---

## 一、本项目现状速览

| 模块 | 现有实现 | 关键文件 |
|------|---------|---------|
| 对话管理 | 树形消息（物化路径）+ 游标定位 | `ChatSession`/`ChatMessage` |
| 工具系统 | BUILTIN（Spring Bean 自注册）+ CUSTOM（Groovy 动态加载）+ MCP（懒加载） | `ToolkitFactory`, `ToolsRegister`, `DynamicAgentTool` |
| 技能系统 | SkillBox + SkillHook，两阶段（目录注入 → 按需加载工具组） | `SkillBoxFactory`, `SkillHook` |
| MCP 集成 | 懒连接 + 共享上下文 + 运行时降级 | `McpClientFactory`, `LazyMcpAgentTool` |
| 记忆/压缩 | AutoContextMemory（6 级渐进压缩）+ PostgresSession 持久化 | `AutoContextMemory`, `AutoContextHook`, `PostgresSession` |
| Hook 机制 | 事件驱动 + 优先级排序 + 响应式链 | `HooksFactory`, `IAgentHook`, `Hook.java` |
| 会话持久化 | PostgreSQL（state_key/item_index 结构） | `AgentScopeSession`, `PostgresSession` |
| 跨会话记忆 | **未实现**（SDK 有 LongTermMemory 接口但未接入） | — |
| 工具确认 | IConfirmationHook（静态列表，需运行时注册） | `IConfirmationHook` |

---

## 二、六大开源实现核心架构特征

### 2.1 OpenCode（sst/opencode）

| 维度 | 特征 |
|------|------|
| 语言 | TypeScript + Effect-TS |
| Agent 循环 | Effect-TS 管道，类型化错误+DI+取消 |
| 对话 | 树形分支（类似本项目） |
| 工具 | 统一注册表 + ToolRouter 分发 |
| Skills | **懒加载**：仅 name+description 在系统提示中，通过 `skill` 工具按需加载完整内容 |
| MCP | 一等公民，统一权限管线，`servername_toolname` 命名空间 |
| 压缩 | 两阶段：先裁剪工具输出，再 LLM 摘要 |
| 子Agent | 独立 session，仅摘要结果回流（防上下文污染） |
| 记忆 | 子Agent 隔离 session + 摘要回流 |

**亮点**：Effect-TS 全链路可观测管道；懒加载 skill 避免系统提示膨胀；子Agent 隔离 session。

### 2.2 OpenAI Codex CLI

| 维度 | 特征 |
|------|------|
| 语言 | Rust |
| Agent 循环 | 异步有界队列（提交 512 + 事件无界），生产者-消费者模式 |
| 对话 | ThreadManager → CodexThread → Session 三层 |
| 工具 | ToolSpec（模型可见）+ ToolRegistry（运行时处理），核心极简 |
| Skills | 目录扫描 SKILL.md，注入系统提示 |
| MCP | 启动快照（本地缓存工具定义先行）+ 命名空间 + 审批管线 |
| 压缩 | **双层压缩**：OpenAI 服务端加密 vs 客户端明文；压缩后 = 1 摘要 + ≤20K 近期消息 |
| 记忆 | **两阶段记忆**：轻量提取（gpt-mini）→ 强力合并（gpt-codex）；评分公式 `score = usage_count × 2.0 × exp(-age_days/30) × quality` |
| 会话记录 | **Event Sourcing**：JSONL 追加写入 + 后台异步写入器 + SQLite 查询层 |
| 会话恢复 | 从 rollout 文件 + SQLite 索引恢复；支持 Fork（FullHistory/LastNTurns/Compacted/MemoryOnly） |
| 子Agent | MCP Server 模式（Codex 自身可作为工具供其他 Agent 调用） |

**亮点**：有界提交队列背压控制；Event Sourcing 会话存储；两阶段记忆（提取+合并+评分衰减）；会话 Fork 策略；后台异步持久化不阻塞 Agent 循环。

### 2.3 Claude Code

| 维度 | 特征 |
|------|------|
| 语言 | TypeScript |
| Agent 循环 | 标准 ReAct + Fork 子Agent |
| 对话 | JSONL 追加写入 + 64K Lite Reader + Snip Healing 恢复 |
| 工具 | 类型化 9+ 内置 + 三级权限层级（Managed→User→Local） |
| Skills | **SKILL.md + YAML frontmatter**：`disable-model-invocation`/`user-invocable`/`allowed-tools`/`context:fork`；动态上下文注入 `` !`command` ``；文件变更热检测 |
| MCP | 7 种传输 + OAuth 状态机 + 信号升级关停（SIGINT→SIGTERM→SIGKILL）+ LRU 缓存 + 通道通知 |
| 压缩 | 有效窗口 167K（200K-20K摘要-13K缓冲）；**Fork 压缩**（复用 Prompt Cache）；3 次失败熔断 + PTL 降级 |
| 记忆 | **6 层记忆**：组织策略 → 项目记忆 → 项目规则（路径作用域）→ 用户记忆 → 本地项目记忆 → Auto Memory |
| Auto Memory 召回 | **侧查询轻量模型**选择相关文件（非暴力注入） |
| 子Agent | 字节级上下文克隆（Prompt Cache 命中）；4 内置类型 + 自定义 Agent（.claude/agents/） |
| 会话持久化 | 异步写队列 + 元数据追加到尾部 + 远程同步 |

**亮点**：6 层记忆分层 + 轻量模型侧查询召回；Fork 压缩复用缓存；SKILL.md 丰富的调用控制；字节级上下文克隆优化缓存。

### 2.4 OpenClaw

| 维度 | 特征 |
|------|------|
| 语言 | TypeScript |
| 架构 | Gateway 守护进程（长驻进程统一管理连接） |
| Agent 循环 | 序列化运行（per-session 队列防竞态）；Queue 模式：steer/followup/collect/interrupt |
| 工具 | 一等公民（browser, canvas, cron, bash, process, read, write, edit） |
| Skills | SKILL.md + ClawHub 市场 |
| MCP | 无显式 MCP 支持 |
| 压缩 | 自动 compaction + 重试机制 |
| 记忆 | 文件注入（AGENTS.md/SOUL.md/TOOLS.md）+ 会话历史工具 |
| 消息 | 23+ 平台适配 |

**亮点**：Gateway 长驻架构 + 序列化运行队列；多平台消息适配。

### 2.5 HermesAgent

| 维度 | 特征 |
|------|------|
| 语言 | Python |
| 架构 | 平台无关 AIAgent 类 + 4 入口（CLI/Gateway/ACP/Batch） |
| Agent 循环 | 迭代推理 + 可中断 API 调用；预算 90 轮/子Agent 50 轮 |
| 工具 | 47 工具 19 工具集，**自注册**（import 时 register）；11 种后端（Local/Docker/SSH/MCP 等） |
| Skills | **程序记忆**：Agent 自主创建 skill；Skills Hub（agentskills.io）|
| MCP | 松耦合 + registry + check_fn 门控 + 优雅降级 |
| 压缩 | 预飞检查（>50%）+ 网关检查（>85%）；**先刷记忆再压缩** |
| 记忆 | **闭环记忆**：MemoryManager + 可插拔 Provider + Honcho 方言式用户建模；定期 nudge 持久化 |
| 会话 | SQLite + FTS5 全文搜索 + 血缘追踪 + Profile 隔离 |
| 子Agent | delegate_task 工具 + 独立预算 |

**亮点**：自注册工具；自主创建 skill；闭环记忆 + Honcho 用户建模；先刷记忆再压缩（防丢失）；FTS5 跨会话搜索。

### 2.6 QwenCode

| 维度 | 特征 |
|------|------|
| 语言 | TypeScript |
| 架构 | CLI + Core 双包分离 + Daemon 模式（HTTP+SSE+ACP） |
| Agent 循环 | 标准 ReAct + 安全门控 |
| 工具 | 11+ 内置 + 可扩展 |
| Skills | SKILL.md + 运行时 URL 加载 |
| MCP | **深度集成**：发现层 + 执行层 + 3 传输 + OAuth + MCP Prompts 作为斜杠命令 |
| 压缩 | `/compress` + compact 模式 |
| 记忆 | **自动记忆 4 生命周期**：Extract（每次响应后）→ Dream（定期 24h/5会话 去重合并）→ Recall（启发式评分选择 ≤5 文档）→ Forget（手动删除） |
| 会话 | 文件存储 + 恢复 + 自动标题 + Recap + 回退 |
| 子Agent | **Fork 子Agent**：继承完整上下文 + CacheSafeParams 复用 API 缓存 + 即发即弃 + 递归防护 |

**亮点**：4 生命周期自动记忆（Extract/Dream/Recall/Forget）+ 启发式评分召回；Fork 子Agent 缓存优化；Daemon 模式多客户端共享会话；MCP Prompts 作为斜杠命令。

---

## 三、关键模块对比与优化建议

### 3.1 Skills 调用机制

#### 现状

本项目采用「目录注入 + 按需加载工具组」的两阶段模式：
1. SkillHook 在 PreCall 时注入技能目录（name/description/skill-id）
2. LLM 调用 `load_skill_through_path` 工具激活技能 → 解锁关联工具组

#### 对比

| 项目 | Skills 调用方式 | 本项目差距 |
|------|---------------|-----------|
| Claude Code | SKILL.md + YAML frontmatter（`disable-model-invocation`/`user-invocable`/`context:fork`）+ `` !`cmd` `` 动态上下文 + 文件变更热检测 | 缺少调用控制粒度、动态上下文、热检测 |
| OpenCode | 懒加载（仅 name+desc 在提示中，通过 `skill` 工具按需加载完整内容） | **已实现类似机制** |
| HermesAgent | Agent 自主创建 skill + Skills Hub 共享 | 无自主创建能力，无市场 |
| QwenCode | 运行时 URL 加载 + fetchable skills | 无远程加载能力 |

#### 优化建议

**S1：增加 Skills 调用控制粒度**（优先级：高）

参考 Claude Code 的 YAML frontmatter 设计，在 `SkillPackage` 中增加调用控制字段：

```
- userInvocable: boolean      — 用户是否可通过斜杠命令调用
- modelInvocable: boolean     — 模型是否可自主调用
- contextMode: ENUM(inline, fork, lazy)
  - inline: 内容直接注入系统提示（当前行为）
  - fork:  以子Agent 上下文运行（隔离+摘要回流）
  - lazy:  仅注入 name+desc，按需加载（当前 load_skill_through_path）
- allowedTools: List<String>  — 限制该 skill 可使用的工具子集
- dynamicContext: List<Cmd>   — 执行命令获取动态上下文（如 git branch、当前时间）
```

**S2：Skill 文件变更热检测**（优先级：中）

当前 skill 修改需重启 Agent 或重新创建 session。参考 Claude Code 文件监听机制，实现 SkillPackage 变更时自动刷新 SkillBox。

**S3：支持 Skill 远程加载/市场**（优先级：低，远期）

参考 QwenCode 的 fetchable skills + HermesAgent 的 Skills Hub，支持从 URL 或市场加载 SkillPackage。可在 `SkillPackage` 增加 `sourceType`（LOCAL/URL/MARKET）和 `sourceUrl`。

---

### 3.2 用户会话记录

#### 现状

- 会话存储：PostgreSQL，`AgentScopeSession` 表以 (session_id, state_key, item_index) 结构序列化
- 消息存储：`ChatMessage` 树形结构 + 物化路径
- 写入方式：同步写入，在 Agent 循环中直接调用 `saveTo()`
- 查询：全量加载 state_data 反序列化

#### 对比

| 项目 | 会话存储方式 | 本项目差距 |
|------|------------|-----------|
| Codex CLI | **Event Sourcing**：JSONL 追加写入 + 后台异步写入器 + SQLite 查询索引 | 同步写入阻塞 Agent 循环；无事件溯源 |
| Claude Code | JSONL 追加 + 异步写队列 + 64K Lite Reader（元数据追加到尾部）| 同上；无轻量元数据读取 |
| HermesAgent | SQLite + FTS5 全文搜索 + 血缘追踪 | 无跨会话搜索能力 |

#### 优化建议

**S4：会话持久化异步化**（优先级：高）

当前 `saveTo()` 在 Agent 循环内同步执行，I/O 延迟直接影响响应速度。参考 Codex/Claude Code 的异步写入模式：

```
方案：引入写入队列 + 后台持久化线程
- Agent 循环 → 提交 SaveCmd 到有界队列 → 立即返回
- 后台 SaveWorker 消费队列 → 批量写入 PostgreSQL
- 关键状态变更（如 memory save）可标记 Persist 强制刷盘
- 普通状态变更（如 toolkit_activeGroups）可延迟批量写入
```

**S5：会话 Event Sourcing**（优先级：中）

当前 `state_data` 存储的是全量快照，无法回溯历史状态。引入 Event Sourcing：

```
方案：
- 新增 agent_scope_events 表：(session_id, event_type, event_data, timestamp)
- 每次状态变更追加一条事件记录
- 快照 + 事件混合：定期生成快照（如每 50 轮），恢复时从最近快照 + 增量事件重建
- 好处：支持会话回溯/重放/审计
```

**S6：轻量会话元数据读取**（优先级：中）

参考 Claude Code 的 64K Lite Reader，当前获取会话列表需要全量加载。优化：

```
方案：
- 在 agent_scope_sessions 中增加 metadata_json 列（独立于 state_data）
- 会话创建/更新时同步写入元数据（sessionId, agentId, title, messageCount, lastMessageAt, tokenUsage）
- 列表查询只读 metadata_json，不加载 state_data
```

---

### 3.3 记忆处理

#### 现状

- 会话内记忆：AutoContextMemory，6 级渐进压缩（工具摘要 → 大消息卸载 → 历史轮次摘要 → 当前轮次大消息摘要 → 当前轮次压缩）
- 会话持久化：PostgresSession 存储 memory_messages
- **跨会话记忆：未实现**（SDK 有 LongTermMemory 接口但未接入）
- 压缩触发：msgThreshold(100) 或 tokenRatio(0.75)

#### 对比

| 项目 | 记忆架构 | 本项目差距 |
|------|---------|-----------|
| Codex CLI | 两阶段记忆：轻量提取 → 强力合并；评分衰减 `usage×exp(-age/30)×quality` | **无跨会话记忆**；无评分/衰减 |
| Claude Code | 6 层分层 + 侧查询轻量模型召回 + ≤5 文档 ×1200 字符 | 无分层、无智能召回 |
| HermesAgent | 闭环记忆 + 可插拔 Provider + Honcho 用户建模 + 先刷记忆再压缩 | 无闭环、无用户建模、压缩时不刷记忆 |
| QwenCode | 4 生命周期（Extract/Dream/Recall/Forget）+ 启发式评分 ≤5 文档 | 无自动提取/合并/遗忘 |

#### 优化建议

**S7：实现跨会话记忆系统**（优先级：高）

这是当前最大的架构缺口。推荐参考 QwenCode 的 4 生命周期模式 + Claude Code 的分层设计：

```
架构设计：

1. 记忆存储
   - 表: agent_memory (id, user_id, agent_id, type, name, description, content, tags, score, created_at, updated_at, accessed_at)
   - 文件: MEMORY.md 索引（人类可读）
   - type 枚举: USER(用户偏好), FEEDBACK(行为指导), PROJECT(项目上下文), REFERENCE(外部资源)

2. Extract（提取）
   - 触发: 每次对话结束（或每 N 轮）
   - 执行: 轻量模型从对话中提取知识片段
   - 去重: 与已有记忆对比，增量更新

3. Dream（合并）
   - 触发: 定期（每 24h 或每 5 次会话）
   - 执行: 强力模型合并去重、分类、压缩
   - 评分: score = usage_count × recency_weight × quality_multiplier
   - recency_weight = exp(-age_days / 30)

4. Recall（召回）
   - 触发: 每次新会话开始 / 每轮推理前
   - 执行: 启发式评分（关键词匹配 + type 权重 + 新鲜度）
   - 注入: Top-5 记忆片段，每段 ≤1200 字符，注入系统提示

5. Forget（遗忘）
   - 触发: 手动(/forget) + 自动（score 低于阈值 + 超过 TTL）
```

**S8：压缩安全 — 先刷记忆再压缩**（优先级：高）

参考 HermesAgent 的关键设计：压缩前先将未持久化的记忆刷盘，防止压缩导致信息丢失。

```
方案：
- 在 AutoContextHook.compressIfNeeded() 前增加 preFlush 逻辑
- 如果启用了 LongTermMemory，先将 workingMemory 中的关键信息提取并持久化
- 压缩后再将提取的记忆从 workingMemory 中标记为已归档
```

**S9：记忆召回 — 智能选择而非暴力注入**（优先级：中）

参考 Claude Code 的侧查询轻量模型召回和 QwenCode 的启发式评分：

```
方案 A（轻量）：启发式评分
- 每条记忆的 tags 与当前消息关键词匹配
- type 权重：FEEDBACK > USER > PROJECT > REFERENCE
- 新鲜度衰减：exp(-age_days/30)
- Top-K（K=5）注入

方案 B（高级）：轻量模型侧查询
- 用小型模型（如 qwen-mini）对记忆索引做相关性判断
- 仅将高度相关的记忆注入上下文
- 更精确但增加一次 API 调用延迟
```

**S10：压缩后上下文恢复**（优先级：中）

当前 AutoContextMemory 的 context_reload 工具可恢复卸载消息，但无系统级保障。参考 Claude Code 的 post-compaction 状态再注入：

```
方案：
- 压缩后自动再注入：文件附件列表、活跃 skill 描述、MCP 工具定义
- 防止压缩后 Agent 丢失对可用工具/资源的认知
```

---

### 3.4 其他优化点

#### 3.4.1 MCP 集成增强

**S11：MCP 工具定义缓存快照**（优先级：中）

参考 Codex CLI 的启动快照模式：

```
现状：LazyMcpAgentTool 从 DB 缓存的 schema 创建，但首次调用仍需建立连接
优化：
- 应用启动时异步预热 MCP 连接（而非懒加载）
- 工具定义从本地缓存先加载（Agent 可立即规划）
- 后台建立实际连接，连接就绪后替换懒加载代理
```

**S12：MCP 服务端模式**（优先级：低）

参考 Codex CLI 的 MCP Server 模式和 QwenCode 的 Daemon 模式：

```
现状：本项目仅作为 MCP Client 消费外部工具
优化：支持 Agent-as-MCP-Server，让其他系统可通过 MCP 协议调用本平台 Agent
- 适配器：将 AG-UI 协议转为 MCP 工具调用
- 用途：多 Agent 编排、IDE 集成、CI/CD 自动化
```

**S13：MCP Prompts 作为斜杠命令**（优先级：低）

参考 QwenCode 的 MCP Prompts 映射为斜杠命令：

```
方案：
- 发现 MCP Server 的 prompts 能力时，自动注册为 /mcp-{server}-{prompt} 命令
- 用户输入 / 前缀时可搜索 MCP 提供的命令
```

#### 3.4.2 子Agent/Fork 机制

**S14：Fork 子Agent 上下文隔离与缓存优化**（优先级：中）

参考 Claude Code 和 QwenCode 的 Fork 子Agent 设计：

```
现状：本项目有 Agent-as-Tool（子Agent 通过 Toolkit 注册），但：
- 子Agent 共享父 Agent 上下文，无隔离
- 无缓存优化意识

优化：
- Fork 模式：子Agent 继承完整系统提示 + 工具集 + 模型配置
- 上下文隔离：子Agent 独立 session，仅摘要结果回流
- 缓存优化：确保父子的 API 请求前缀相同，命中 Prompt Cache
- 递归防护：深度追踪（最大 3-5 层）+ fork-boilerplate 标记检测
```

#### 3.4.3 工具确认/权限

**S15：分级权限体系**（优先级：中）

参考 Claude Code 的三级权限层级：

```
现状：IConfirmationHook 的 NEED_CONFIRM_TOOLS 列表默认为空，需运行时手动注册

优化：
- 权限层级：Managed（管理员策略）→ Agent（Agent 定义级）→ Session（会话级）
- 工具分类：SAFE（自动通过）/ SENSITIVE（需确认）/ DANGEROUS（需管理员授权）
- 配置化：在 AgentDefinition 中配置工具权限策略
- 持久化审批：用户一次审批后，同类操作可自动通过（session 范围内）
```

#### 3.4.4 会话搜索与回溯

**S16：跨会话搜索**（优先级：中）

参考 HermesAgent 的 SQLite + FTS5：

```
现状：会话列表仅支持按 userId/agentId/isPinned 过滤

优化：
- 对 ChatMessage.content 建立全文索引
- 支持关键词搜索历史会话
- 支持 LLM 对搜索结果做摘要（跨会话回忆）
```

**S17：会话分支 Fork**（优先级：低）

参考 Codex CLI 的会话 Fork 策略：

```
现状：ChatMessage 的树形结构支持分支（regenerate），但无法从已有会话 fork 新会话

优化：
- 支持 Fork 策略：FullHistory / LastNTurns(n) / CompactedHistory / MemoryOnly
- 新会话继承原会话的分支点内容
```

#### 3.4.5 Agent 循环健壮性

**S18：背压控制与队列化**（优先级：中）

参考 Codex CLI 的有界提交队列：

```
现状：AguiRequestProcessor 直接处理请求，无队列化

优化：
- 引入有界提交队列（per-session + global）
- 防止单用户过多并发请求导致资源耗尽
- 新请求排队策略：steer（打断）/ followup（排队）/ collect（收集）
```

**S19：压缩熔断与降级**（优先级：中）

参考 Claude Code 的 3 次失败熔断 + PTL 降级：

```
现状：AutoContextMemory 压缩失败后无明确降级策略

优化：
- 压缩连续失败 3 次 → 熔断，切换到简单截断模式
- PTL（Percentage Truncation Least）降级：移除最旧 20% 消息后重试
- 记录压缩失败事件，便于诊断
```

#### 3.4.6 可观测性

**S20：Agent 执行链路追踪**（优先级：低）

```
现状：StudioService 提供调试功能，但无结构化链路追踪

优化：
- 为每次 Agent 运行生成 traceId
- 关键节点记录 span：prompt构建、模型调用、工具执行、压缩、记忆召回
- 对接 OpenTelemetry 或自定义追踪系统
```

---

## 四、优化路线图建议

### 第一阶段：补齐核心能力（高优先级）

| 编号 | 优化项 | 预期收益 |
|------|-------|---------|
| S7 | 实现跨会话记忆系统 | 最大架构缺口，Agent 可跨会话积累知识 |
| S4 | 会话持久化异步化 | 消除 I/O 阻塞，提升响应速度 |
| S8 | 压缩安全（先刷记忆再压缩） | 防止压缩导致信息丢失 |
| S1 | Skills 调用控制粒度 | 提升技能调用的灵活性和安全性 |
| S15 | 分级权限体系 | 工具审批从手动注册变为配置化 |

### 第二阶段：增强体验与效率（中优先级）

| 编号 | 优化项 | 预期收益 |
|------|-------|---------|
| S9 | 记忆智能召回 | 减少无关记忆注入，提升上下文利用率 |
| S10 | 压缩后上下文恢复 | 压缩后 Agent 不丢失工具/资源认知 |
| S5 | 会话 Event Sourcing | 支持会话回溯/重放/审计 |
| S6 | 轻量会话元数据读取 | 提升会话列表查询性能 |
| S11 | MCP 工具定义缓存快照 | Agent 启动即可规划，无需等待 MCP 连接 |
| S14 | Fork 子Agent 上下文隔离 | 防止子Agent 污染父上下文 |
| S16 | 跨会话搜索 | 用户可检索历史对话 |
| S18 | 背压控制与队列化 | 防止资源耗尽 |
| S19 | 压缩熔断与降级 | 提升系统健壮性 |

### 第三阶段：生态与扩展（低优先级，远期）

| 编号 | 优化项 | 预期收益 |
|------|-------|---------|
| S2 | Skill 文件变更热检测 | 修改 Skill 无需重启 |
| S3 | Skill 远程加载/市场 | 构建 Skill 生态 |
| S12 | MCP 服务端模式 | Agent 可被其他系统调用 |
| S13 | MCP Prompts 作为斜杠命令 | 丰富用户交互方式 |
| S17 | 会话分支 Fork | 从已有会话派生新会话 |
| S20 | Agent 执行链路追踪 | 可观测性与诊断 |

---

## 五、六大实现架构速查表

| 特性 | OpenCode | Codex CLI | Claude Code | OpenClaw | HermesAgent | QwenCode | **本项目** |
|------|----------|-----------|-------------|----------|-------------|----------|-----------|
| 语言 | TS+Effect | Rust | TS | TS | Python | TS | **Java** |
| Agent 循环 | Effect 管道 | 有界队列+Event Sourcing | ReAct+Fork | 序列化队列 | 迭代推理+可中断 | ReAct+安全门控 | **ReAct+Hook** |
| 会话存储 | DB | JSONL+SQLite | JSONL+异步队列 | 文件锁 | SQLite+FTS5 | 文件 | **PostgreSQL 快照** |
| 会话恢复 | ✅ | ✅(Fork策略) | ✅(Snip Healing) | ✅ | ✅(血缘追踪) | ✅ | **✅(快照加载)** |
| 跨会话记忆 | 子Agent隔离摘要 | 两阶段+评分衰减 | 6层+侧查询召回 | 文件注入 | 闭环+Honcho | 4生命周期+启发式 | **❌ 未实现** |
| 压缩策略 | 裁剪+摘要 | 双层压缩 | Fork压缩+熔断 | 自动compaction | 预飞+网关+先刷记忆 | /compress | **6级渐进** |
| Skills | 懒加载 | SKILL.md注入 | YAML控制+动态上下文 | SKILL.md+市场 | 自主创建+Hub | URL加载 | **两阶段(目录+按需)** |
| MCP | 一等公民 | 启动快照+审批 | 7传输+OAuth+LRU | 无 | 松耦合+降级 | 深度+OAuth+Prompts | **懒加载+降级** |
| 工具权限 | 统一管线 | 审批管线 | 三级层级 | 沙箱白名单 | 检测+确认 | 安全门控 | **静态确认列表** |
| 子Agent | 隔离session | MCP Server模式 | Fork+缓存克隆 | sessions_spawn | delegate+独立预算 | Fork+CacheSafe | **Agent-as-Tool** |
| 异步持久化 | Effect调度 | 后台写入器 | 异步写队列 | Gateway管理 | — | — | **同步写入** |

---

## 六、总结

本项目在对话树形结构、6 级渐进压缩、MCP 懒加载降级、Hook 事件驱动等方面已有扎实基础。主要架构缺口和优化方向：

1. **最大缺口 — 跨会话记忆**：所有对比项目都实现了某种形式的跨会话记忆，本项目 SDK 已有 LongTermMemory 接口但未接入，应优先实现。
2. **性能瓶颈 — 同步持久化**：所有先进实现都采用异步写入，本项目同步写入阻塞 Agent 循环，应改为队列化异步。
3. **安全问题 — 压缩不刷记忆**：HermesAgent 的「先刷记忆再压缩」是防止信息丢失的关键实践，本项目缺失。
4. **灵活性不足 — Skills 调用控制**：Claude Code 的 YAML frontmatter 设计提供了精细的调用控制，本项目缺少 user/model 调用区分和动态上下文。
5. **健壮性不足 — 压缩无降级**：Claude Code 的熔断+PTL 降级策略值得借鉴。
6. **生态能力 — Skill 市场/MCP Server**：远期方向，但应有架构预留。
