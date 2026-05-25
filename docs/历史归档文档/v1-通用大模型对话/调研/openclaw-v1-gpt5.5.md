# OpenClaw 设计调研

> 调研版本：v1
> 调研模型：GPT-5.5
> 调研日期：2026-05-14
> 调研对象：OpenClaw
> 对照范围：通用大模型对话 01/02/03/04 文档
> 资料基准：优先采用 `docs.openclaw.ai` 与 GitHub README 中的当前设计说明；`openclawdoc.com` 作为较旧或营销向文档补充；安全部分参考 2026-05-11 的 MATRA OpenClaw case study 摘要。

## 1. 背景与定位

OpenClaw 当前更接近一个 local-first / self-hosted 的个人 AI assistant gateway，而不是单纯聊天应用。它通过一个长驻 Gateway 进程连接多种消息渠道、agent runtime、工具、插件、浏览器、节点设备和会话存储。

从最新公开文档看，OpenClaw 的核心设计重心已经从“多渠道聊天机器人”演进为：

- 一个本地优先的 Gateway control plane。
- 一个嵌入式 agent runtime。
- 可配置的模型 provider 与 model ref。
- 工具、skills、plugins 三层能力体系。
- 会话、记忆、上下文压缩和 context engine。
- 安全边界、工具策略、sandbox、审批和审计。
- 多 agent、delegate、node、browser 等扩展运行面。

当前项目第一版是 Web 端通用大模型对话，目标是基础会话、流式回复、thinking 展示、历史续聊和用户隔离。OpenClaw 的完整能力远超 V1 范围，因此本报告重点判断哪些理念可以借鉴，哪些应暂缓。

## 2. OpenClaw 最新设计要点

### 2.1 Gateway 作为长驻控制平面

OpenClaw 的 Gateway 是单机长驻进程，统一负责 messaging surfaces、WebSocket API、节点连接、事件推送、健康状态和控制面。客户端、CLI、Web UI、节点设备都通过 Gateway 协议连接。

可借鉴点：

- 后端不只是 HTTP API，也可以逐步演进为“会话与运行时控制平面”。
- 对于流式对话、异步任务、工具调用、多端接入，统一事件协议比零散接口更容易治理。
- Side-effect 请求需要 idempotency key 和短期去重缓存，避免重试导致重复发送或重复执行。

对当前项目的适用性：

- V1 不建议改成 WebSocket Gateway，现有 REST + SSE 足够。
- 可以在 `04-详细设计` 中补“后续如引入多端/工具/任务，可演进为 Gateway 控制平面”的说明。
- 现在可以先借鉴 idempotency key 思路，用于未来发送消息、重试和工具执行。

### 2.2 Agent Runtime 与 Workspace 注入

OpenClaw 使用 workspace 作为 agent 工作目录，并注入一组用户可编辑文件，例如 `AGENTS.md`、`SOUL.md`、`TOOLS.md`、`USER.md` 等。它把 persona、操作规则、工具使用约定、用户偏好和长期说明分层管理。

可借鉴点：

- 系统提示词不应只是一段硬编码字符串。
- 可以把系统提示词拆成多层：平台默认规则、应用规则、用户偏好、会话上下文。
- 这些规则需要版本化、可审计、可回滚。

对当前项目的适用性：

- V1 已经把 `system-prompt` 配置化，这是正确方向。
- 建议后续在 `02/04` 预留 `PromptProfile` 或 `AgentProfile` 概念：
  - `systemPrompt`
  - `behaviorRules`
  - `safetyRules`
  - `locale/timezone`
  - `toolPolicy`（后续）
- 不建议 V1 引入 workspace 文件体系，会增加权限、存储和编辑复杂度。

### 2.3 Tools / Skills / Plugins 三层能力体系

OpenClaw 最新文档把能力拆成三层：

- Tool：模型可调用的 typed function，例如 `exec`、`browser`、`web_search`、`message`。
- Skill：Markdown 形式的操作指导，告诉 agent 何时、如何使用工具。
- Plugin：能力打包单元，可注册 channel、model provider、tool、skill、speech、media、web search 等。

可借鉴点：

