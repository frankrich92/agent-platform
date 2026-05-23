# 开源 Agent 实现横向对比与本项目优化方向

日期：2026-05-23
作者：Claude Opus 4.7
落盘说明：原始路径 `dcos/v2/调研/claude4.7-v2.md` 中 `dcos` 应为 `docs`，按既有目录结构归档到 `docs/v2/调研/claude4.7-v2.md`。

## 1. 文档说明

### 1.1 目的

对比当前主流开源 Agent / Coding Agent 实现，针对本项目（`agent-platform`，前身为 `.apboa/`）的 Agent 部分提出可落地的优化方向。重点覆盖：

- 对话与会话记录
- 工具加载与调用
- Skill 调用机制
- MCP 接入与治理
- 记忆处理
- 用户提示发起到推理结束之间所有可观测、可控、可重放的链路
- 其他用户未提及但有显著差距的环节

### 1.2 对比对象

| 项目 | 类型 | 主要参考价值 |
| --- | --- | --- |
| Anthropic Claude Code | 终端 / IDE Agent（闭源 CLI，开放协议和约定） | Skills 的 progressive disclosure、Hook 生命周期、Subagent、CLAUDE.md 记忆体系 |
| OpenAI Codex CLI | 开源 Rust Agent | Approval 三档模式、seccomp/Seatbelt 沙箱、JSONL 会话日志、AGENTS.md |
| sst/opencode | 开源 TS+Go Agent（client/server） | Per-turn 文件快照、消息分支、LSP 集成、Provider 抽象 |
| 第三方 Claude Code 兼容实现（统称 OpenClaw 类） | 开源克隆 | Skills/Hook/MCP 协议落地的另一条参考路径 |
| HermesAgent | 开源 Java Agent 平台（仓库历史归档已分析） | Run/Span/Capability 抽象、统一 SSE envelope、AI Native 方法论 |
| qwen-code（基于 Gemini CLI 改造） | 开源 TS Agent | 长上下文压缩、checkpoint、QWEN.md 记忆，多模态输入 |
| Aider / Cline / Continue | 终端 / IDE Agent | Git checkpoint、editor 集成、补充对照点 |

参考线：本项目（Apboa）—— 见 `docs/apboa/01-系统详细设计报告.md` 第 6、7 章和 `RTK.md`。

### 1.3 写作约束

- `.apboa/` 只读，所有建议都落到 `source/agent-platform/` 现有模块边界内。
- 保留第一阶段数据库表结构不变；如需新结构，通过新表增量引入而不是替换 `chat_session`、`chat_message`、`agentscope_sessions`。
- 模块映射使用 `RTK.md` 定义的 `agent-domain / agent-api / agent-biz / agent-admin / agent-infra / agent-runtimes / agent-repo / agent-adapter / agent-boot`。

## 2. 当前 Agent 链路基线摘要

为下面的对比提供锚点，先把 Apboa 现状压缩成一张图：

```mermaid
flowchart TB
  U[用户输入] --> CSS[ChatSession API<br/>chat_session/chat_message]
  CSS --> AGUI[/apboa/agui/{agentCode}/]
  AGUI --> CTX[AgentContext<br/>InheritableThreadLocal]
  CTX --> RAH[ReActAgentHelper 装配]
  RAH -->|每次请求重建| TK[Toolkit<br/>builtin + dynamic + MCP lazy + agent-as-tool + workspace]
  RAH --> KN[Knowledge / RAG]
  RAH --> SK[SkillBox<br/>skill_content + scripts]
  RAH --> HK[Hooks<br/>workspace + dynamic + studio]
  RAH --> MEM[Memory<br/>InMemoryMemory / AutoContextMemory]
  RAH --> PLN[PlanNotebook]
  RAH --> M[ChatModel]
  M --> SSE[AGUI SSE 事件]
  SSE -->|前端落库| CSS
  MEM -.->|memoryActive=true| AS[(agentscope_sessions)]
```

这张图在下文用作每个对比维度的差距对照面。

## 3. 横向对比矩阵

### 3.1 对话与会话记录

| 维度 | Apboa 现状 | Claude Code | Codex CLI | OpenCode | qwen-code | Hermes | 差距 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 消息存储 | 树形（parent_id/path/depth） + AgentScope MysqlSession 双轨 | JSONL transcript（线性，分支用 fork） | 会话 JSONL（线性 + rollout 文件） | per-message 文件，支持 fork | JSONL，支持 checkpoint | Run + Span + Step（关系型） | 树形是亮点；但 transcript 与 AgentScope 内部状态分离，重放时无法对齐 |
| 工具调用记录 | 作为 `chat_message.role=tool` 的一条消息存储 | tool_use / tool_result blocks 单独序列化 | 同上，结构化 | 同上 | 同上 | Span 维度独立 | 当前是字符串落库，缺少结构化 input/output/duration/cost |
| 推理过程 | `assistant` 内容里夹带 reasoning 文本 | thinking blocks 独立 | reasoning items | 独立 part | 独立 | Span trace | 缺少 reasoning 与 final answer 的物理隔离 |
| 流式协议 | 自定 AGUI SSE（agent / message / tool 等事件） | Anthropic streaming + Claude Code 自定 SSE | OpenAI Responses event + 内部 SSE | 自定 SSE envelope | 同 Gemini | 统一 SSE envelope | 当前事件粒度可用，但缺少 `run_id / span_id / cost / token_usage / model` 元数据 |
| Resume / Continue | memoryActive=true 时由 AgentScope 自动恢复 | --continue / --resume 显式 | resume from JSONL | 显式 fork from message | resume from session | run.id 显式 | Apboa 没有命令式 resume 接口；前端通过 `current_message_id` 改分支，但缺少“恢复完整 Agent 状态”的 API |
| 对话分支（fork） | 已有，前端可切换 | 部分实现 | 不支持 | 一等公民 | 部分 | Run 维度可派生 | Apboa 这点反而最强，但前端 UI 使用率低 |
| 检查点 / 快照 | 仅工作空间目录 | session-level | rollout file | per-turn 文件系统快照 | checkpoint | 可选 | 缺少“执行某轮前的 FS + memory + tool state” 三元快照 |

