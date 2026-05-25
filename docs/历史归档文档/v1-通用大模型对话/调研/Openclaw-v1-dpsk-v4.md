# OpenClaw 设计调研

> 调研版本：v1
> 调研模型：DeepSeek-v4-pro
> 调研日期：2026-05-14
> 调研对象：OpenClaw（原 Clawdbot，Peter Steinberger 创建）
> 对照范围：通用大模型对话 01/02/03/04 文档
> 备注：本调研独立完成，未参考本地其他调研报告。

## 1. 背景与定位

OpenClaw（GitHub 347,000+ stars，截至 2026.4）是 Peter Steinberger 创建的 MIT 开源个人 AI 助手平台。技术栈为 TypeScript + Node.js 22+，pnpm workspace 单仓库结构。

其核心哲学与 Hermes Agent 形成鲜明对比：

| 维度 | Hermes Agent | OpenClaw |
|------|-------------|----------|
| 核心定位 | 自改进的 Agent 运行时 | Gateway 优先的编排控制平面 |
| 设计哲学 | Agent 本身就是产品，Gateway 是管道 | Gateway 本身就是产品，LLM 是可替换部件 |
| 架构起点 | Agent Loop（run_agent.py） | WebSocket Gateway（server.ts） |
| 学习能力 | 自动创建/改进 Skills | 被动记忆，跨会话不学习 |
| 多 Agent | 子任务委派（单 Agent 深度） | 原生多 Agent 编排（广度） |
| Agent 运行时 | 自研 ~14k 行 Python | 嵌入 Pi Agent Core（第三方） |
| 通道集成 | 6+ 核心平台 | 25+ 平台 |
| 插件生态 | 成长中（~200 skills） | 庞大（ClawHub 社区市场） |

**对当前项目的核心启示**：OpenClaw 的 Gateway-first 架构代表了一种与我们项目更接近的设计思路——将 AI 能力组织为可编排、可路由的服务平面，而非一个深度学习的单体 Agent。如果项目后续演化为多 Agent 平台，OpenClaw 的架构模式比 Hermes 更容易映射到我们的 Java/Spring 技术栈。

## 2. Gateway-first 架构：OpenClaw 最大的设计决策

### 2.1 核心洞察

> "OpenClaw is not a framework. It is a gateway — a single runtime that sits between your AI model and the outside world."
> "The hard problem in personal AI agents is not the agent loop itself, but everything around it."

OpenClaw 的设计起点不是 Agent Loop，而是 **Gateway**——一个长期运行的 WebSocket 服务器（`src/gateway/server.ts`，绑定 `127.0.0.1:18789`）。它作为整个系统的单点控制平面，承担以下职责：

- 连接管理与消息路由
- 访问控制与会话管理
- 健康监控与心跳调度
- 跨通道事件协调

**Agent 运行时反而是被嵌入的第三方组件**（`@mariozechner/pi-agent-core`），OpenClaw 自己构建的是 orchestration、integration、persistence 层。这是一个重要的架构教训：**控制平面比推理引擎更难做对**。

### 2.2 Hub-and-Spoke 拓扑

```
外部通道（WhatsApp/Telegram/Discord/...）
         │
         ▼
    ┌─────────────┐
    │   Gateway    │  ← WebSocket 控制平面（127.0.0.1:18789）
    │ (server.ts)  │
    └──────┬──────┘
           │
    ┌──────┼──────┬──────────────┐
    ▼      ▼      ▼              ▼
  Agent  Cron   Heartbeat    Plugin Loader
  Runtime Scheduler Loop     (extensions/)
```

五个边界明确分离：
1. **外部通道** → 消息进入系统
2. **Gateway** → 归一化、路由、状态管理（本地进程）
3. **设备节点** → 手机/Mac 上的 companion apps
4. **模型 Provider** → 外部 LLM 服务
5. **工具可访问的外部状态** → 浏览器、文件系统、API

### 2.3 对我们项目的启示

OpenClaw 的 Gateway-first 模式恰好对应了我们项目的 Maven 多模块设计意图：

| OpenClaw | 当前项目对应 | 映射关系 |
|----------|------------|---------|
| Gateway (server.ts) | `agent-web` 模块（Controller + Filter + Spring Boot） | HTTP/SSE 入口层 |
| Agent Runtime (Pi) | `agent-core` 模块（LLM 适配 + 会话管理） | 业务核心层 |
| Plugin/Channel Adapters | 后续平台 Agent 工具模块 | 扩展/集成层 |
| Session Store | PostgreSQL 会话/消息表 | 持久化层 |
| Heartbeat + Cron | 暂不需要（V1 纯对话） | 后续自动化层 |

