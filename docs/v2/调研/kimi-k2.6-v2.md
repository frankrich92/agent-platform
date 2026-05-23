# 开源 Agent 实现对比与 AgentScope 优化分析

## 1. 调研范围与目标

### 1.1 对比对象

| 框架 | 类型 | 核心语言 | 特点 |
|------|------|----------|------|
| **OpenCode** | 开源 Agent 框架 | Python/TypeScript | Skills + Tools + MCP + 记忆系统 |
| **Codex CLI** | OpenAI 编码 Agent | Rust (96%) | 三层审批模型 + Native Memories + MCP |
| **Claude Code** | Claude CLI 工具 | TypeScript | 三层记忆系统 (CLAUDE.md/Auto Memory/Session Memory) |
| **OpenClaw** | 开源 Agent 网关 | Python | 聊天应用网关 + Pi Agent 包装器 |
| **HermesAgent** | 开源 Agent 框架 | Python | 47 工具 + 自改进 Skill 循环 + 三层 Prompt |
| **Qwen Code** | 阿里编码 Agent | TypeScript | 分层客户端-服务端 + Plan Mode + 六级配置 |

### 1.2 分析维度

- Skills 调用机制
- 用户会话记录管理
- 记忆处理架构
- Tools/MCP 加载与治理
- 对话系统与上下文管理
- 其他架构层面的优化点

---

## 2. 各开源框架架构要点

### 2.1 OpenCode

**Skills 系统：**
- 支持本地/压缩包/Git 三种导入方式
- Skill 包含描述、内容、资源引用、示例、脚本
- 运行时通过 SkillBox 注册到 Agent

**记忆系统：**
- 短期记忆：InMemoryMemory（基于列表的对话历史）
- 长期记忆：AutoContextMemory（自动压缩，基于 Token 阈值触发）
- 树形结构管理：支持父子节点关系，便于分支对话

**Tools/MCP：**
- 内置工具 + 动态工具 + MCP 工具三层架构
- MCP 支持 stdio/sse/http 三种协议
- 懒加载机制：Agent 创建时不连接 MCP，调用时才初始化

### 2.2 Codex CLI

**Skills 系统：**
- `~/.codex/memories/skills/` 存储可复用流程
- 背景提取管道：6+ 小时空闲后自动提取会话洞察

**记忆系统（三层）：**
1. **静态规则**：`~/.codex/codex.md`（全局）、`./codex.md`（项目级）、`./AGENTS.md`（目录级）
2. **Native Memories**：后台两阶段管道提取洞察，存入 `memory_summary.md`（~5K token 预算）
3. **Skills**：可复用程序存储

**会话管理：**
- 交互式终端绑定，单次进程内状态
- `/compact` 命令压缩对话历史释放 token
- 支持 `Ctrl+C` 中断、`Ctrl+D` 终止

**安全模型：**
- 三层审批：`suggest`（只读）→ `auto-edit`（自动文件变更）→ `full-auto`（无人值守）

### 2.3 Claude Code

**记忆系统（三层）：**
1. **CLAUDE.md**：项目级/全局级静态规则文件
2. **Auto Memory**：自动提取跨会话记忆
3. **Session Memory**：当前会话上下文

**架构特点：**
- 文件驱动的记忆层次结构
- 自动维护记忆文件（提取→整合→修剪 30 天未使用）

### 2.4 Qwen Code

**Skills 系统：**
- 目录化模块，含 `SKILL.md`（YAML frontmatter：name/description/allowedTools/paths）
- 三级发现：项目 `.qwen/skills/` > 用户 `~/.qwen/skills/` > 内置
- 动态暴露为 `SkillTool`，支持模板替换（如 `{{model}}`）
- 文件监听热重载

**记忆系统：**
- 分层 markdown 文件：`QWEN.md`、`GEMINI.md`
- 作用域：全局 (`~/.qwen/`)、项目 (`.qwen/`)、嵌套工作空间
- 运行时工具：`save_memory`（追加事实）、`read_memory`（检索条目）
- 自动维护：Extract（从对话提取事实）+ Dream（重新摘要防止膨胀）

**会话管理：**
- Token 缓存 + 历史压缩 (`/compress`)
- 会话回顾与自动标题生成
- 支持恢复会话
- **Plan Mode**：显式状态机分离规划与执行