- Tool 是执行能力，Skill 是行为指导，Plugin 是交付和扩展单元。三者边界清晰。
- 工具应有结构化 schema、allow/deny 策略、profile 和 provider-specific restriction。
- 当显式 allowlist 没有任何可调用工具时，系统应 fail closed，而不是让模型假装工具存在。

对当前项目的适用性：

- V1 不做工具调用，不应引入完整 tools runtime。
- 但 `02-技术方案` 可以补后续演进：
  - `ToolDescriptor`
  - `ToolPolicy`
  - `ToolExecutionService`
  - `SkillInstruction`
  - `PluginRegistry`
- 对当前对话系统最直接的借鉴是“显式能力边界”：没有启用工具时，模型上下文中不应出现工具能力暗示。

### 2.4 Context Engine 与 Compaction

OpenClaw 把上下文构建抽象为 context engine，生命周期包括：

- ingest：新消息进入时可索引或存储。
- assemble：模型调用前组装上下文。
- compact：上下文溢出或手动压缩时摘要旧消息。
- afterTurn：回合结束后持久化、索引或后台压缩。

OpenClaw 的 compaction 会保留最近消息，压缩旧消息，并且在工具调用场景中保持 tool call 与 tool result 成对，不让摘要切断工具交互。

可借鉴点：

- 上下文组装应成为独立服务，而不是散落在发送消息接口里。
- 压缩、检索、摘要、token 预算都应该归属于上下文引擎。
- `summary_message_id` 这类字段需要明确什么时候写、怎么用、是否影响历史回放。

对当前项目的适用性：

- 建议 `04` 后续补 `ContextAssembler` / `ContextEngine` 包职责：
  - 读取 completed 消息。
  - 排除 thinking。
  - 估算 token。
  - 按预算截断或摘要。
  - 返回模型消息列表。
- V1 可以只实现 legacy/pass-through 逻辑。
- 后续再加入 auto-compaction 和 overflow retry。

### 2.5 Command Queue：按 Session 串行，跨 Session 并行

OpenClaw 的 command queue 使用 lane-aware FIFO：同一 session lane 只允许一个 active run，跨 session 可以并行；同时还有全局 lane 限制总体并发。入站消息支持不同 queue mode，例如 `collect`、`followup`、`steer`、`interrupt`。

可借鉴点：

- “同一会话串行、不同会话并行”应成为明确不变量。
- 排队策略不只有一种：收集、后续、插入当前 run、打断当前 run，适合不同产品形态。
- 队列应有 debounce、cap、overflow policy。

对当前项目的适用性：

- 我们已在 `04` 中补了服务端 `tryAcquire` 和前端排队。
- 建议进一步借鉴：
  - 排队上限 `cap`。
  - 溢出策略：拒绝新消息或摘要丢弃消息。
  - 可观测日志：排队等待超过阈值时记录。
- V1 仍保持简单 FIFO，不做 `steer` 或 `interrupt`。

### 2.6 Streaming：区分 token delta、block streaming、preview streaming

OpenClaw 最新设计区分两类流式：

- block streaming：把助手输出按块发送到渠道。
- preview streaming：在 Telegram/Discord/Slack 等渠道上编辑临时预览消息。

它还明确了 chunking 算法：最小/最大字符数、段落/换行/句子/空白优先级、代码块不拆断、coalescing 合并小片段、按渠道限制长度。

可借鉴点：

- 流式体验不等于逐 token 展示；对 UI 可以逐 token，对消息渠道更适合分块和预览。
- chunking 需要明确边界策略，尤其是 Markdown、代码块、长文本。
- 工具进度也可以进入流式预览，让多步骤任务不显得卡住。

对当前项目的适用性：

- 当前 Web V1 使用 SSE delta 是合理的。
- 建议在 `04` 的前端 SSE 解析里补后续优化方向：
  - 前端可对 delta 做 coalescing，降低高频渲染。
  - 长回复渲染时保护 Markdown/code fence 完整性。
  - 后续工具调用时增加 tool-progress 事件。
- V1 不需要实现 block streaming。

### 2.7 Retry Policy：只重试当前步骤，避免重复副作用

OpenClaw 的 retry 目标是：