**关键教训**：

1. **不要把 Agent Loop 和控制平面耦合**。当前项目 `agent-core` 应该保持纯粹的 LLM 对话逻辑，不包含 HTTP 路由、鉴权、协议适配等 Web 层面关注。
2. **控制平面应是单点**。如果后续引入多 Agent 或多通道，应有一个统一的路由/编排层协调各个 Agent 实例，而非各 Agent 自行管理生命周期。
3. **嵌入而非重建 Agent 运行时**。OpenClaw 没有自己实现 LLM 交互循环，而是嵌入 Pi。我们使用 `agentscope-java` 已经体现了同样思路——利用已验证的 SDK 处理模型调用细节，自己聚焦在平台能力上。

## 3. 四层架构模型

OpenClaw 的架构可分解为四个清晰层次，每层各司其职：

| 层级 | 职责 | 关键模式 |
|------|------|---------|
| **Gateway** | 连接管理、路由、鉴权 | 单进程多路复用（WebSocket） |
| **Execution** | 任务排序、并发控制 | Per-session 串行队列（Lane Queue） |
| **Integration** | 平台归一化 | Channel Adapter（适配器模式） |
| **Intelligence** | Agent 行为、知识、主动性 | Skills + Memory + Heartbeat |

### 3.1 Gateway 层

单进程 WebSocket 服务器，多路复用 HTTP API、Control UI、WebSocket 控制消息于同一端口。所有客户端（CLI、mobile nodes、web UI）通过 WebSocket 连接，而非各自独立的 HTTP 端点。

**对我们的启示**：当前 V1 使用纯 HTTP + SSE 是正确选择（Web 端不需要 WebSocket 持久连接）。如果后续需要 mobile 端推送、实时通知或 Agent 状态推送，WebSocket 方案比轮询更合理。建议在 `02-技术方案` 中记录："后续多端接入时，Gateway 层应支持 WebSocket 协议，统一会话路由。"

### 3.2 Execution 层——Lane Queue（最重要模式）

OpenClaw 的 Lane Queue 是本次调研中最有价值的工程模式。

**设计规则**：

- 每个 session 拥有独立的队列（lane）
- 同一 lane 中的任务**串行执行**（`main` 默认 lane）
- 额外 lane（`cron`、`subagent`）允许后台任务**并行**运行而不阻塞主会话
- Session Key 结构为 `workspace:channel:userId`，不是简单的 user ID，防止同一用户在不同通道间的上下文泄露
- 并行是可选的，默认串行
- 背压（backpressure）内建于队列层，而非分散在各个 handler 中

**与当前项目的对比**：

当前项目 `04-详细设计` 中已经设计了会话锁机制——同一 session 内的消息串行处理，并发请求返回 `409`。这与 OpenClaw 的 Lane Queue 理念一致，但 OpenClaw 的实现更加系统化：

| 特性 | 当前项目（V1） | OpenClaw Lane Queue |
|------|--------------|-------------------|
| 串行化范围 | 单 session，Semaphore tryAcquire | 单 session，dedicated lane |
| 并发请求处理 | 返回 409（优雅拒绝） | 入队等待（排队而非拒绝） |
| 跨通道隔离 | 不适用（仅 Web） | `workspace:channel:userId` 复合 key |
| 后台任务 | 无 | 独立 lane 不阻塞主对话 |
| 背压控制 | 队列层未明确 | 内建于队列系统 |

**建议**：

- V1 的 409 策略对纯对话场景足够，且更简单。
- 如果后续引入后台任务（如定时摘要、Agent 自主巡检）、Agent 工具调用 或排队消息持久化，应升级为 Lane Queue 模式。
- 关键设计点：**同一 session 的所有用户触发消息必须串行**；**后台任务使用独立 lane，不阻塞用户交互**。

### 3.3 Integration 层——Channel Adapter

OpenClaw 通过 channel adapter 接口（`ChannelMessagingAdapter`、`ChannelGatewayAdapter`、`ChannelAuthAdapter`）将 25+ 平台的消息格式、媒体处理、鉴权、限流策略归一化为内部标准协议。

**对我们项目的启示**：V1 只有 Web 端，不需要 channel adapter。但如果后续支持移动端、API 调用、Webhook 等多入口，建议采用同样的适配器模式：定义 `DialogEntryAdapter` 接口，各入口（REST、WebSocket、mobile push）各自实现，核心对话逻辑不感知入口差异。