### 3.2 工具加载与调用

| 维度 | Apboa | Claude Code | Codex CLI | OpenCode | 备注 |
| --- | --- | --- | --- | --- | --- |
| 注册时机 | 每次请求装配 Toolkit（MCP 是 LazyMcpAgentTool） | 启动加载，session 持有 | 启动加载 | 启动加载，热插拔 | Apboa 每请求重建是为了多租户隔离，但浪费明显 |
| 工具描述注入 | 全量注入 system prompt | 全量 | 全量 + skill 化 | 全量 + 分组 | 工具数 > 30 时 system prompt 膨胀严重 |
| 并行 | `parallel=false` 强制串行 | 默认允许并行（read-only） | 允许并行 | 允许并行 | Apboa 太保守 |
| 超时 | 60s 全局 | per-tool 配置 | per-tool | per-tool | Apboa 缺少 per-tool 超时 |
| 人工确认 | `needConfirm` + ConfirmationHook | 三档 ask/allow/deny + 模式（plan/auto-edit/full-auto） | suggest/auto-edit/full-auto | per-tool ask/allow/deny + glob 范围 | Apboa 粒度只到工具开关；缺少基于参数（路径、命令、URL）的细粒度允许列表 |
| 错误回退 | 工具异常抛出后 ReAct 自然观测 | 同 | 同 | 同 | 缺少“工具失败次数阈值后自动降级” |
| 内置工具集 | search_replace_file（仅在代码执行启用） | Read/Edit/Write/Bash/Glob/Grep/WebSearch/WebFetch/Task/TodoWrite | shell/apply_patch/web_search/MCP | Read/Edit/Write/Bash/Glob/Grep/WebFetch/Todo/Task | Apboa 内置工具相对薄，依赖动态/Custom 工具填补 |
| 动态代码工具安全 | script-security 静态检查 | 不允许加载用户自写 Java | 不允许 | 不允许（仅 plugin） | Apboa 是唯一允许 JVM 内动态加载用户 Java/Groovy 的，安全压力远大于上述任一参照系 |

### 3.3 Skills 调用机制

这是和参照系差距最大的一块，单独展开。

| 维度 | Apboa | Claude Code Skills | qwen-code 类 | OpenCode | 备注 |
| --- | --- | --- | --- | --- | --- |
| 装载策略 | 选中即整包注入（skill_content + references + examples 拼接到 prompt 或 SkillBox 上下文） | 元工具 `skill` 仅暴露 name + description；正文按需读取（progressive disclosure） | 类似 Claude | 类似 Claude | Apboa 是 eager 模式 |
| 描述粒度 | skill_content 全文 | SKILL.md frontmatter（name + description ≤ 1024 char） | SKILL.md 风格 | SKILL.md 风格 | Apboa 没有“摘要”层 |
| 执行环境 | SkillBoxFactory 挂到 AgentScope SkillBox，可选代码执行 | 脚本/二进制驻留磁盘，模型按需 invoke | 同 | 同 | Claude 系把 skill 当“可调用的小工具集合 + 文档”，Apboa 当“可注入的知识 + 脚本” |
| 与工具关系 | skill_tools 关联表 | skill 内部可声明所需 tools | 同 | 同 | 概念相近，但 Apboa 是平铺关系 |
| Token 成本 | O(已选 skill 的总长度) | O(skill 数 × 摘要长度) | O(skill 数 × 摘要) | O(skill 数 × 摘要) | Apboa 在 skill 多或体量大时上下文消耗陡升 |
| 来源 | 手动 / Git / 本地 / 上传 | 文件目录约定（user-level / project-level） | 同 | 同 | Apboa 已有 Git 导入是亮点 |

**核心结论**：Apboa 的 skill_package 概念偏向“可复用知识包 + 可执行脚本”，而 Claude Code 系统的 Skills 是“可发现 + 按需展开 + 可执行”的元能力。前者塞 prompt，后者管索引。前者天花板取决于上下文窗口，后者天花板取决于模型对 skill 索引的检索能力。

### 3.4 MCP 接入

