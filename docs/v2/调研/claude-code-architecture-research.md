# Claude Code 架构深度研究

> 研究日期：2026-05-23
> 数据来源：Anthropic 官方文档、DeepWiki 源码分析、社区逆向工程文档

---

## 1. 记忆系统（Memory System）

### 1.1 六层记忆架构

Claude Code 的记忆系统是一个 **文件化、多层级的架构**，核心原则：**更具体的层覆盖更一般的层**。

| 层级 | 名称 | 路径 | 可见性 | 用途 |
|------|------|------|--------|------|
| 1 | 组织策略 | `/Library/Application Support/ClaudeCode/CLAUDE.md` | 全组织 | 公司级编码标准与安全策略 |
| 2 | 项目记忆 | `./CLAUDE.md` 或 `./.claude/CLAUDE.md` | 团队（通过 Git） | 共享项目指令——架构规范、编码标准、常见工作流 |
| 3 | 项目规则 | `./.claude/rules/*.md` | 团队（通过 Git） | 模块化规则文件，按主题拆分，支持路径限定 |
| 4 | 用户记忆 | `~/.claude/CLAUDE.md` | 仅自己（所有项目） | 个人偏好，跨项目生效 |
| 5 | 本地项目记忆 | `./CLAUDE.local.md` | 仅自己（自动 gitignore） | 个人项目级设置——本地测试 URL、沙箱配置 |
| 6 | 自动记忆 | `~/.claude/projects/<project>/memory/` | 仅自己（按项目） | Claude 自己的笔记——调试发现、架构观察、用户偏好 |

### 1.2 CLAUDE.md 加载机制

| 文件位置 | 加载行为 |
|----------|----------|
| 当前目录及父目录 | 启动时 **立即加载** |
| 子目录（如 `frontend/CLAUDE.md`） | **懒加载/按需加载**——仅当 Claude 在该目录下读取文件时注入 |
| 自动记忆的 `MEMORY.md` | 仅加载前 **200 行** |

**设计意图**：在大型 monorepo 中，可在 `frontend/CLAUDE.md` 和 `backend/CLAUDE.md` 放置领域特定规则，Claude 只在实际工作于该目录时加载，不浪费上下文窗口。

### 1.3 `.claude/` 目录结构

```
project/
├── CLAUDE.md                  # 层 2：主项目记忆
├── CLAUDE.local.md            # 层 5：个人本地设置（gitignored）
├── .claude/
│   ├── CLAUDE.md              # 层 2（替代位置）
│   ├── settings.json          # 项目级设置
│   ├── rules/                 # 层 3：模块化规则
│   │   ├── code-style.md
│   │   ├── testing.md
│   │   ├── frontend/
│   │   │   ├── react.md
│   │   │   └── styles.md
│   │   └── backend/
│   │       ├── api.md
│   │       └── database.md
│   └── agents/                # 自定义 Agent 定义
│       └── my-agent.md
```

### 1.4 路径限定规则（Path-Scoped Rules）

规则文件可包含 YAML frontmatter 中的 `paths` 字段，仅当 Claude 处理匹配文件时激活：

```yaml
---
paths:
  - "src/api/**/*.ts"
---
# API 开发标准
- 所有 API 端点必须包含输入验证
```

- 支持标准 glob 模式和大括号展开（如 `*.{ts,tsx}`）
- 没有 `paths` 字段的规则无条件加载

### 1.5 自动记忆（Layer 6）详细机制

**唯一由 Claude 自己书写的层**。其他记忆文件均由人类编写。

**存储结构**：
```
~/.claude/projects/<project>/memory/
├── MEMORY.md          # 索引文件，前 200 行自动加载
├── debugging.md      # 调试笔记（按需加载）
├── api-conventions.md # API 决策（按需加载）
```

**相关记忆召回**（`findRelevantMemories.ts`）——三步流程：
1. **扫描**：`scanMemoryFiles()` 读取记忆目录中所有 `.md` 文件的头部
2. **清单生成**：`formatMemoryManifest()` 创建文件名和描述的紧凑列表
3. **选择**：一个 **轻量级模型调用** 识别当前轮次需要哪些文件

**关键设计**：不是暴力注入全部记忆，而是通过 LLM 辅助检索保持上下文精简。