**配置体系：**
- 六级优先级：CLI 参数 > 环境变量 > 项目级 > 用户级 > 系统级 > 默认值

### 2.5 HermesAgent

**Skills 系统：**
- 中央工具注册表 (`tools/registry.py`)，导入时注册
- 47 个工具跨 20 个工具集
- Skill 自动从经验创建，存储于 `~/.hermes/skills/`，使用时自动改进

**记忆系统（三层 Prompt）：**
1. `prompt_builder.py`：从 SOUL.md、MEMORY.md、USER.md、skills、context 文件组装
2. `prompt_caching.py`：Anthropic 缓存断点
3. `context_compressor.py`：上下文超限时的有损摘要

**会话管理：**
- 双存储：`hermes_state.py`（SQLite + FTS5）+ `gateway/session.py`（消息会话）
- 谱系追踪、原子写入、跨平台隔离、全文搜索

**工具架构：**
- 终端 6 后端：local、Docker、SSH、Modal、Daytona、Singularity
- 浏览器 11 个自动化工具
- MCP 客户端 (~2,200 行)
- 子 Agent 委托

### 2.6 OpenClaw

**架构特点：**
- 自托管网关，作为聊天应用（Discord/Telegram/WhatsApp）与 Pi Agent 之间的透传
- 单进程单体架构
- 工具/记忆主要委托给底层 Pi 模型

---

## 3. AgentScope 现状分析

### 3.1 当前架构概览

```
AgentScope (Apboa)
├── Agent 类型：ReAct / A2A
├── Skills：SkillBoxFactory → AgentSkill + Toolkit
├── Tools：ToolkitFactory → 内置/动态/MCP/子Agent
├── Memory：IMemoryFactory → InMemoryMemory / AutoContextMemory（树形）
├── Session：ChatSessionServiceImpl → MySQL 持久化（path/depth 分支模型）
├── Context：AgentContext → InheritableThreadLocal
├── Hooks：HooksFactory → 内置 + 动态加载
└── MCP：McpClientFactory → stdio/sse/http + 降级机制
```

### 3.2 核心代码落点

| 模块 | 文件 | 职责 |
|------|------|------|
| Skills | `SkillBoxFactory.java` | SkillBox 创建、技能包注册、代码执行配置 |
| Tools | `ToolkitFactory.java` | 工具工厂、MCP 懒加载、子 Agent 注册 |
| Memory | `IMemoryFactory.java` | 树形记忆节点管理（ConcurrentHashMap） |
| Session | `ChatSessionServiceImpl.java` | 会话 CRUD、消息追加、分支切换、分页加载 |
| Context | `AgentContext.java` | ThreadLocal 上下文（threadId/runId/memoryActive/planActive） |
| Agent | `ReActAgentHelper.java` | Agent 装配：model + skill + tool + knowledge + memory + hook |
| MCP | `McpClientFactory.java` | MCP 客户端管理、懒加载、共享上下文 |
| MCP 降级 | `McpRuntimeDegradeServiceImpl.java` | Redis 计数降级、自动故障转移 |

---

## 4. 对比分析与优化建议

### 4.1 Skills 调用机制

#### 4.1.1 现状问题

1. **无自动发现机制**：Skill 必须手动绑定到 Agent，无法根据对话上下文自动激活
2. **无 Skill 元数据标准**：Skill 缺少 `allowedTools`、`autoActivationPaths` 等声明式配置
3. **无 Skill 版本管理**：导入后无版本追踪，更新依赖全量覆盖
4. **Skill 间无依赖声明**：无法表达 Skill A 依赖 Skill B 的关系
5. **无运行时 Skill 动态加载**：会话中无法动态增删 Skill

#### 4.1.2 开源实践对比

| 特性 | AgentScope | Qwen Code | HermesAgent | Codex CLI |
|------|-----------|-----------|-------------|-----------|
| 自动发现 | ❌ 手动绑定 | ✅ 三级目录发现 | ✅ 注册表自动注册 | ✅ 技能目录 |
| 元数据声明 | ❌ 仅 name/desc | ✅ YAML frontmatter | ✅ 工具集分类 | ✅ 结构化存储 |
| 版本管理 | ❌ 无 | ❌ 无 | ✅ 自动改进 | ❌ 无 |
| 依赖声明 | ❌ 无 | ❌ 无 | ❌ 无 | ❌ 无 |
| 动态加载 | ❌ 无 | ✅ 热重载 | ✅ 运行时创建 | ❌ 无 |
| 模板替换 | ❌ 无 | ✅ `{{model}}` | ❌ 无 | ❌ 无 |