| 维度 | Apboa | Claude Code | Codex | OpenCode | 备注 |
| --- | --- | --- | --- | --- | --- |
| 协议 | HTTP/SSE/STDIO | 同 | 同 | 同 | 一致 |
| 装载 | LazyMcpAgentTool（基于落库 toolSchemas 注册占位，调用时再连接） | 启动连接，工具列表加入 toolset | 启动连接 | 启动连接 + 热加载 | Apboa 的 lazy 在弱依赖场景明显优势 |
| 配置层级 | 平台维度（mcp_server 表）+ Agent 绑定（agent_mcp_servers / agent_mcp_tool） | 三层：user / project / local | 全局 + project | 多层 | Apboa 缺少“项目级 / 用户级”分层；当前是平台级管理员视角 |
| 健康度 | activationStatus + healthStatus + activationRevision + runtimeFailThreshold | enabled flag + 重连 | 类似 | 类似 + 监控 | Apboa 这块设计较完整 |
| 工具命名空间 | 工具名直接落 mcp_tool；多 server 同名工具未隔离 | 自动加 server prefix（mcp__server__tool） | server prefix | server prefix | Apboa 容易冲突 |
| 凭据托管 | mcp_server.config 字段（可能明文） | env / secret 引用 | secret 引用 | secret 引用 | Apboa 依赖 KMS 改造 |
| Sampling / elicitation | 未支持 | 已实现 sampling | 部分 | 已实现 | MCP 协议较新部分 Apboa 落后 |

### 3.5 记忆处理

记忆是用户特别关心的点，单独细化。

| 维度 | Apboa | Claude Code | Codex | qwen-code | mem0 / Letta（学术参考） | 差距 |
| --- | --- | --- | --- | --- | --- | --- |
| 短期对话 | InMemoryMemory（窗口内全量） | 窗口 + auto compact | 窗口 + compact | 窗口 + compact | working memory | Apboa 等价 |
| 自动压缩 | AutoContextMemory + memory_compression_config | --compact / 自动触发 | /compact | /compact + 自动 | 任务完成后归档 | Apboa 有，但策略不可见、不可解释、不可重放 |
| 项目记忆 | 无（系统 prompt 模板是“静态 prompt”，不是“持久记忆”） | CLAUDE.md（项目根 + 父目录链） | AGENTS.md | QWEN.md / GEMINI.md | 项目级 KV | **明显缺失** |
| 用户记忆 | 无 | ~/.claude/CLAUDE.md（用户级） | ~/.codex/AGENTS.md | ~/.qwen/QWEN.md | 用户级 profile | **明显缺失** |
| Agent 维度持久记忆 | 无（system_prompt 和 prompt_template 不算） | 子目录 CLAUDE.md | per-project AGENTS.md | 同 | per-agent profile | **明显缺失** |
| 记忆写入工具 | 无 | `# 记忆这个：xxx` 触发写 CLAUDE.md | `/memory` 命令 | `save_memory` 工具 | remember 工具 | **明显缺失** |
| 记忆召回 | 仅 RAG 走向量库 | 启动时把 CLAUDE.md 拼到 system prompt（非召回） | 同 | 同 | embedding + recall API | Apboa 把“记忆”和“知识”混在一起：知识库走 RAG，但缺“轻量、运行态、跨会话个性化”的记忆层 |
| 多会话长程记忆 | agentscope_sessions 仅会话内 | 会话间记忆通过 CLAUDE.md 间接实现 | 同 | 同 | 显式 long-term store | Apboa 不具备“跨会话学到的偏好/事实”能力 |
| 命中可解释 | 无（黑箱压缩） | CLAUDE.md 在 prompt 里可见 | 同 | 同 | recall 时返回引用 | Apboa 用户难判断 Agent 为什么“记得”或“忘了” |

### 3.6 Hook / 生命周期事件

| 维度 | Apboa | Claude Code | OpenCode | 备注 |
| --- | --- | --- | --- | --- |
| 事件粒度 | WorkspaceValidate / WorkspaceWebsocket / 用户动态 Hook / Studio Hook / Confirmation | sessionStart / userPromptSubmit / preToolUse / postToolUse / preCompact / postCompact / sessionEnd / notification 等 | 类似 + custom | Apboa 偏“工具确认 + 工作空间通知”，Claude Code 是“跨整个 Agent 生命周期” |
| 触发方 | Java Bean 内置 + 在线代码 | JSON 配置 + shell 命令 / askAgent | TS 模块 / shell | Apboa 没有 shell-as-hook 形态（这反而是企业内更安全的形态：声明式而非动态 Java） |
| 阻塞能力 | 通过 ConfirmationHook 阻塞 | preToolUse 可拦截 | 可拦截 | Apboa 已具备 |

### 3.7 子 Agent / 任务委托