### 3.4 Intelligence 层

参考第 6 节（Skills + Memory + Heartbeat）详细分析。

## 4. Session 路由：结构化身份键

### 4.1 复合 Session Key

```
// OpenClaw session key 结构
main              → 直接对话
dm:channel:id     → 私聊
group:channel:id  → 群聊
agent::subagent:  → 子代理
```

**关键设计**：Session key 包含 channel 信息。同一用户通过 Discord 和 WhatsApp 发送的消息路由到**不同 session**，防止跨通道上下文泄露。

### 4.2 对我们项目的 Session 设计影响

当前项目使用 `userCode` 路由会话，所有会话来自同一 Web 端，不存在跨通道混淆风险。这一模式在 V1 完全合理。

**但有一个重要的预留**：如果后续支持 API 调用和 Web 端共存，同一个 `userCode` 可能同时通过两种入口与助手交互。此时需要考虑：
- API 调用创建的会话是否在 Web 端可见？
- 同一会话能否同时被两个入口操作？
- 入口标识是否需要加入 session key？

**建议**：在当前 `session` 表中预留 `source_type` 字段（`WEB`、`API`、`MOBILE` 等枚举），V1 全部设为 `WEB`，后续扩展时用于 session 隔离策略判断。

## 5. 插件与扩展架构

### 5.1 发现与热加载

OpenClaw 的插件系统通过 `extensions/` 目录自动发现：

1. 插件加载器扫描 workspace 包中的 `package.json` 的 `openclaw.extensions` 字段
2. 对声明的 schema 进行校验
3. 配置存在时热加载（无需重启 Gateway）

四种插件类型注册到不同 slot：

| 插件类型 | 注册目标 | 职责 | 典型实例 |
|---------|---------|------|---------|
| Channel Plugin | Channel Router | 鉴权、入站解析、出站格式化 | MS Teams, Matrix, WeChat |
| Tool Plugin | Tool Registry | 扩展 Agent 可调用的工具 | Browser, Canvas, GitHub |
| Memory Plugin | Memory Search | 记忆索引和检索后端 | memory-core (SQLite), memory-lancedb |
| Provider Plugin | Model Providers | 模型 provider 适配（OAuth、fallback） | Claude, GPT, Gemini |

### 5.2 核心与扩展的边界

OpenClaw 的 `AGENTS.md` 明确规定了核心与扩展的代码边界：

- 核心代码不包含任何扩展特定逻辑
- 扩展仅通过 `openclaw/plugin-sdk/*` 和 manifest metadata 进入核心
- 扩展的 prod 代码不能引用核心 `src/**`、`src/plugin-sdk-internal/**`、其他扩展的 `src/**`
- 新 seam 必须向后兼容、文档化、版本化

### 5.3 对我们项目的启示

当前 V1 的 Maven 多模块结构（`agent-api` / `agent-core` / `agent-web`）已经体现模块边界思想。后续引入插件/工具系统时，OpenClaw 的四个设计原则值得直接引用：

1. **Manifest-first**：插件通过声明式元数据（而非代码 import）注册到核心
2. **Targeted runtime loaders**：按需加载，不预装所有插件到 prompt
3. **No hidden contract bypasses**：插件与核心的通信路径必须显式文档化
4. **Ownership boundaries**：扩展特定的行为修复在扩展模块中完成，只在多扩展共同需要时向核心添加通用 seam

**建议**：在 `02-技术方案` 中记录："后续工具/插件系统采用 manifest-first + registry pattern，插件通过声明式元数据注册，核心通过 contract interface 消费，禁止反向依赖和隐式通信路径。"

## 6. 记忆系统：SQLite-backed 结构化存储

### 6.1 架构特点

OpenClaw 的记忆系统与 Hermes 的 Markdown 文件方案不同：

| 特性 | Hermes | OpenClaw |
|------|--------|---------|
| 存储 | MEMORY.md（Markdown 文件）+ SQLite FTS5（会话搜索） | SQLite（`memory-core` 插件） |
| 检索 | 关键词 FTS5 + LLM 摘要 | 嵌入式向量相似度搜索（`sqlite-vec`） |
| 写入 | Agent 通过 memory tool 管理 | Agent 通过 memory tool 写入 |
| 结构 | 非结构化的 Markdown 文件 | 结构化、可查询的存储 |
| 容量 | MEMORY.md 限制 2,200 字符 | 无硬性限制 |