- 按单个 HTTP 请求重试，不按完整多步骤流程重试。
- 保持顺序。
- 避免重复非幂等操作。

可借鉴点：

- 模型调用、消息投递、工具执行的重试语义应分开。
- 对非幂等动作，应依赖 idempotency key 或禁止自动重试。
- 长时间 `Retry-After` 不应阻塞整个运行时，可转为快速失败或 failover。

对当前项目的适用性：

- V1 当前不做自动重试是保守正确的。
- 可在后续设计中补：
  - LLM transient error 是否允许“本轮重试一次”。
  - 用户消息写入后，模型调用失败不重复写用户消息。
  - 未来工具执行必须带 idempotency key。

### 2.8 安全设计：Access Control Before Intelligence

OpenClaw 最新安全文档强调 personal-assistant trust model，不把单个 Gateway 当作敌对多租户边界。它建议按 trust boundary 拆分 gateway、凭证、OS 用户或主机。

同时它提供：

- `security audit`。
- DM pairing 和 allowlist。
- context visibility。
- tool allow/deny。
- sandbox。
- dangerous flags 检查。
- browser SSRF policy。
- logs/transcripts redaction and retention。

可借鉴点：

- 先做访问控制和工具策略，再相信模型判断。
- 多用户共享一个 tool-enabled agent 风险极高，需要强隔离。
- 调试输出、thinking、trace、tool args 在公开渠道中可能泄漏敏感信息。

对当前项目的适用性：

- 当前第一版 `userCode = appCode` 是 mock 隔离，不是真实安全边界。
- 建议 `01/02/04` 中更明确地标注：
  - V1 鉴权只适用于内部验证。
  - 生产接入前必须替换真实鉴权。
  - thinking 默认只给当前用户展示，不进入公开渠道或日志。
  - traceId、errorMessage、日志要避免保存完整 token 和敏感模型输出。

### 2.9 Sandboxing 与 Tool Policy

OpenClaw 支持工具在 sandbox backend 中运行，Gateway 本身仍在 host 上。sandbox 可以按 agent、session 或 shared scope 创建。它还明确 sandbox 不是完美边界，但能显著降低误操作和 prompt injection 的影响范围。

可借鉴点：

- Sandbox、tool policy、elevated mode 是三个不同概念。
- 默认应该最小权限，需要明确逃逸路径。
- 对浏览器、文件、shell、网络等工具要按 agent/session 配置权限。

对当前项目的适用性：

- V1 不做工具，无需 sandbox。
- 但如果未来接入工具，必须先设计：
  - 默认 deny。
  - 按应用/用户/agent 授权。
  - 审批与审计。
  - 沙箱执行。

### 2.10 Browser Tool：独立 profile 与 SSRF 防护

OpenClaw 把浏览器控制做成 bundled plugin，支持独立 `openclaw` profile、用户真实浏览器 profile、远程 CDP、node browser proxy。它强调浏览器是 agent-only surface，并有 SSRF policy、profile allowlist、loopback control service 等约束。

可借鉴点：

- 浏览器自动化应是独立运行面，不应直接复用用户浏览器。
- 对内网访问、远程 CDP、登录态复用要有明确安全策略。
- 浏览器能力应作为插件/工具，可禁用、可替换。

对当前项目的适用性：

- V1 不引入浏览器自动化。
- 后续如做 Agent 工具，应把 browser 作为高风险工具，不应默认启用。

### 2.11 Delegate Architecture：不要冒充用户

OpenClaw 的 delegate 模式强调 agent 应有自己的身份，并通过组织身份系统获得明确委托权限。它区分 Read-only + Draft、Send on Behalf、Proactive 三个能力等级，并要求先定义 hard blocks、工具限制、sandbox 和审计。

可借鉴点：

- 企业场景中，Agent 应该有清晰身份，不应伪装成人。
- 能力分级比一次性全授权更可控。
- “草稿优先、人工确认、再逐步自动化”是合理路径。

对当前项目的适用性：

- V1 只是对话，不涉及外部动作。
- 平台级 Agent 编排后续可以参考 delegate 分级：
  - Tier 1：只读 + 草稿。
  - Tier 2：代表发送，但需要明确授权。
  - Tier 3：定时/主动执行，需要硬性禁止项和审计。