**记忆提取**（`shouldExtractMemory`）：
- 在成功的工具执行或任务完成后触发
- 模型被要求区分 "持久事实" vs "瞬态状态"
- 新 markdown 文件创建在 `memories/` 文件夹，新条目追加到 `MEMORY.md`

**大小限制**：
- `MEMORY.md` 最多 **200 行 / 25,000 字节**
- `truncateEntrypointContent` 确保即使手动扩展，LLM 只接收可管理的尾部

### 1.6 `@import` 语法

```markdown
See @README for project overview and @package.json for available npm commands.
```

| 特性 | 行为 |
|------|------|
| 路径解析 | 相对路径从包含 import 的文件解析 |
| 递归深度 | 最多 **5 层** |
| 首次审批 | 遇到外部 import 时触发审批对话框 |
| 排除 | 代码块和行内代码中的 `@` 不被解析 |

### 1.7 会话记忆与压缩

- 由 `sessionMemory.ts` 管理的瞬态记忆
- 当对话超过 token 限制时，`autoCompactIfNeeded` 触发
- 生成被丢弃消息的摘要，作为 "Session Memory" 块注入回提示词

### 1.7 团队记忆同步

- `watcher.ts` 监控本地团队记忆目录变更
- 推送本地更新到远程仓库，拉取队友更新
- 确保 "项目事实层" 在组织中同步

### 1.8 Agent 记忆快照

- `loadAgentsDir.ts`：发现可用的 Agent 模板及其关联记忆文件
- `agentMemorySnapshot.ts`：从已知基线初始化子 Agent 的记忆

---

## 2. 技能系统（Skills System）

### 2.1 核心机制

技能通过 `SKILL.md` 文件定义可复用指令，按需加载。与 `CLAUDE.md`（始终在上下文中）不同，技能内容 **仅在调用时加载**，节省上下文 token。

**技能内容生命周期**：
- 调用时，渲染的 `SKILL.md` 内容进入对话，在该会话剩余时间内 **持久存在**
- **自动压缩**在 token 预算内保留已调用技能：
  - 重新附加最近调用，保留每个技能的 **前 5,000 token**
  - 合并预算：所有重新附加技能共 **25,000 token**
  - 从最近调用的技能开始填充；较旧的技能可能被丢弃

### 2.2 SKILL.md 格式

**YAML Frontmatter 字段**：

| 字段 | 说明 |
|------|------|
| `name` | 显示名称，默认为目录名 |
| `description` | 技能描述及触发条件（截断至 1,536 字符） |
| `when_to_use` | 附加触发上下文 |
| `argument-hint` | 自动补全提示 |
| `arguments` | 命名位置参数用于 `$name` 替换 |
| `disable-model-invocation` | `true` = 仅用户可调用 |
| `user-invocable` | `false` = 从 `/` 菜单隐藏 |
| `allowed-tools` | 技能激活时免审批的工具列表 |
| `model` | 技能激活时的模型覆盖 |
| `context` | 设为 `fork` 在分支子 Agent 中运行 |
| `agent` | `context: fork` 时使用的子 Agent 类型 |
| `hooks` | 限定于技能生命周期的钩子 |
| `paths` | 限制技能激活的 glob 模式 |

### 2.3 调用方式

- **直接调用**：输入 `/skill-name`
- **自动调用**：当对话匹配 `description` 时 Claude 自动加载
- **带参数**：`/fix-issue 123`，`$ARGUMENTS` / `$0` / `$1` 替换

### 2.4 技能注册与存储位置

| 级别 | 路径 | 适用于 |
|------|------|--------|
| 企业 | 托管设置 | 组织内所有用户 |
| 个人 | `~/.claude/skills/<skill-name>/SKILL.md` | 你的所有项目 |
| 项目 | `.claude/skills/<skill-name>/SKILL.md` | 仅此项目 |
| 插件 | `<plugin>/skills/<skill-name>/SKILL.md` | 插件启用处 |

**优先级**：企业 > 个人 > 项目。插件技能使用 `plugin-name:skill-name` 命名空间，不会冲突。

### 2.5 技能发现机制

- Claude Code **监控技能目录**的文件变更，当前会话内即时生效
- **父目录**：从启动目录到仓库根目录的 `.claude/skills/` 都会加载
- **嵌套目录**：处理子目录文件时，按需发现嵌套的 `.claude/skills/`（支持 monorepo）
- `--add-dir` 添加的目录中的 `.claude/skills/` 也会自动加载