### 6.2 Active Memory 机制（重点参考）

OpenClaw v2026.4.12 引入的 **Active Memory** 是最值得参考的记忆检索模式：

- **概念**：在主回复生成之前，运行一个**阻塞式记忆子代理**，使用受限工具集（`memory_search` + `memory_get`）检索相关记忆，将结果注入主回复的 context。
- **双门控模型**：
  1. Gate 1（配置门控）：插件启用 + 当前 agent ID 在配置目标列表中
  2. Gate 2（运行时门控）：仅对交互式持久聊天会话生效（排除 headless 运行、Heartbeat、后台任务、子代理内部调用）
- **失败回退**：如果记忆子代理失败或工具不可用，跳过记忆检索，主回复不受影响（graceful degradation）
- **工具白名单**：记忆子代理**只能调用记忆工具**，不能调用 shell、文件、浏览器等 Agent 工具

### 6.3 对我们项目记忆设计的启示

当前项目 V1 的"历史消息上下文组装"是最基础的记忆形式——当前会话的消息历史全部注入 prompt。这与 OpenClaw/Hermes 的"跨会话记忆检索"不在同一维度。

**当项目进入 Agent 平台阶段时**，OpenClaw 的 Active Memory 模式提供了一个精确的实现蓝图：

```
用户消息到达
  │
  ├─→ [Gate 1: 配置检查] → 未启用 → 直接进入主回复
  │
  ├─→ [Gate 2: 运行时类型检查] → 不是交互式会话 → 直接进入主回复
  │
  └─→ [阻塞式记忆子代理]
        ├─ 仅可使用 memory_search / memory_get
        ├─ 检索相关记忆摘要
        ├─ 注入主 Agent 的 context
        └─ 失败 → 静默跳过，主回复不受影响
```

**关键设计决策**：

1. **阻塞式而非异步**：记忆检索必须在主回复前完成，不能"先回复再补充记忆"
2. **独立子代理**：记忆检索使用受限工具集的子代理，不会干扰主 Agent 的工具调用链
3. **Graceful degradation**：记忆检索失败不影响核心对话功能
4. **不持久化到主对话**：记忆检索的中间过程不写入会话历史，只有检索结果被注入 context

**建议**：在 `02-技术方案` 的后续演进部分记录 Active Memory 模式作为长期记忆检索的实现蓝图。V1 不需要实现，但当前的数据模型设计应为后续的记忆检索预留接口：消息表外挂 `message_embedding` 表或独立的 `memory_fact` 表。

## 7. 子代理委派系统

### 7.1 深度与权限模型

OpenClaw 的子代理系统与 Hermes 有相似的安全约束，但差异化在工具策略层：

```
Max Spawn Depth（可配置）：
  Depth 0: agent::main → 始终可 spawn
  Depth 1: agent::subagent: → maxSpawnDepth >= 2 时可 spawn（orchestrator）
  Depth 2: agent::subagent::subagent: → 永远不可 spawn（leaf worker）
```

**默认行为**：
- `maxSpawnDepth: 1`（子代理不能再 spawn 子代理）
- 设置为 `2` 时启用 orchestrator 模式：main → orchestrator → workers

**工具权限层级**：

| Depth | 角色 | 工具权限 |
|-------|------|---------|
| 0 | Main | 全部工具 |
| 1 (orchestrator, maxDepth≥2) | 编排者 | `sessions_spawn` + `subagents` + `sessions_list` + `sessions_history` |
| 1 (leaf, maxDepth=1) | 叶子 | 无 session 工具 |
| 2 | 叶子 worker | 无 session 工具（`sessions_spawn` 始终拒绝） |

### 7.2 工具策略 Pipeline

```
Sub-agent 工具策略 = Parent profile → Tool policy → Sub-agent restriction layer
```

1. 先继承父代理的 profile 和 tool-policy
2. 再叠加子代理限制层（移除 session tools、system tools）
3. `coding` 和 `full` 配置的 agent 默认暴露 `sessions_spawn`
4. `messaging` 配置的 agent 需显式 `tools.alsoAllow` 来启用委派

### 7.3 Lane 隔离

子代理运行在全局 `subagent` lane 上（独立于 `main` lane），因此子代理的执行不会阻塞主会话的用户交互。完成后通过 announce 步骤回报结果。

### 7.4 对当前项目的启示

V1 不涉及子代理。但 OpenClaw 的委派模型有几个设计决策值得在后续平台 Agent 编排中直接参考：