#### 4.1.3 优化建议

**P0 - Skill 元数据与自动发现：**
- 为 SkillPackage 增加 `skill.yaml` 元数据文件，支持声明：
  - `allowedTools`: 允许使用的工具白名单
  - `autoActivationPaths`: 自动激活路径模式（如 `**/*.java`）
  - `dependencies`: 依赖的其他 Skill
  - `version`: 版本号
- 实现 Skill 目录监听器（参考 Qwen Code 的文件监听），支持热重载

**P1 - Skill 运行时动态管理：**
- 在 AgentContext 中增加 `activeSkills` 集合
- 实现 `SkillSelector`：根据用户输入 + 当前文件类型 + 历史使用频率自动选择 Skill
- 支持会话中 `/skill add <name>`、`/skill remove <name>` 指令

**P2 - Skill 版本与依赖：**
- SkillPackage 表增加 `version`、`dependencies` 字段
- 导入时解析依赖关系图，自动安装依赖 Skill
- 支持 Skill 版本锁定（类似 npm 的 package-lock）

---

### 4.2 用户会话记录

#### 4.2.1 现状问题

1. **会话模型较简单**：仅支持线性 path + depth，无多分支可视化支持
2. **无会话压缩机制**：长会话历史全部加载，Token 消耗线性增长
3. **无会话自动命名**：标题默认"新对话"，需手动修改
4. **无会话归档/搜索**：会话列表仅按时间排序，无全文检索
5. **无跨会话关联**：用户多个会话间无关联，无法形成知识网络
6. **分页加载粒度粗**：按 depth 切片，非语义化分页

#### 4.2.2 开源实践对比

| 特性 | AgentScope | Qwen Code | Codex CLI | HermesAgent |
|------|-----------|-----------|-----------|-------------|
| 分支模型 | ✅ path/depth | ❌ 线性 | ❌ 线性 | ❌ 线性 |
| 会话压缩 | ❌ 无 | ✅ `/compress` | ✅ `/compact` | ✅ context_compressor |
| 自动命名 | ❌ 无 | ✅ 自动标题 | ❌ 无 | ❌ 无 |
| 全文搜索 | ❌ 无 | ❌ 无 | ❌ 无 | ✅ SQLite+FTS5 |
| 跨会话关联 | ❌ 无 | ❌ 无 | ✅ Native Memories | ✅ 谱系追踪 |
| 会话恢复 | ✅ 基于 MySQL | ✅ 支持 | ❌ 进程绑定 | ✅ 双存储 |

#### 4.2.3 优化建议

**P0 - 会话压缩与 Token 管理：**
- 实现 `/compact` 或自动压缩机制：
  - 当消息数超过阈值（如 50 条）时，触发 LLM 摘要
  - 将历史消息压缩为 `summary` 节点，保留最近 N 条原始消息
  - 参考 AutoContextMemory 的压缩配置，但增加会话级控制
- 在 ChatMessage 中增加 `compressedContent` 字段，存储摘要版本

**P1 - 自动命名与搜索：**
- 会话创建后，首条用户消息到达时异步生成标题（LLM 调用）
- 为 ChatMessage 内容建立全文索引（Elasticsearch 或 MySQL FTS）
- 支持按内容搜索历史会话

**P2 - 跨会话知识网络：**
- 增加 `SessionRelation` 表，记录会话间关系（如"衍生自"、"关联主题"）
- 用户画像表：跨会话提取用户偏好、常用工具、项目结构
- 新会话初始化时注入用户画像摘要

---

### 4.3 记忆处理

#### 4.3.1 现状问题