### 2.6 动态上下文注入

`` !`<command>` `` 语法在技能内容发送给 Claude **之前**运行 shell 命令：

```yaml
## PR context
- PR diff: !`gh pr diff`
- PR comments: !`gh pr view --comments`
```

### 2.7 内置技能

`/code-review`、`/batch`、`/debug`、`/loop`、`/claude-api`、`/run`、`/verify`

### 2.8 Slash Commands 与 Skills 的关系

- 自定义命令（`.claude/commands/deploy.md`）和技能（`.claude/skills/deploy/SKILL.md`）都创建 `/deploy`
- **自定义命令已合并到技能中**——现有 `.claude/commands/` 文件继续工作
- 同名时 **技能优先**

---

## 3. 工具调用系统（Tool Calling）

### 3.1 工具架构

Claude Code 实现了一个 **类型化工具系统**，每个工具代表暴露给 AI Agent 的特定能力。工具通过 **权限层** 执行，验证并可能提示用户审批。

**工具交互流程**：
1. 自然语言请求 → Agent 解析
2. 工具选择 → Agent 选择类型化工具
3. 权限层 → 按规则评估（托管 > 用户 > 本地）
4. 执行 → 审批通过后工具执行

### 3.2 内置工具列表

| 工具 | 用途 | 备注 |
|------|------|------|
| `FileReadTool` | 读取文件内容 | 支持部分视图，超大文件返回截断首页 |
| `FileWriteTool` | 创建或覆盖文件 | 路径安全 + 符号链接检查 |
| `FileEditTool` | 基于行的编辑 | 包含 diff 逻辑 |
| `BashTool` | 执行 shell 命令 | 支持沙箱执行 |
| `PowerShellTool` | Windows shell 执行 | |
| `WebFetchTool` | 获取 URL 内容 | |
| `WebSearchTool` | 执行网络搜索 | |
| `AskUserQuestion` | 向用户提问 | |
| `AgentTool` | 委派子 Agent | |

### 3.3 权限系统架构

**权限规则层级**：
```
托管规则（最高优先级）
  ↓
用户规则
  ↓
本地规则（最低优先级）
```

**三种规则类型**：

| 规则类型 | 行为 |
|----------|------|
| `deny` | 无条件阻止工具执行 |
| `ask` | 每次都提示用户审批 |
| `allow` | 自动审批匹配的模式 |

**权限模式**：

| 模式 | 说明 |
|------|------|
| Auto-approve | 匹配 allow 模式的工具免提示执行 |
| Ask | 交互式提示用户审批 |
| Bypass | `--dangerously-skip-permissions` 跳过所有权限检查，可全局禁用 |

### 3.4 Bash 沙箱系统

| 配置 | 说明 |
|------|------|
| `autoAllowBashIfSandboxed` | 沙箱活跃时允许命令免提示执行 |
| `allowedDomains` | 网络访问域名白名单 |
| Unix socket 阻止 | 防止通过 unix socket 逃逸沙箱 |
| 命令排除 | 特定命令可从沙箱中排除 |

### 3.5 输出 Token 分层策略

| 级别 | Token 限制 | 用途 |
|------|-----------|------|
| `CAPPED_DEFAULT_MAX_TOKENS` | 8,000 | >99% 的请求，优化调度 |
| `ESCALATED_MAX_TOKENS` | 64,000 | 达到 8k 限制时的"干净重试" |

---

## 4. 上下文压缩（Context Compaction）

### 4.1 Token 预算与上下文分配

| 变量 | 默认值 | 用途 |
|------|--------|------|
| `MODEL_CONTEXT_WINDOW_DEFAULT` | 200,000 | Claude 3 模型基线上下文 |
| `MAX_OUTPUT_TOKENS_FOR_SUMMARY` | 20,000 | 为摘要 API 预留的输出 token |
| `AUTOCOMPACT_BUFFER_TOKENS` | 13,000 | 窗口满之前的"提前触发"阈值 |

**有效窗口** = 200,000 - 20,000 - 13,000 = **约 167,000 token**

### 4.2 自动压缩生命周期

`autoCompactIfNeeded` 是主要编排器，在每次工具执行或用户轮次后监控 token 使用。

**压缩流程**：
1. `QueryEngine` 检测 token 使用接近限制
2. 生成一个 **分支 Agent** 处理摘要
3. 分支 Agent 处理对话历史并生成摘要
4. 原始对话被摘要 + 重新注入的状态替换
5. Agent 使用压缩后的上下文继续运行