1. **深度硬限制**：max 2 层（可配置但不超过 3），防止递归爆炸
2. **角色与权限写入 session metadata**：恢复或扁平化的 session key 不会意外获得编排权限
3. **工具策略分层叠加**：继承 + 限制，而非全局白名单
4. **Lane 隔离**：子代理不阻塞父代理的用户交互
5. **默认不可委派**：`messaging` 角色的 agent 需要显式授权才能 spawn 子代理——最小权限原则

**建议**：在 `02-技术方案` 后续演进中记录："Agent 编排采用深度限制 + 角色权限 + Lane 隔离模式。子代理默认不可 spawn，需显式授权。"

## 8. 心跳与主动性

### 8.1 Heartbeat Loop

OpenClaw 有一个独特的 **Heartbeat 系统**（`src/infra/heartbeat-runner.ts`），每 30 分钟自动触发：

- Agent 读取 `HEARTBEAT.md` 中的检查清单
- 与当前 context 比对，判断是否有待处理事项
- 如果无事可做 → 返回特殊 token `HEARTBEAT_OK`，Gateway 静默丢弃（用户看不到）
- 如果有事可做 → Agent 执行任务并投递到消息平台

### 8.2 六大输入类型

OpenClaw 将输入统一建模为六种类型（不仅仅是用户消息）：

1. **Messages** — 用户消息（来自各通道）
2. **Heartbeats** — 定时唤醒检查
3. **Cron Jobs** — 定时任务
4. **Hooks** — 事件驱动的触发
5. **Webhooks** — 外部系统触发
6. **Agent-to-Agent** — 代理间消息

### 8.3 对我们项目的启示

> "Give your agent a pulse. Your agent should exist when nobody's talking to it."

这是 OpenClaw 区别于纯对话系统的核心理念之一。当前 V1 完全是被动响应的对话应用——没有用户消息就没有任何动作。这在 V1 是合理的，但当项目演进为 Agent 平台时，主动性能大幅提升产品价值。

**V1 不需要实现心跳**。但建议在 `01-需求` 的非首版范围中增加："定时自主任务（如摘要生成、数据巡检、提醒推送）"作为后续方向。

**后续实现参考**：
- 通过 Spring `@Scheduled` 或 Quartz 实现定时触发
- Agent 读取预定义的检查清单（类比 `HEARTBEAT.md`）
- 无事可做时静默（不消耗回复给用户）
- 有事可做时正常执行并投递（WebSocket push、通知）

## 9. 安全架构

### 9.1 五层防御深度

OpenClaw 的安全模型值得深入研究：

| 层级 | 机制 | 职责 |
|------|------|------|
| L1 | 入站访问控制 | DM 配对 + allowlists + 通道级鉴权 |
| L2 | 设备身份与加密握手 | CLI/UI 的 challenge-response + 密钥签名 |
| L3 | 工具策略与执行安全 | Profiles（coding/full/messaging）+ ask-mode |
| L4 | Docker 沙箱 | Main session 在 host 上，untrusted sessions 在临时容器中 |
| L5 | 审计与日志 | 命令执行纪录、tool call 追踪 |

### 9.2 沙箱策略的关键细节

- **Main session**（operator 自己）：运行在 host 上，全权限
- **dm/group sessions**（untrusted）：运行在**临时 Docker 容器**中
- 非 main session 的工具白名单：`bash`、`read`、`write`
- 非 main session 的工具黑名单：`browser`、`canvas`、`nodes`

### 9.3 对我们的启示

V1 的安全需求简单（token 非空校验），不涉及沙箱。但 OpenClaw 的**分层安全管理**概念应在早期技术方案中建立：

- **当前 V1 安全层**：token 校验 → `userCode` 隔离 → 会话/消息按用户过滤
- **后续 Agent 阶段安全层**：工具策略（profile-based tool permissions）→ 沙箱隔离（Docker/虚拟线程）→ 命令审批（dangerous commands detection）

**建议**：在 `02-技术方案` 中增加"安全设计"小节，记录当前 V1 的安全边界和后续扩展方向，建立分层的安全意识。

## 10. Agent 运行时抽象

### 10.1 多种运行时类型

OpenClaw 支持三种 Agent 运行时架构：

| 类型 | 描述 | 实例 |
|------|------|------|
| **Embedded** | 运行在 OpenClaw 的 Agent Loop 内 | Pi（内置）、Codex（插件） |
| **CLI Backend** | 运行本地 CLI 进程 | `anthropic/claude` with `claude` runtime |
| **ACP Harness** | 外部 Agent 通过 ACP 协议交互 | Claude Code, Gemini CLI, OpenCode, Cursor |