1. **记忆层次单一**：仅短期（InMemoryMemory）和长期（AutoContextMemory）两层，无用户级/项目级静态记忆
2. **无文件驱动记忆**：不支持 `AGENTS.md`、`CLAUDE.md` 等项目级记忆文件
3. **记忆无持久化语义**：AutoContextMemory 的压缩结果不持久化到磁盘，进程重启丢失
4. **无跨会话记忆提取**：无法从已完成会话中提取洞察供未来使用
5. **记忆树管理粗糙**：IMemoryFactory 使用全局静态 ConcurrentHashMap，无 TTL 清理，存在内存泄漏风险

#### 4.3.2 开源实践对比

| 特性 | AgentScope | Claude Code | Codex CLI | Qwen Code | HermesAgent |
|------|-----------|-------------|-----------|-----------|-------------|
| 静态记忆文件 | ❌ 无 | ✅ CLAUDE.md | ✅ codex.md | ✅ QWEN.md | ✅ SOUL.md |
| 自动提取 | ❌ 无 | ✅ Auto Memory | ✅ Native Memories | ✅ Extract/Dream | ✅ 经验创建 Skill |
| 跨会话共享 | ❌ 无 | ✅ 全局记忆 | ✅ memory_summary | ✅ 全局作用域 | ✅ SQLite+FTS5 |
| 压缩策略 | ✅ AutoContext | ❌ 无 | ❌ 无 | ✅ 多级压缩 | ✅ 有损摘要 |
| 持久化 | ❌ 仅 MySQL | ✅ 文件系统 | ✅ 文件系统 | ✅ 文件系统 | ✅ SQLite |
| TTL 清理 | ❌ 无 | ✅ 30天修剪 | ✅ 30天修剪 | ✅ 自动维护 | ❌ 无 |

#### 4.3.3 优化建议

**P0 - 多层次记忆体系：**
引入四层记忆架构：

```
L1: 工作记忆 (Working Memory) - 当前会话上下文 → InMemoryMemory
L2: 短期记忆 (Short-term Memory) - 最近 N 会话摘要 → Redis/MySQL
L3: 长期记忆 (Long-term Memory) - 用户画像、项目知识 → 向量数据库
L4: 静态记忆 (Static Memory) - AGENTS.md、项目规则 → 文件系统
```

- **L4 实现**：支持项目级 `AGENTS.md`、用户级 `~/.agentscope/agents.md`
  - 启动 Agent 时自动读取并注入 system prompt
  - 文件变更时热重载

**P1 - 记忆持久化与自动提取：**
- AutoContextMemory 压缩结果持久化到 MySQL/Redis
- 后台任务：会话结束 6+ 小时后，LLM 提取关键洞察存入 `user_memory` 表
- 实现记忆修剪：30 天未访问的记忆自动降级或删除

**P2 - 记忆树治理：**
- IMemoryFactory 增加 `ScheduledExecutorService` 定期清理过期根节点
- 为 TreeNode 增加 `lastAccessTime` 和 `ttl` 字段
- 引入 WeakReference 替代强引用，允许 GC 回收不活跃记忆

---

### 4.4 Tools/MCP 加载与治理

#### 4.4.1 现状优势

1. ✅ **MCP 懒加载**：Agent 创建时不连接 MCP，调用时才初始化
2. ✅ **共享 MCP 上下文**：`sharedContexts` 避免重复创建连接
3. ✅ **运行时降级**：`McpRuntimeDegradeService` 基于 Redis 计数自动降级
4. ✅ **多协议支持**：stdio/sse/http 三种协议
5. ✅ **工具治理**：`McpToolExposureMode.SELECTED_ONLY` 支持白名单

#### 4.4.2 现状问题

1. **无工具调用链路追踪**：无法追踪某次请求调用了哪些工具、耗时、成功率
2. **无工具性能指标**：缺少工具级 QPS、Latency、Error Rate 监控
3. **MCP 工具无缓存**：每次调用都重新序列化 schema
4. **无工具编排**：不支持工具间的依赖编排（如必须先 A 后 B）
5. **子 Agent 加载较重**：`registerSubAgents` 同步创建 Agent 实例

#### 4.4.3 开源实践对比