## 3. 建议借鉴到当前项目的内容

### 3.1 立即适合吸收

#### 3.1.1 会话队列语义增强

建议在 `04-详细设计` 中补充：

- 同一 session 串行是后端不变量。
- 不同 session 可以并行。
- 排队队列有 `cap`。
- 溢出策略第一版为拒绝新消息并提示。
- 排队等待超过阈值记录日志。

理由：这与当前前端排队和服务端锁设计完全一致，只是补清边界。

#### 3.1.2 ContextAssembler 抽象

建议在 `04-详细设计` 中把上下文组装从“发送消息接口步骤”提升为独立职责：

```text
llm/context
  ContextAssembler
  ContextBudgetEstimator
  ContextMessageMapper
```

V1 行为：

- 只取 `completed` 消息。
- 排除 thinking。
- 当前用户消息放在最后。
- 暂不压缩，只预留 token 估算。

理由：这为后续 compaction、检索增强、摘要边界做准备。

#### 3.1.3 错误与重试语义

建议在 `02/04` 补：

- 第一版不自动重试完整对话流程。
- 用户消息写入后，模型失败只更新助手失败态。
- 后续如做重试，只重试模型调用步骤，不重复写用户消息。
- 所有未来副作用工具必须支持 idempotency key。

理由：OpenClaw 的“只重试当前步骤”原则可以直接避免重复副作用。

#### 3.1.4 安全声明更明确

建议在 `01/02/04` 补：

- V1 的 `X-Token` 非空校验不是生产鉴权。
- `appCode` mock `userCode` 只适合验证期。
- thinking、trace、verbose、errorMessage 不应暴露给其他用户或公共渠道。
- 日志默认脱敏 token、apiKey、请求头和模型原始异常。

理由：当前系统虽然不做工具，但会保存对话和 thinking，仍有数据泄漏风险。

### 3.2 适合作为后续演进

#### 3.2.1 Gateway 控制平面

当系统从 Web 单入口扩展到多端、多渠道、定时任务、工具调用时，可以参考 OpenClaw Gateway：

- 统一会话运行时。
- WebSocket 事件协议。
- 运行状态、健康状态、任务事件。
- idempotency key。
- 多端订阅。

V1 不建议引入，避免重构范围过大。

#### 3.2.2 Tool / Skill / Plugin 体系

后续 Agent 平台能力可以参考三层边界：

- Tool：结构化可执行函数。
- Skill：提示词级操作指南。
- Plugin：能力交付包。

V1 不实现，但可以写入非首版范围和未来架构路线。

#### 3.2.3 Context Engine 插件化

后续可以把上下文策略插件化：

- legacy：当前按历史消息组装。
- summary：摘要压缩。
- retrieval：检索增强。
- hybrid：摘要 + 最近消息 + 召回片段。

V1 只需要先把 `ContextAssembler` 边界立住。

#### 3.2.4 停止生成、打断与 steering

OpenClaw 的 queue mode 包含 `interrupt` 和 `steer` 类能力。当前项目可以后续演进：

- 停止生成：用户中断当前 assistant 回复。
- followup：当前回复结束后继续。
- collect：把多条排队消息合并成一次后续请求。

V1 当前 FIFO 自动发送足够。

### 3.3 不建议进入当前 V1

- 多渠道接入。
- 浏览器自动化。
- shell / 文件 / 设备工具。
- sandbox 执行。
- plugin marketplace。
- delegate 组织身份体系。
- proactive cron 任务。
- 多 agent 路由和子 agent 委派。
- Canvas / A2UI / voice / mobile node。

这些能力属于平台级 Agent，不属于通用对话第一版。

## 4. 建议更新到现有文档的位置

### 4.1 `01-通用大模型对话需求.md`

建议补充到非首版范围或后续方向：

- 停止生成。
- 多端/多渠道接入。
- 工具调用。
- 插件化能力。
- 上下文压缩。
- 长期记忆。
- 真实账号与生产鉴权。
- 定时任务和主动执行。

不建议把 OpenClaw 的 tool、browser、delegate 写进 V1 目标。