**运行时选择优先级**（per-turn 解析）：

```
1. Model-scoped runtime policy（per-model 配置）
2. Provider-scoped runtime policy（per-provider 配置）
3. Plugin runtime auto-claim（插件声明的支持范围）
4. PI compatibility runtime（默认 fallback）
```

**关键约束**：
- 显式 provider/model runtime 配置**fail closed**（解析失败 → 报错，不静默回退）
- 不正确的 `auto` 回退比明确的错误更难排查
- 全 session 和全 agent 的 runtime pin 被废弃，改为 per-turn 解析

### 10.2 对我们的启示

当前项目使用 `agentscope-java` 作为唯一的 LLM 运行时，V1 不需要切换。但当后续支持多个 LLM provider 或不同的交互模式时，OpenClaw 的运行时抽象提供了清晰的参考：

```
// V1 当前状态（单体）
agentscope-java → 所有模型调用

// 后续演进方向（多运行时）
LlmRuntime interface
  ├── AgentscopeRuntime（OpenAI-compatible providers）
  ├── AnthropicDirectRuntime（Anthropic Messages API）
  └── LocalModelRuntime（Ollama / vLLM）
```

**建议**：在 `02-技术方案` 中记录运行时抽象方向，当前 `LlmProviderAdapter` 接口设计（参见 Hermes 调研第 4 节）已经为此打下基础。

## 11. 上下文组装与 Prompt 构建

### 11.1 Composite Prompting

OpenClaw 的 prompt-builder 从多个来源组装 context：

```
System Prompt（会话内不变）:
  ├── SOUL.md          → Agent 人格、价值观、通信风格
  ├── USER.md          → 用户画像、偏好
  ├── AGENTS.md        → 工作目录指令
  ├── TOOLS.md         → 能力定义
  └── Skills/          → 按需加载的 SKILL.md

Ephemeral（每次对话注入）:
  ├── 相关记忆（SQLite 语义搜索 → 仅匹配项注入，非全量历史）
  ├── 当前触发器（用户消息或 Heartbeat 检查清单）
  └── 工具 schema 定义（运行时自动生成）
```

### 11.2 上下文压缩

当对话历史超过 context window 时，OpenClaw 自动触发压缩：

- 对最旧的轮次运行摘要步骤
- 将多条消息压缩为单个摘要条目
- 保留语义含义同时大幅减少 token 使用

这与 Hermes 的压缩策略相似（保留最后 N 条 + 压缩中间），但触发条件是 context window 大小而非轮次数。

### 11.3 对我们项目的启示

当前 V1 的 context 组装就是"当前会话的所有历史消息"，简单直接。但随着对话积累，token 消耗会线性增长。OpenClaw 的 composite prompting 模式为后续提供了演进参考：

- **分层组装**：固定层（system prompt） + 检索层（memory search） + 对话层（current history）
- **按需注入**：记忆只注入匹配当前 query 的条目，而非全量历史
- **压缩触发**：基于 token 估算而非轮次数

**建议**：在 `02-技术方案` 中记录："会话 context 组装采用分层模式——固定 prompt + 动态记忆检索 + 当前对话历史。V1 仅实现对话历史层，固定 prompt 层和记忆检索层作为后续扩展。"

## 12. Canvas（A2UI）：Agent-to-UI 交互

OpenClaw 的 Canvas 是一个有趣的能力——Agent 可以生成带有特殊属性的 HTML（例如 `data-action="click"`），客户端将其渲染为可交互 UI。用户点击后，工具调用发送回 Agent。

这超出了当前项目的范围，但其思想值得关注：**Agent 不仅能回复文本，还能生成结构化的交互界面**。后续平台能力中，"Agent 生成表单/图表/操作面板"是一个有价值的方向。

## 13. OpenClaw vs Hermes：两种哲学

### 13.1 根本分歧

> "OpenClaw treats an agent as a system to be orchestrated. Hermes treats an agent as a mind to be developed."

这是理解两个项目所有设计差异的关键：

| 决策维度 | OpenClaw（编排） | Hermes（认知） |
|---------|----------------|--------------|
| Agent 如何变得更好？ | 社区贡献新 Skills / 配置优化 | 自动从经验中创建和改进 Skills |
| 能力广度 vs 深度 | 广度优先（25+ 通道，ClawHub 市场） | 深度优先（6 核心通道，自改进回路） |
| 可预测性 | 高（行为由配置文件显式决定） | 中—高（自动改进引入变化） |
| Operator 介入 | 高（配置文件显式编辑） | 低—中（Agent 自动管理 memory/skills） |
| 多 Agent 编排 | 原生支持（Gateway 级路由） | 子任务委派（单 Agent 深度） |
| 适用场景 | 多通道团队协作、企业治理 | 个人深度助手、长期工作流自动化 |