| 特性 | AgentScope | HermesAgent | Qwen Code | Codex CLI |
|------|-----------|-------------|-----------|-----------|
| 懒加载 | ✅ 有 | ❌ 无 | ✅ 有 | ✅ 有 |
| 自动降级 | ✅ 有 | ❌ 无 | ❌ 无 | ❌ 无 |
| 链路追踪 | ❌ 无 | ❌ 无 | ❌ 无 | ❌ 无 |
| 性能指标 | ❌ 无 | ❌ 无 | ❌ 无 | ❌ 无 |
| 工具编排 | ❌ 无 | ✅ 工具集 | ✅ SkillTool | ❌ 无 |
| 调用审批 | ✅ IConfirmationHook | ❌ 无 | ✅ Approval | ✅ 三层审批 |

#### 4.4.4 优化建议

**P0 - 工具调用可观测性：**
- 增加 `ToolInvocationLog` 表/ES 索引，记录：
  - toolName, sessionId, latency, success, input/output(脱敏), mcpServerId
- 提供工具级 Dashboard：调用次数、平均耗时、错误率 Top N

**P1 - 工具缓存与编排：**
- MCP Tool Schema 缓存：解析后的 `McpSchema.Tool` 存入 Caffeine 本地缓存
- 工具编排 DSL：支持声明式工具链（如 `workflow: [toolA -> toolB -> toolC]`）

**P2 - 子 Agent 优化：**
- 子 Agent 延迟初始化：改为 `Supplier<Agent>` 真正调用时才创建
- 子 Agent 池化：对高频子 Agent 维护对象池，减少重复创建开销

---

### 4.5 对话系统与上下文管理

#### 4.5.1 现状问题

1. **AgentContext 过于简单**：仅 threadId/runId/memoryActive/planActive，无当前主题、意图、情感等语义信息
2. **无 Plan Mode**：规划与执行混合，用户无法先审阅计划再执行
3. **无对话状态机**：对话无显式状态（如 `GATHERING_INFO` → `PLANNING` → `EXECUTING` → `REVIEWING`）
4. **上下文传递全靠 ThreadLocal**：跨线程/异步任务时容易丢失
5. **无用户意图识别**：每次请求都走完整 ReAct 循环，简单问答也消耗大量 token

#### 4.5.2 开源实践对比

| 特性 | AgentScope | Qwen Code | HermesAgent | Codex CLI |
|------|-----------|-----------|-------------|-----------|
| Plan Mode | ❌ 无 | ✅ 显式状态机 | ❌ 无 | ❌ 无 |
| 对话状态 | ❌ 无 | ✅ 规划/执行分离 | ❌ 无 | ❌ 无 |
| 意图识别 | ❌ 无 | ❌ 无 | ❌ 无 | ❌ 无 |
| 上下文传递 | ⚠️ ThreadLocal | ✅ 结构化请求 | ✅ 双存储 | ✅ 进程内 |
| 审批流程 | ✅ Hook | ✅ Approval | ❌ 无 | ✅ 三层模型 |

#### 4.5.3 优化建议

**P0 - Plan Mode 支持：**
- 增加 `PlanModeEnabled` 配置
- 当启用时，Agent 先输出计划（工具调用列表），等待用户确认（`confirm_plan`）后才执行
- 参考 Qwen Code 的 `exit-plan-mode` 工具设计

**P1 - 对话状态机：**
```java
enum DialogueState {
    IDLE,           // 等待用户输入
    GATHERING,      // 收集信息（追问）
    PLANNING,       // 生成计划
    PLAN_REVIEW,    // 计划待审阅
    EXECUTING,      // 执行工具
    RESPONDING,     // 生成回复
    ERROR           // 错误处理
}
```
- 状态流转通过 Hook 拦截实现
- 前端根据状态显示不同 UI（如执行中显示进度条）

**P2 - 意图识别与快捷路径：**
- 轻量级意图分类器（规则/小模型）：识别 `CHAT` / `CODE` / `SEARCH` / `PLAN` 等意图
- `CHAT` 意图跳过 ReAct 循环，直接对话回复
- `CODE` 意图自动激活代码执行 Skill 和相关工具

---

### 4.6 其他优化点

#### 4.6.1 配置体系

**现状**：配置分散在 AgentDefinition 各字段，无统一优先级。

**优化**：引入六级配置优先级（参考 Qwen Code）：
1. 请求级参数（`RunAgentInput.params`）
2. 环境变量
3. 项目级配置（工作空间 `.agentscope/config.yaml`）
4. 用户级配置（`~/.agentscope/config.yaml`）
5. 系统级配置（`/etc/agentscope/config.yaml`）
6. 默认值