### 4.3 安全机制

**连续失败断路器**：
- 如果压缩连续失败 **3 次**（`MAX_CONSECUTIVE_AUTOCOMPACT_FAILURES`），系统 **停止尝试** 压缩该会话
- 防止无限循环和 API 额度浪费

**PTL（Prompt Too Long）回退**：
- 如果压缩请求本身对模型来说太长，`truncateHeadForPTLRetry` 激活
- "剥洋葱"：移除 **20% 最旧的消息**然后重试

### 4.4 压缩后状态重新注入

| 保留项 | 来源 |
|--------|------|
| 文件附件 | 仍在"工作集"中最近通过 `FileReadTool` 访问的文件 |
| 计划与技能 | 当前 `CLAUDE.md` 计划或活跃技能定义 |
| 工具增量 | 特别是 MCP 服务器的工具定义 |

### 4.5 压缩前内容剥离

| 剥离函数 | 移除内容 | 目的 |
|----------|----------|------|
| `stripImagesFromMessages` | 用 `[image]` 占位符替换图片/文档块 | 防止 OOM |
| `stripReinjectedAttachments` | 移除即将重新添加的项目 | 避免冗余摘要和幻觉 |

### 4.6 摘要 Prompt Cache 优化

- 使用 `tengu_compact_cache_prefix` 让分支 Agent **复用主对话的 prompt cache**
- 显著减少处理历史生成摘要所需的 token

---

## 5. MCP 集成（MCP Integration）

### 5.1 配置到工具的管线

```
配置（config.ts）→ 连接管理（useManageMCPConnections.ts）→ 客户端创建（client.ts）→ 工具注册
```

### 5.2 传输类型

| 传输类型 | 实现类 | 用途 | 认证 |
|----------|--------|------|------|
| `stdio` | `StdioClientTransport` | 本地子进程（默认） | 系统级权限 |
| `sse` | `SSETransport` | 远程 SSE 服务 | OAuth via `ClaudeAuthProvider` |
| `http` | `StreamableHTTPClientTransport` | HTTP 流式服务 | OAuth |
| `sse-ide` | `SSETransport` | IDE 集成服务器 | Lockfile token |
| `ws-ide` | `WebSocketTransport` | IDE WebSocket 桥 | `X-Claude-Code-Ide-Authorization` |
| `ws` | `WebSocketTransport` | 通用 WebSocket 服务 | Session ingress token |
| `claudeai-proxy` | `SSETransport` | Claude.ai 基础设施 | OAuth bearer + 401 retry |

### 5.3 认证状态机

- **401 Unauthorized** → 触发 `handleRemoteAuthFailure()`
- **冗余 UI 提示防止** → 使用 15 分钟 TTL 的缓存
- **永久性服务端拒绝**（401/403/404）→ 传输立即转为 `closed` 状态

### 5.4 连接生命周期

**初始化与记忆化**：
- `connectToServer` 使用从 **服务器名称 + 配置签名** 派生的键进行记忆化
- 连接关闭时主动清除多个缓存

**配置合并**：四个作用域——User、Project、Local、Managed
**去重**：`dedupClaudeAiMcpServers()` 跨作用域识别同一服务器

**关闭——stdio 进程清理**：

| 步骤 | 信号 | 等待时间 |
|------|------|----------|
| 1 | SIGINT | 100ms |
| 2 | SIGTERM | 400ms |
| 3 | SIGKILL | 立即 |

**总清理窗口上限 600ms。**

### 5.5 工具发现与集成

**工具规范化**：
- 命名约定：`mcp__<serverName>__<toolName>`
- 描述截断：上限 **2,048 字符**（`MAX_MCP_DESCRIPTION_LENGTH`）
- 注解映射：MCP `annotations` → 内部能力（`readOnlyHint`、`destructiveHint`）

**工具发现缓存**：LRU 缓存（大小 20），避免活跃会话期间昂贵的 `tools/list` 调用

### 5.6 并发限制

| 连接类型 | 并发限制 | 原因 |
|----------|----------|------|
| 本地（stdio） | 3 | 重量级 OS 进程 |
| 远程（HTTP/SSE） | 20 | 轻量级网络请求 |

### 5.7 Channel 通知（Kairos）