### 13.2 对我们项目定位的启示

当前项目 V1 是 Web 端通用大模型对话——既不需要 OpenClaw 的 25+ 通道编排，也不需要 Hermes 的自改进学习回路。但项目名称（`htam-agent-platform`）暗示其长期愿景是一个 Agent 平台。

**平台化路径选择**：

- **如果走 OpenClaw 路线**：Gateway-first，聚焦多 Agent 编排、通道集成、工具策略管理、团队权限。适合企业级/多租户场景。
- **如果走 Hermes 路线**：Agent-first，聚焦单个 Agent 的深度能力、自改进技能、长期记忆、上下文压缩。适合个人/单用户深度助手场景。
- **混合路径**：以 OpenClaw 式的 Gateway 编排层作为平台骨架，以 Hermes 式的自改进能力作为 Agent 差异化能力。Gormes 项目（Hermes 的 Go 移植）正在尝试这种混合。

**建议**：在技术方案中明确当前项目定位。如果目标是"企业级 Agent 平台"，OpenClaw 式的编排架构更合适；如果目标是"深度个人 Agent"，Hermes 式的认知架构更有价值。当前 V1 的 Web 端通用对话是两者的共同起点。

## 14. 建议更新到现有文档

### 14.1 `02-通用大模型对话技术方案.md`

**新增"架构设计原则"小节**：

- **Core 层与 Web 层解耦**：`agent-core` 不依赖 WebFlux，核心逻辑与入口协议分离
- **单点会话路由**：所有同 session 的消息操作通过统一入口串行化
- **分层 context 组装**：固定 prompt + 动态检索 + 对话历史（V1 仅实现最后一层）

**新增"后续演进参考"小节**：

- Lane Queue 模式（per-session 串行队列 + 独立后台 lane）
- Active Memory 记忆检索模式（阻塞式子代理 + 优雅降级）
- Session Key 复合结构（来源标识预留）
- 工具/插件 manifest-first 注册模式
- 子代理深度限制与权限分层
- Agent 运行时抽象（multi-runtime）

### 14.2 `04-通用大模型对话详细设计.md`

**补充消息表字段预留**：

- `source_type` 字段（WEB/API/MOBILE），V1 全部设为 WEB
- 独立 `memory_fact` 表或 `message_embedding` 表作为后续记忆检索的扩展点

**补充会话锁语义**：

- 明确当前 `Semaphore` 策略对应的是"单 lane 串行"模式
- 记录后续升级为 Lane Queue 的条件（引入后台任务、Agent 工具调用时）

### 14.3 `01-通用大模型对话需求.md` 非首版范围

新增：

- 定时自主任务（Heartbeat 式定期检查）
- 多入口接入（API / mobile / WebSocket）
- Agent 工具策略与权限分层
- 跨会话长时记忆检索
- 多 Agent 编排与子代理委派

## 15. 综合判断

### 15.1 OpenClaw 对 V1 的直接价值

OpenClaw 的 Gateway-first 架构对当前 V1 的**即时指导意义**不如 Hermes（因为 V1 不需要多通道编排），但其几个工程模式可以直接引入：

| 模式 | 适用程度 | 原因 |
|------|---------|------|
| Lane Queue（串行队列） | 高 | 当前已有类似设计（会话锁），可将其系统化 |
| Session Key 复合结构 | 高 | 当前核心字段（userCode + sessionId）结构清晰，预留 source 字段即可 |
| Core 与适配层分离 | 高 | 当前 Maven 多模块已体现，需在文档中明确约束 |
| Manifest-first 插件注册 | 中 | V1 无插件，但可以作为后续工具系统的设计约束 |
| Active Memory 检索模式 | 中 | 后续记忆检索的精确实现蓝图 |
| 子代理深度限制 | 中 | 后续 Agent 编排的安全基线 |
| Agent 运行时抽象 | 低 | V1 单运行时（agentscope-java），后续有价值 |
| Canvas A2UI | 低 | 超出当前范围 |

### 15.2 OpenClaw 的核心教训