| 维度 | Apboa | Claude Code | OpenCode | A2A 协议 |
| --- | --- | --- | --- | --- |
| 形态 | Agent as Tool（agent_sub_agents） + A2A 配置 | Task 工具 + Subagent 定义文件（.claude/agents/*.md） | Task 工具 | A2A SDK |
| 上下文隔离 | 子 Agent 共享父 Agent 的 AgentContext | 子 Agent 独立上下文窗口，仅返回 summary | 同 Claude | 远程进程隔离 |
| 失败传播 | 异常抛回父 Agent | summary 返回 | summary 返回 | 业务定义 |
| 协议化 | 部分 A2A | 私有 | 私有 | A2A |

Apboa 已具备 A2A，是相对超前的能力。短板是“本地子 Agent 的上下文隔离”和“subagent 选择策略（让父 Agent 自主路由到合适的 subagent）”。

### 3.8 沙箱与权限

| 维度 | Apboa | Claude Code | Codex CLI | OpenCode |
| --- | --- | --- | --- | --- |
| 文件系统 | workspace 目录 + 路径校验 + ZipSlip | 全局 cwd + 显式 allow path | sandbox-exec / landlock + allow path | per-session 沙箱 |
| 命令执行 | 动态工具/Hook 直接进 JVM | bash 工具受 ask/allow/deny 控制 | seccomp/Seatbelt 系统调用层 | 类似 |
| 网络 | 未限 | 工具内自控 | sandbox 限网 | sandbox 限网 |
| 模式切换 | 无 | plan / auto-edit / accept-edits / bypass | suggest / auto-edit / full-auto | 类似 |

Apboa 在“代码执行 = JVM 内动态加载”这条路上，安全风险显著高于其他参照系。这点 RTK 已经识别，应在 `agent-runtimes` 下规划独立的沙箱 runtime。

### 3.9 用户提示快捷化

| 维度 | Apboa | Claude Code | Codex | OpenCode |
| --- | --- | --- | --- | --- |
| Slash 命令 | 无 | .claude/commands/*.md（项目级） + ~/.claude/commands（用户级） | /commands | /commands |
| Prompt 模板 | system_prompt_template | 同 + slash 命令更轻 | 同 | 同 |
| 输出风格 | 无 | output styles | 无 | 类似 |

### 3.10 观测与成本

| 维度 | Apboa | Claude Code | Codex | Hermes |
| --- | --- | --- | --- | --- |
| Token 计费 | 未显式记录 | 实时显示 + ccusage | 显示 | Span 计费 |
| Trace ID | 未统一 | 部分 | 部分 | run_id / span_id 全链路 |
| 审计事件 | 无统一模型 | hook + JSONL | JSONL | 标准事件 |
| 指标导出 | 无 | OpenTelemetry（实验） | 无 | 标准 |

## 4. 重点优化方向（按优先级）

下面把对比矩阵转化成可落地的优化项。每项给出：动机、参照、目标行为、落点（按 RTK 模块）、第一步可执行的最小切片。

### 4.1 Skill 调用机制：从 eager 注入改为 progressive disclosure

**动机**：当前每激活一个 skill_package，整包内容（skill_content + references + examples）会进入装配链路，多 skill 并存时 system prompt 急剧膨胀；且模型无法根据当前任务自主决定加载哪一个 skill 的细节。

**参照**：Claude Code Skills（SKILL.md frontmatter + 按需读取）。

**目标行为**：

1. 每个 skill_package 引入“描述层”（建议字段：`summary` 短描述 ≤ 256 字符 + `triggers` 触发关键词 / 适用场景 + `entry_files` 主入口文件清单）。Apboa 现有 `skill_package.description` 可承担短描述，缺 `triggers` 和入口文件清单两个字段。
2. 装配阶段只把所有可见 skill 的 (name, summary, triggers) 拼成索引，作为元工具 `list_skills` / `read_skill` 的语义。
3. 模型在推理时通过 `read_skill(name)` 拉取 skill 全文；执行脚本仍走原有 SkillBox 通道。
4. 兼容旧 skill：未声明 `summary` 时回退到当前 eager 注入，加 deprecation 标注。

**落点**：

- `agent-domain`：在 `SkillPackage` 值对象里增加 summary、triggers、entry_files。
- `agent-biz/biz-skill`：新增 `SkillIndexService`、`SkillReader`。
- `agent-runtimes/runtime-agentscope`：在 ToolkitFactory 增加 `list_skills` / `read_skill` 元工具。
- `agent-repo`：扩展 skill_package 字段（不影响 Apboa 表结构原则，使用新增列且为 nullable）。
- 数据库：第一阶段约束允许新增列；只增不改不删。

**最小切片**：先在新数据列就绪后选 1 个高频 skill 做 progressive 改造，比较 token 消耗。

### 4.2 用户记忆 / 项目记忆 / Agent 记忆三层 Markdown 体系

**动机**：当前“记忆”全部塞在 system_prompt_template 里，缺少“跨会话沉淀的运行态记忆”能力；用户也没有“让 Agent 记住我的某项偏好”的入口。

**参照**：Claude Code 的 CLAUDE.md（项目级 + 用户级 + Agent 级），Codex 的 AGENTS.md。

**目标行为**：

1. 引入三层 Markdown 记忆，按以下顺序在 system prompt 之后拼接，下层覆盖上层：
   - 用户级：`account_memory`（按 account_id 持久化）
   - Agent 级：`agent_definition.memory_md`（已有 system_prompt 不动；memory_md 是“运行态学习到的事实/偏好”）
   - 会话级：`chat_session.memory_md`（会话内沉淀，可跨重连恢复）
2. 提供两种写入入口：
   - 显式工具 `remember(scope, content)`，scope ∈ {user, agent, session}
   - 隐式触发：用户消息以 `#` 开头，平台询问写入哪一层（沿用 Claude Code 习惯，可作为 UX 选项）
3. 召回阶段不做向量检索，直接把三段 Markdown 拼接到 system prompt 末尾（与知识库 RAG 解耦）。
4. 提供查看 / 编辑界面，可解释，可重置。

**和现有结构的关系**：

| 现有 | 改造后 |
| --- | --- |
| `system_prompt_template` | 仍是“管理员定义”的静态人设/规则 |
| `agentscope_sessions` 中的 InMemoryMemory | 仍承担“窗口内对话状态” |
| AutoContextMemory 压缩 | 仍承担窗口压缩 |
| 知识库 / RAG | 仍承担“可大、可搜索的知识” |
| 新增三层 Markdown | 承担“跨会话、轻量、可解释的偏好与事实” |

**落点**：

- `agent-domain`：`UserMemory`、`AgentMemory`、`SessionMemory` 值对象。
- `agent-biz/biz-memory`（新增子模块）：MemoryService。
- `agent-runtimes/runtime-agentscope`：在 `ReActAgentHelper` 装配 system prompt 阶段拼接三段。
- `agent-adapter`：管理 API + 显式 `remember` 工具暴露。
- `agent-repo`：新增 `account_memory`、`agent_memory`、`session_memory` 表（不修改 Apboa 已有表，符合“第一阶段不动表结构”）。

**最小切片**：先实现 session 级，对齐 chat_session.id 一对一存储，再扩到 agent 级和 user 级。

### 4.3 用户会话记录：补齐 Run / Span / 结构化 Tool Call

**动机**：当前 `chat_message` 把工具调用作为字符串落库，重放、审计、计费、可观测都受限；`agentscope_sessions` 与 `chat_message` 双轨增加了一致性负担。

**参照**：Hermes 的 Run/Span 模型；OpenCode 的结构化 message parts；Claude Code 的 transcript JSONL；MCP 的 tool_use / tool_result 结构。

**目标行为**：

1. 新增 `agent_run` 表：每次 AGUI 触发一条记录，承载 run_id、agent_id、session_id、status、started_at、finished_at、token_in、token_out、cost、model_id、error。
2. 新增 `agent_span` 表：一次 run 内的“一步”，类型 ∈ {model_call, tool_call, sub_agent, hook, retrieval}，承载 input、output、duration、status。
3. `chat_message` 不变（保留树结构和分支能力）；新增 `chat_message.run_id` 软关联。
4. AGUI SSE 事件每帧加 `run_id` 和 `span_id` 元数据，前端可重放。
5. AgentScope MysqlSession 仍保留（不破坏 ReAct 内部状态恢复）；但平台审计、重放、计费走 Run/Span 通道。

**落点**：

- `agent-domain`：`Run`、`Span` 聚合。
- `agent-infra/infra-stream`：SSE envelope 增加元数据字段。
- `agent-infra/infra-observability`（新增子模块）：Run/Span 写入和查询 SPI。
- `agent-runtimes/runtime-agentscope`：在 ReAct 推理循环周围埋点。
- `agent-repo`：新增表（同样不动 Apboa 已有表）。

**最小切片**：先把 model_call 和 tool_call 两类 Span 落地，复盘一周后扩展。

### 4.4 工具加载：分组、并行、细粒度审批

**动机**：每请求重建 Toolkit 的代价高；`parallel=false` 限制读类工具效率；`needConfirm` 颗粒度过粗。

**参照**：Claude Code 的 plan / auto-edit / accept-edits / bypass 四档 + 每工具 ask/allow/deny + 路径 / 命令模式匹配；Codex 的三档 approval。

**目标行为**：

1. Toolkit 按 (agent_id, agent_revision) 缓存，仅在 Agent 配置 revision 变化时重建。
2. 内置工具按 read-only / write 分类，read-only 默认允许并行（先做 file/glob/grep，bash 不允许）。
3. 引入 Session 级 `tool_policy_mode` ∈ {plan, ask, auto-edit, full-auto}：
   - plan：所有写工具拦截
   - ask：写工具默认 needConfirm
   - auto-edit：白名单内写工具放行
   - full-auto：无审批（仅在受控场景启用）
4. 工具的 `needConfirm` 升级为规则：`{ require: 'never' | 'always' | { args_match: <jsonpath glob> } }`。

**落点**：

- `agent-runtimes/runtime-agentscope/ToolkitFactory`：缓存 + 并行决策。
- `agent-domain`：`ToolPolicy` 值对象。
- `agent-biz/biz-chat`（或 biz-session）：SessionPolicy 持久化到 `chat_session` 新增列。
- `agent-adapter`：审批 SSE 事件标准化（已有 ConfirmationHook 基础）。

**最小切片**：先做 Toolkit 缓存（最容易、收益最大），再做并行，再做模式。

### 4.5 MCP 治理：命名空间、分层配置、密钥隔离

**动机**：多 MCP server 同名工具会冲突；当前是平台单层配置，缺少“某用户/某项目临时挂载 MCP”的能力；密钥可能明文。

**参照**：Claude Code 三层（user / project / local）；OpenCode 多层 + 热加载。

**目标行为**：

1. 工具名落库时强制带 server prefix：`mcp__{serverCode}__{tool}`，旧数据迁移时同步加 prefix。
2. 配置层引入 scope ∈ {platform, workspace/team, account}，运行时按 (account_id, agent_id) 合并三层。
3. mcp_server.config 中的密钥字段标记加密；展示时脱敏；运行时通过 `infra-security` 解密。
4. 增加 MCP `sampling` 和 `elicitation` 协议支持（如有规划接入更高版本 MCP server）。

**落点**：

- `agent-biz/biz-mcp` + `agent-admin/admin-capability`。
- `agent-infra/infra-security`：密钥托管 SPI，先用本地 Keystore，预留 KMS 适配。
- `agent-repo`：mcp_server 增加 scope 字段（兼容默认 platform）。

### 4.6 Hook：扩展生命周期事件

**动机**：现有 Hook 偏“工具确认 + 工作空间”，缺少“在用户提交 prompt 之前/之后”、“在压缩前/后”、“在 session 结束时”等事件。

**参照**：Claude Code 八类 hook 事件。

**目标行为**：

1. 引入事件类型：sessionStart、userPromptSubmit、preToolUse、postToolUse、preCompact、postCompact、sessionEnd、subAgentSpawn、modelCall。
2. Hook 配置支持 `command`（shell）和 `askAgent`（递交一段 prompt 给当前 Agent）两种动作。shell 动作走 `runtime-sandbox` 隔离。
3. 现有 `WorkspaceValidateHook`、`WorkspaceWebsocketHook`、`ConfirmationHook` 标记为内置 preToolUse / postToolUse 实现。

**落点**：

- `agent-domain`：`HookEvent` 枚举与 `HookSpec`。
- `agent-biz/biz-hook` + `agent-runtimes/runtime-agentscope`：事件分发。
- `agent-runtimes/runtime-sandbox`（规划中）：shell hook 的执行容器。

### 4.7 子 Agent：上下文隔离 + 路由策略

**动机**：当前 sub-agent 共享父 AgentContext，子任务的中间消息会污染父对话。

**参照**：Claude Code 的 Task 工具（subagent 独立窗口，仅返回 summary）。

**目标行为**：

1. Sub-agent 调用作为一种 Span 类型（见 4.3），独立 run 子链路。
2. 父 Agent 工具描述中暴露每个 subagent 的 (name, description, when_to_use)，使父模型可路由。
3. Sub-agent 完成后，仅把结构化 result 注入父对话，不展开内部 reasoning 和工具痕迹（前端可展开查看明细）。

**落点**：`agent-runtimes/runtime-agentscope` 增加 subagent 调用包装；`agent-biz/biz-agent` 暴露路由元信息。

### 4.8 沙箱化：把高风险动作迁出 JVM

**动机**：动态 Java/Groovy 工具与 Hook 在主进程加载是当前最严重的风险点；script-security 的静态检查不足以兜底。

**参照**：Codex 的 seccomp/Seatbelt；OpenCode 的 per-session 沙箱；AgentScope Runtime Java（仓库历史归档已对比）。

**目标行为**：

1. `agent-runtimes/runtime-sandbox` 落地：默认承载 shell、python、node 脚本类工具；通过容器或 process-level 隔离。
2. 动态 Java 工具：默认禁止；保留 builtin Java 工具走主 JVM；如需在线编辑业务 Java 工具，作为 sandbox 内的 jshell 或独立 jar 子进程加载。
3. ToolPolicy 中明确每个工具的执行环境；非沙箱外部能力默认走主进程。
4. 与 4.4 的 SessionPolicy 联动。

**落点**：`agent-runtimes/runtime-sandbox`、`agent-runtimes/runtime-spi`（执行环境抽象）、`agent-infra/infra-capability`（能力声明）。

### 4.9 上下文压缩：可解释、可控、可重放

**动机**：AutoContextMemory 是黑箱，用户无法看到“被压缩了什么”。

**参照**：Claude Code 的 /compact 显式触发 + summary 可见；qwen-code 的 checkpoint。

**目标行为**：

1. 在 4.3 的 Run/Span 基础上，增加 `compaction` 类型 Span：保存压缩前消息列表、压缩后 summary、模型、prompt。
2. 暴露用户级 `/compact` 命令（与 4.10 配合）。
3. AutoContextMemory 触发时强制写一次 compaction Span。

**落点**：`agent-runtimes/runtime-agentscope`、`agent-infra/infra-observability`。

### 4.10 Slash 命令：用户侧可编排的提示模板

**动机**：用户每次都要重写相似 prompt（如“总结这段会话”、“切换到深度调查模式”），缺少快捷入口。

**参照**：Claude Code `.claude/commands/*.md`，OpenCode `/commands`。

**目标行为**：

1. 引入用户级 / Agent 级 / 工作空间级三档命令模板（Markdown 文件 / 数据库行均可，Apboa 倾向数据库以便管理）。
2. 前端 Chat 输入框 `/` 触发联想；后端把命令内容渲染为完整 prompt 注入。
3. 与 4.2 记忆体系一脉：命令 = 可复用的 prompt 片段，记忆 = 持久化的事实/偏好。

**落点**：`agent-biz/biz-chat`（命令解析）+ `agent-platform-ui`（前端联想框）。

### 4.11 观测：Token / 成本 / 模型实时反馈

**动机**：当前用户感受不到“这次对话花了多少 token / 多少钱 / 用了哪个模型”。

**参照**：Claude Code 状态栏 + ccusage；OpenCode footer；Codex token 显示。

**目标行为**：

1. 每个 model_call Span 记录 token_in / token_out / model / cost。
2. SSE 事件帧带 cumulative usage。
3. 前端在对话气泡尾部显示。
4. 后台聚合到 dashboard。

**落点**：`agent-infra/infra-provider`（统一 usage 抽取）、`agent-infra/infra-observability`、`agent-platform-ui`。

### 4.12 检查点 / 快照（中长期）

**动机**：当前无法“回到某一轮重新尝试”。

**参照**：OpenCode per-turn 快照；Aider git 集成。

**目标行为**：

1. 每个 user-turn 结束时，对工作空间做轻量 git 内部仓库提交（仅本地），SHA 写入 Span。
2. 前端可在某条消息上点“rewind to here”，平台执行：恢复 FS、截断 chat_message 路径、重置 AgentScope session。
3. 仅在工作空间启用代码执行的 Agent 上启用，避免常态成本。

**落点**：`agent-biz/biz-workspace`（已有）+ `agent-infra/infra-orchestration`。

## 5. 非用户提及但影响明显的几个点

### 5.1 Provider 抽象 vs 内部模型网关

OpenCode 用 AI SDK 作为 provider 抽象层，加新 provider 几乎零代码。Apboa 的 ChatModelFactory 当前是 if-else 形式的工厂，每加一个 provider 要写适配。建议在 `agent-infra/infra-provider` 内引入“能力声明 + 适配器登记”的结构，并预留一个“内部模型网关”adapter，把所有 model 调用收敛到一处出网。这与 RTK 第 12 节方向一致。

### 5.2 流式 envelope 标准化

各开源项目的 SSE 事件正在收敛（OpenAI Responses 事件、Anthropic streaming、Hermes envelope）。Apboa 的 AGUI SSE 是 AgentScope 衍生格式，建议参考 Hermes 仓库历史归档结论，把 envelope 收敛为 `{run_id, span_id, type, payload}` 四字段，type 枚举受控。这点对 4.3 是前置条件。

### 5.3 工具结果截断与流式

当前工具结果落到 chat_message 是整体存储；当工具产生大输出（如 grep 命中 1 万行）时，会撑爆上下文。Claude Code 内置工具会做结果截断 + 提示“查看更多用 read_file 指定 offset”。Apboa 内置 `search_replace_file` 等工具应统一引入“截断策略 + 偏移读取”模式。

### 5.4 Agent 配置版本化与灰度

`agent_definition.version` 字段已存在，但缺少版本快照表。一旦 Agent 上线后 prompt 调整，旧会话的“当时定义”不可考。建议引入 `agent_definition_revision` 历史表，每次保存一份不可变 snapshot；run 表关联 revision_id。这同时是审计、灰度、回滚的基础。

### 5.5 评测闭环

OpenCode、Cline、Aider 都在引入“agent eval harness”（基准任务集 + 自动判分）。当前 Apboa 没有 Agent 维度的回归基准。建议先为最常用的 Agent 类型建一个小型评测集（10-20 任务），每次 Agent 配置变更前跑一次，作为发布前 gate。这一点和 RTK 12.2 “Agent 发布流程”可以合并落地。

### 5.6 AGENTS.md 接管

仓库根 `AGENTS.md` 是给 AI 工具看的工程约定（不是 Apboa 平台的产物），但同时也提示了一种行为：很多开源 Agent 把项目根的 AGENTS.md / CLAUDE.md / .cursorrules 自动并入 system prompt。Apboa 自己作为“面向用户提供 Agent 的平台”，未来在 Agent 工作空间内若用户上传了 AGENTS.md，应自动检测并并入；这与 4.2 的项目级记忆是同一件事，只是来源不同。

## 6. 优先级与排期建议

按“收益 / 成本”和“阻塞关系”排序：

| 序 | 项 | 收益 | 成本 | 阻塞依赖 |
| --- | --- | --- | --- | --- |
| P0 | 4.4 Toolkit 缓存 | 高 | 低 | 无 |
| P0 | 4.5 MCP 工具命名空间 | 高 | 低 | 无 |
| P0 | 4.3 Run/Span 落库（先 model_call + tool_call） | 高 | 中 | 无（但是 4.9/4.11 的前置） |
| P1 | 4.2 三层 Markdown 记忆（先 session 级） | 高 | 中 | 无 |
| P1 | 4.1 Skills progressive disclosure | 高 | 中 | 4.4 Toolkit 缓存先就绪更稳 |
| P1 | 4.4 SessionPolicy + 模式切换 | 中 | 中 | 4.3 |
| P1 | 4.11 Token / 成本 | 中 | 低 | 4.3 |
| P2 | 4.6 Hook 生命周期扩展 | 中 | 中 | 4.3 |
| P2 | 4.10 Slash 命令 | 中 | 低 | 无 |
| P2 | 4.7 子 Agent 上下文隔离 | 中 | 中 | 4.3 |
| P3 | 4.9 Compaction Span 暴露 | 中 | 低 | 4.3 |
| P3 | 4.8 沙箱化高风险动作 | 高 | 高 | runtime-sandbox 立项 |
| P3 | 4.12 检查点快照 | 中 | 中 | 4.3 + 工作空间 git 化 |
| P3 | 5.4 Agent 版本快照 | 中 | 中 | 无 |
| P3 | 5.5 评测闭环 | 中 | 中 | 5.4 |

## 7. 不建议直接吸收的点

- **完全替换树形 chat_message 为线性 transcript**：Apboa 的树结构反而是优势，不应为了对齐 Claude Code 而退化。
- **直接迁移 Claude Code 的文件型 SKILL.md / CLAUDE.md 到平台**：开源工具是单机文件型生态，企业平台应保留数据库存储 + 前端编辑，文件 import/export 作为可选通道。
- **过度依赖 sandbox-exec / seccomp**：Apboa 部署在内部容器中，可优先用容器级隔离 + JVM 安全管理替代方案，再决定是否引入操作系统级沙箱。
- **AI SDK 作为唯一 provider 抽象**：内部已规划走模型网关，AI SDK 只能做参考。
- **完全用 markdown 文件目录管理 prompt / 命令 / 记忆**：保留以数据库为权威源，文件作为 import/export 形态。

## 8. 与 RTK 模块边界的映射汇总

| 优化项 | 主落点模块 | 新增子模块（建议） |
| --- | --- | --- |
| 4.1 Skills progressive disclosure | `agent-biz/biz-skill`、`agent-runtimes/runtime-agentscope` | 无 |
| 4.2 三层记忆 | `agent-biz` | `biz-memory` |
| 4.3 Run/Span | `agent-infra` | `infra-observability` |
| 4.4 Toolkit + Policy | `agent-runtimes/runtime-agentscope`、`agent-biz/biz-chat` | 无 |
| 4.5 MCP 治理 | `agent-biz/biz-mcp`、`agent-admin/admin-capability`、`agent-infra/infra-security` | 无 |
| 4.6 Hook 事件扩展 | `agent-biz/biz-hook` | 无 |
| 4.7 子 Agent | `agent-biz/biz-agent`、`agent-runtimes/runtime-agentscope` | 无 |
| 4.8 沙箱化 | `agent-runtimes` | `runtime-sandbox` |
| 4.9 Compaction Span | `agent-runtimes/runtime-agentscope`、`agent-infra/infra-observability` | 无 |
| 4.10 Slash 命令 | `agent-biz/biz-chat`、UI | 无 |
| 4.11 观测 | `agent-infra/infra-provider`、`infra-observability` | 无 |
| 4.12 快照 | `agent-biz/biz-workspace`、`agent-infra/infra-orchestration` | 无 |
| 5.1 Provider 抽象 | `agent-infra/infra-provider` | 无 |
| 5.2 SSE envelope | `agent-infra/infra-stream` | 无 |
| 5.3 工具结果截断 | `agent-runtimes/runtime-agentscope` | 无 |
| 5.4 Agent revision | `agent-biz/biz-agent`、`agent-repo` | 无 |
| 5.5 评测闭环 | `agent-biz/biz-agent`、独立工程 | 可考虑 `source/agent-platform-eval/` |

## 9. 后续动作

下一步建议按 P0 三项启动正式 Spec：

- Toolkit 缓存与并行（小切片，先收 toolkit-cache）
- MCP 工具命名空间收敛（含 mcp_tool 表迁移）
- Run/Span 基础落库（model_call + tool_call 两类）

P1 项中 4.2（session 级记忆）和 4.1（progressive skills）建议作为 v2 阶段的两条主线特性，分别立 Spec。

其余项作为路线储备，等待 P0/P1 落地稳定后再启动。

## 10. 参考资料

- 项目内：
  - `RTK.md`
  - `docs/apboa/01-系统详细设计报告.md`
  - `docs/v1/02-模块迁移方案.md`
  - `docs/v1/03-Apboa源码迁移映射关系.md`
  - `docs/v2/调研/历史归档文档可吸收内容分析.md`
  - `docs/历史归档文档/调研/AgentScope-Java与AgentScope-Runtime-Java区别.md`
  - `docs/历史归档文档/调研/HermesAgent替代AgentScope执行侧方案对比.md`
- 外部（公开资料 / 各项目仓库 README 与文档约定，供后续按需深入）：
  - Anthropic Claude Code 公开文档（Skills、Hooks、Subagents、CLAUDE.md）
  - OpenAI Codex CLI 仓库 README 与 AGENTS.md 约定
  - sst/opencode 仓库 README 与协议文档
  - QwenLM/qwen-code 仓库 README
  - MCP 协议规范（modelcontextprotocol.io）
  - A2A 协议规范

注：本调研依据现有仓库内材料和公开实现的通用模式进行总结；具体外部项目的 API 细节如需作为实现依据，应在落地 Spec 阶段再次校对最新版本。