#### 4.6.2 Hook 系统增强

**现状**：Hook 为简单拦截器，无优先级、无条件触发。

**优化**：
- Hook 增加 `priority` 和 `condition` 属性
- 支持条件 Hook：仅当 `toolName matches "shell.*"` 时触发
- Hook 链可视化：前端展示当前生效的 Hook 列表

#### 4.6.3 安全与沙箱

**现状**：代码执行有 allowedCommands 白名单，但无完整沙箱。

**优化**（参考 Codex CLI）：
- 引入 seatbelt/sandbox-exec 或 Docker 沙箱执行代码
- 文件访问限制：仅允许访问工作空间目录
- 网络访问控制：默认禁止出站网络，需显式开启

#### 4.6.4 多模态支持

**现状**：仅支持文本对话。

**优化**：
- 文件上传后自动识别类型（图片/文档/代码）
- 图片支持：Vision Model 调用
- 文档支持：自动 RAG 索引

#### 4.6.5 A/B 测试与实验

**现状**：无实验机制。

**优化**：
- Agent 配置支持 `experimentId` 字段
- 同一路由按比例分流到不同 Agent 版本
- 自动收集对比指标（满意度、完成率、Token 消耗）

---

## 5. 优化优先级汇总

| 优先级 | 领域 | 优化项 | 参考框架 |
|--------|------|--------|----------|
| P0 | Skills | Skill 元数据与自动发现 | Qwen Code |
| P0 | Memory | 多层次记忆体系（L1-L4） | Claude Code, Codex CLI |
| P0 | Session | 会话压缩与 Token 管理 | Qwen Code, Codex CLI |
| P0 | Tools | 工具调用可观测性 | - |
| P0 | Dialogue | Plan Mode 支持 | Qwen Code |
| P1 | Skills | Skill 运行时动态管理 | Qwen Code |
| P1 | Memory | 记忆持久化与自动提取 | Codex CLI |
| P1 | Session | 自动命名与搜索 | Qwen Code |
| P1 | Tools | 工具缓存与编排 | HermesAgent |
| P1 | Dialogue | 对话状态机 | - |
| P2 | Skills | Skill 版本与依赖 | - |
| P2 | Memory | 记忆树治理（TTL/WeakRef） | - |
| P2 | Session | 跨会话知识网络 | HermesAgent |
| P2 | Tools | 子 Agent 优化 | - |
| P2 | Dialogue | 意图识别与快捷路径 | - |
| P2 | Config | 六级配置优先级 | Qwen Code |
| P2 | Security | 沙箱执行 | Codex CLI |

---

## 6. 架构演进建议

### 6.1 短期（1-2 个月）

1. **引入 L4 静态记忆**：支持 `AGENTS.md` 文件注入
2. **实现会话压缩**：基于 AutoContextMemory 扩展会话级压缩
3. **增加工具调用日志**：建立 ToolInvocationLog 表
4. **Plan Mode MVP**：支持计划生成与用户确认

### 6.2 中期（3-6 个月）

1. **Skill 元数据体系**：`skill.yaml` + 自动发现 + 热重载
2. **记忆自动提取**：后台任务从会话提取洞察
3. **对话状态机**：完整状态流转与前端联动
4. **配置中心**：六级优先级配置体系

### 6.3 长期（6-12 个月）

1. **跨会话知识网络**：用户画像 + 会话关系图谱
2. **工具编排引擎**：可视化工具链编排
3. **多模态支持**：图片/文档原生处理
4. **沙箱安全体系**：完整代码执行隔离

---

## 7. 参考资源

- OpenCode: https://github.com/opencode-ai/opencode
- Codex CLI: https://github.com/openai/codex
- Claude Code: https://docs.anthropic.com/en/docs/claude-code
- Qwen Code: https://github.com/QwenLM/qwen-code
- HermesAgent: https://github.com/NousResearch/HermesAgent
- OpenClaw: https://github.com/stitionai/openclaw
- Microsoft Multi-Agent Reference Architecture: https://github.com/Azure-Samples/multi-agent-coding-web-app