1. **控制平面比推理引擎更难做对**。OpenClaw 没有自己写 Agent Loop，而是嵌入 Pi。它把精力花在 routing、queuing、channel normalization、memory persistence 上——这些才是平台型 Agent 产品的核心竞争力。
2. **Gateway 是产品，LLM 是可替换部件**。这种架构分离使得 OpenClaw 能支持 35+ 模型而无需修改核心逻辑。我们的 `agent-core` / `agent-web` 分离已经体现了同样思路。
3. **串行执行是 Agent 系统的安全基础**。Lane Queue 模式——per-session 串行 + 独立后台 lane——是用最简单的并发模型解决最复杂的状态一致性问题。
4. **给 Agent 一个脉搏**。主动性（Heartbeat）是对话产品和 Agent 产品的分水岭。V1 不需要，但应作为关键里程碑写入路线图。
5. **记忆检索不应阻塞主回复**。Active Memory 的优雅降级策略——记忆检索失败时静默跳过——保证了核心功能的可靠性。

### 15.3 与 Hermes 调研的互补性

Hermes 调研聚焦于"单 Agent 如何深度学习"（记忆、技能、压缩、Provider 抽象），OpenClaw 调研聚焦于"多 Agent 如何被编排"（Gateway、Lane Queue、插件、子代理权限、通道适配）。两者结合覆盖了 Agent 平台设计的完整维度：

```
          自改进深度
              ▲
              │  Hermes 擅长
              │  (Memory layers, Skill creation, Learning loop)
              │
              │    当前 V1
              │    (Web 对话)
              │
              │                  OpenClaw 擅长
              │                  (Gateway, Lane Queue, Plugins,
              │                   Multi-agent, Channel adapters)
              └─────────────────────────────────────► 编排广度
```

### 15.4 对后续版本最优先级引入的 OpenClaw 模式

1. **Lane Queue** — Agent 工具/后台任务引入时，升级会话锁为 per-session lane queue
2. **Active Memory** — 长期记忆检索时，采用阻塞式子代理 + graceful degradation
3. **Manifest-first Plugins** — 工具/技能系统时，采用声明式注册 + registry pattern
4. **Subagent Depth Limits** — Agent 编排时，硬限制委派深度 ≤ 2

## 16. 参考来源

- OpenClaw GitHub：https://github.com/openclaw/openclaw
- OpenClaw 官网：https://openclaw.ai/
- OpenClaw 架构文档：https://openclawlab.com/en/docs/start/architecture/
- OpenClaw 架构深入分析：https://slashdev.io/-inside-openclaw-a-technical-breakdown-of-the-open-source-ai-agent-platform
- OpenClaw 源码解析：https://starkslab.com/notes/i-read-openclaws-source-code
- OpenClaw 架构教训：https://blog.agentailor.com/posts/openclaw-architecture-lessons-for-agent-builders
- OpenClaw 架构深入：https://s1dd4rth.github.io/openclaw-mastery/openclaw-unpacked.html
- OpenClaw 架构洞察：https://navant.github.io/posts/openclaw-architecture-and-insights/
- OpenClaw Agent 工作原理：https://learnopenclaw.org/architecture.html
- OpenClaw Active Memory：https://docs.openclaw.ai/concepts/active-memory
- OpenClaw 子代理文档：https://docs.openclaw.ai/tools/subagents
- OpenClaw Agent 运行时：https://docs.openclaw.ai/concepts/agent-runtimes
- OpenClaw AGENTS.md：https://github.com/openclaw/openclaw/blob/748d6dc75e6aa4df68de8d83a4d3f1284479b758/AGENTS.md
- OpenClaw vs Hermes 对比：
  - https://advenboost.com/hermes-agent-vs-openclaw/
  - https://jiaweing.com/blog/hermes-agent-openclaw-killer
  - https://www.tryopenclaw.ai/blog/openclaw-vs-hermes-agent/
  - https://typescript.news/articles/2026-03-31-hermes-agent-vs-openclaw-ai-agent-comparison
  - https://sanjayshankar.me/hermesagent-vs-openclaw-comparison/
  - https://blog.dotw.me/en/not-just-another-ai-agent-hermes-vs-openclaw/
  - https://lushbinary.com/blog/hermes-agent-vs-openclaw-updated-comparison-may-2026/
  - https://open-claw.me/blog/hermes-agent-vs-openclaw-comparison
  - https://digitalbydefault.ai/blog/hermes-agent-vs-openclaw-2026
  - https://powerdrill.ai/blog/hermes-agent-vs-openclaw-which-ai-agent-should-you-choose-for-real-work