Channel 是可以向活跃会话推送外部事件的 MCP 服务器。入站消息由 `gateChannelServer()` 门控，用 `<channel>` XML 标签包装。支持远程审批/拒绝工具执行。

---

## 6. 会话管理（Session Management）

### 6.1 存储模型：仅追加 JSONL

核心存储是 `.jsonl` 文件，每个交互作为新行追加。

**转录条目类型**：

| 条目类型 | 持久化 | 影响 LLM 上下文 |
|----------|--------|-----------------|
| `user`、`assistant` | 是 | 是 |
| `attachment`、`system` | 是 | 是 |
| `progress` | 否 | 否 |
| `content-replacement` | 是 | 是（用于 snip healing） |
| `metadata`（标题、标签） | 是 | 否（仅用于 UI/Resume） |

**文件组织**：
- 主转录：`[projectDir]/[sessionId].jsonl`
- 子 Agent 侧链：`[projectDir]/[sessionId]/subagents/agent-[agentId].jsonl`

### 6.2 异步写入队列

使用 **异步写入队列** 确保文件 I/O 不阻塞主执行循环或 TUI 渲染。

- `appendEntry`：持久化任何会话事件的主入口
- `drainWriteQueue`：批量处理待写入的 JSON 行，使用 `fs.appendFile` 刷盘

### 6.3 元数据重追加与轻量读取器

- 元数据变更时追加到 JSONL 尾部
- **轻量读取器**（64KB 尾部窗口）：仅扫描 `.jsonl` 文件最后 64KB 以快速填充会话列表 UI
- 因为元数据重追加到尾部，轻量读取器无需解析整个文件

### 6.4 对话恢复管线（`/resume`）

`conversationRecovery.ts` 将原始 JSONL 条目转回功能性的 `Transcript`：

1. **JSONL 解析**：读取文件，过滤有效 `Entry` 类型
2. **链重建**：通过 `parentUuid` 链接消息
3. **Snip Healing**：如果存在 `content-replacement` 条目（来自 compact 操作），用摘要版本替换原始大消息
4. **元数据恢复**：从尾部条目提取最新的 `title`、`mode`、`worktree`、`agent` 设置
5. **状态重新注入**：重建 `fileHistory` 和 `contextCollapse` 状态

### 6.5 远程会话同步（sessionIngress）

- 基于 token 的认证
- 非阻塞：`persistToRemote` 异步执行，不等待响应
- 仅主链：侧链（子 Agent）消息不同步到远程

### 6.6 关键设计决策

| 决策 | 原因 |
|------|------|
| 仅追加 = 崩溃安全 | 中断的写入不会损坏现有转录数据 |
| 无外部侧车数据库 | 元数据通过重追加共置于 JSONL 中 |
| 尾部扫描性能 | 64KB 轻量读取器避免解析多兆字节文件 |
| 压缩后 Snip Healing | 恢复的会话无缝反映压缩状态 |
| 全程异步 I/O | 写入队列和远程同步均非阻塞 |

---

## 7. 子 Agent / 分支模式（Sub-Agent / Fork Pattern）

### 7.1 AgentTool 架构

基于 **策略模式**，清晰分离路由逻辑和执行逻辑。

**模块职责**：

| 模块 | 职责 |
|------|------|
| Tool 主入口 | 输入模式、路由策略、同步/异步分支 |
| Agent Runner | 生命周期管理、上下文构建、MCP 初始化 |
| Fork Pattern | 构建缓存安全的消息前缀用于并行执行 |
| Agent Registry | 基于特性门控动态加载 Agent |
| Custom Agent Loader | 解析 Markdown/JSON Agent 定义 |

### 7.2 四种内置 Agent 类型

| Agent 类型 | 角色 | 工具约束 | 核心理念 |
|------------|------|----------|----------|
| Code-Review | 代码质量专家 | 通常只读 | "信任但验证"——关注可维护性和 bug |
| Security-Review | 安全审计 | 受限网络 | "假设被入侵"——寻找漏洞和泄露 |
| Verification | 对抗性测试 | 允许执行工具 | "证明它有效"——尝试找到解决方案的边界情况 |
| General-Purpose | 任务运行器 | 完整工具集 | 灵活委派任意子任务 |

### 7.3 Fork 模式——字节级上下文克隆