### 4.2 `02-通用大模型对话技术方案.md`

建议补“OpenClaw 可借鉴设计”小节：

- Gateway 控制平面作为后续演进，不进入 V1。
- 当前 REST + SSE 保持，但未来事件协议可统一。
- 引入 `ContextAssembler` 概念。
- 队列策略补充 cap、overflow、日志。
- Tool / Skill / Plugin 三层作为平台后续方向。
- 安全策略强调 access control before intelligence。

### 4.3 `04-通用大模型对话详细设计.md`

建议补充更具体的设计项：

- `llm/context/ContextAssembler`。
- `ContextBudgetEstimator` 预留。
- 排队 cap 和溢出错误码，例如 `QUEUE_LIMIT_EXCEEDED`。
- 日志脱敏规则。
- SSE delta 前端 coalescing 可作为优化项。
- 未来工具执行必须有 idempotency key、tool policy、audit log。

### 4.4 `03-原型设计/chat-prototype.html`

当前不建议更新。OpenClaw 的多渠道、工具、浏览器、delegate 都不属于 V1 原型。

如后续要增加“停止生成”，再单独更新原型按钮和状态。

## 5. 与 Hermes Agent 调研的差异

Hermes Agent 与 OpenClaw 都强调 agent runtime、工具、记忆和长期运行，但侧重点不同：

- Hermes 更偏自主 Agent runtime、skills、session lineage、检查点和长期任务。
- OpenClaw 更偏 local-first Gateway、多渠道、工具策略、sandbox、安全 hardening 和个人助手运行面。

对当前项目而言：

- Hermes 更适合借鉴模型 provider、session lineage、长期记忆。
- OpenClaw 更适合借鉴 Gateway 控制平面、队列语义、context engine、工具权限、安全策略和 streaming/chunking。

## 6. 综合结论

OpenClaw 的最新设计理念可以概括为：

- Local-first gateway owns runtime。
- Session 是并发与隔离的核心单位。
- 工具能力必须被结构化、授权、审计和沙箱化。
- 上下文组装是独立引擎，不应绑死在接口里。
- 流式输出要按渠道和 UI 形态做 chunking/coalescing。
- 生产安全要先做访问控制，再让模型行动。

当前项目最值得马上吸收的是：

1. `ContextAssembler` 抽象。
2. 排队 cap、overflow 和日志。
3. 重试只针对当前步骤，避免重复副作用。
4. 安全声明和日志脱敏。
5. 后续 Gateway / Tool / Skill / Plugin 路线说明。

不建议现在引入完整 OpenClaw 风格 Gateway、工具系统、浏览器自动化或多渠道接入。第一版仍应保持 Web 对话 V1 的交付边界。

## 7. 参考来源

- OpenClaw GitHub README: https://github.com/openclaw/openclaw
- OpenClaw current docs index: https://docs.openclaw.ai/llms.txt
- OpenClaw Gateway architecture: https://docs.openclaw.ai/concepts/architecture
- OpenClaw Agent runtime: https://docs.openclaw.ai/concepts/agent
- OpenClaw Session management: https://docs.openclaw.ai/concepts/session
- OpenClaw Command queue: https://docs.openclaw.ai/concepts/queue
- OpenClaw Streaming and chunking: https://docs.openclaw.ai/concepts/streaming
- OpenClaw Retry policy: https://docs.openclaw.ai/concepts/retry
- OpenClaw Context engine: https://docs.openclaw.ai/concepts/context-engine
- OpenClaw Compaction: https://docs.openclaw.ai/concepts/compaction
- OpenClaw Tools and plugins: https://docs.openclaw.ai/tools
- OpenClaw Browser tool: https://docs.openclaw.ai/tools/browser
- OpenClaw Security: https://docs.openclaw.ai/gateway/security
- OpenClaw Sandboxing: https://docs.openclaw.ai/gateway/sandboxing
- OpenClaw Delegate architecture: https://docs.openclaw.ai/concepts/delegate-architecture
- OpenClaw older docs overview: https://openclawdoc.com/docs/getting-started/what-is-openclaw/
- MATRA OpenClaw case study: https://arxiv.org/abs/2605.10763