**核心机制**：子 Agent 继承父 Agent 的完整对话上下文，且在字节级别确保前缀一致以命中 Prompt Cache。

**工作原理**：
1. 父 Agent 到达 N 个 token 的状态
2. 调用 `agent` 工具并设置 `fork: true`
3. Fork 确保子 Agent 的初始提示 **在字节级别与父 Agent 的历史到 token N 完全匹配**
4. LLM 提供商的 Prompt Caching 识别前缀并 **从缓存恢复**，减少延迟和成本

**并行执行**：多个 Fork 共享完全相同的 API 请求前缀：
```
[System Prompt]         ← 所有 fork 相同
[...history]            ← 相同（从父级克隆）
[assistant tool_use]    ← 相同
[placeholder results]   ← 相同文本："Fork started — processing in background"
[<fork-boilerplate>]    ← 仅此部分不同
```

### 7.4 Fork Agent 定义

```ts
export const FORK_AGENT = {
  agentType: 'fork',
  tools: ['*'],              // 通配符：使用父级完整工具集
  maxTurns: 200,
  model: 'inherit',          // 继承父级模型
  permissionMode: 'bubble',  // 权限上冒到父级终端
  getSystemPrompt: () => '', // 不使用：父级已渲染的 prompt 直接传入
}
```

### 7.5 安全防护

**递归保护**：
- 每次 Agent 调用递增 `depth` 计数器，最大深度 **3-5 层**
- 循环检测：同一 Agent 类型在单链中无状态变化被调用两次时触发警告

**Fork 递归防护**（两层）：
1. `querySource` 检查：`toolUseContext.options.querySource === 'agent:builtin:fork'`
2. 消息扫描：`isInForkChild()` 扫描消息历史中的 `<fork-boilerplate>` 标签

**隔离模式**（`isolationMode: true`）：

| 限制 | 细节 |
|------|------|
| 环境变量 | 子 Agent 无法访问父级环境变量 |
| 文件系统 | 访问可能限于特定子目录 |
| 项目指令 | `omitClaudeMd` 可省略 `CLAUDE.md` |

**Fork 禁用条件**：
- 特性标志 `FORK_SUBAGENT` 关闭
- 运行在 Coordinator 模式
- 运行在非交互式会话（pipe/SDK 模式）

### 7.6 自定义 Agent

两种格式定义：
- **Markdown**：`.claude/agents/my-agent.md` → 解析为 `CustomAgentDefinition`
- **JSON**：更程序化的控制

自定义 Agent 是一等公民，拥有与内置 Agent 相同的执行路径（同步、异步、fork）。

### 7.7 子 Agent 编排流程

```
AgentTool.call({ prompt, name })
      │
      ▼
isForkSubagentEnabled() && !subagent_type?
      │
      ├── No → Normal agent path
      └── Yes → Fork path
            │
            ▼
      递归防护检查
            │
            ▼
      获取父级 system prompt（直接传递渲染版本）
            │
            ▼
      buildForkedMessages(prompt, assistantMessage)
            │
            ▼
      [可选] 构建工作树通知
            │
            ▼
      runAgent({ useExactTools: true, ... })
```

---

## 架构对比要点

与 CodeBuddy Code 的主要差异对照：

| 方面 | Claude Code | CodeBuddy Code |
|------|-------------|----------------|
| 记忆层次 | 6 层（含组织策略层） | 4 层（user/feedback/project/reference） |
| 记忆存储 | 文件化 Markdown + MANIFEST 索引 + 侧查询 | 文件化 Markdown + MEMORY.md 索引 |
| 记忆召回 | LLM 辅助检索（轻量模型选择相关文件） | 全量加载 MEMORY.md |
| 技能系统 | SKILL.md + Frontmatter + 动态注入 + `context: fork` | Skill 工具调用 + 插件系统 |
| 工具权限 | deny/ask/allow 三级 + 托管层级 + 沙箱 | 权限提示 + 沙箱 |
| 上下文压缩 | Fork Agent 摘要 + 状态重新注入 + PTL 回退 + 断路器 | 自动摘要 |
| MCP | 7 种传输 + OAuth + LRU 缓存 + Channel 通知 | MCP 客户端 |
| 会话持久化 | JSONL 追加 + Snip Healing + 64KB 轻量读取 | JSONL |
| 子 Agent | Fork 模式（字节级缓存共享）+ 4 种内置类型 + 自定义 | Agent 工具 |
