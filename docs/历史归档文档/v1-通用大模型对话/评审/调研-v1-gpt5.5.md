# 通用大模型对话调研综合评审

> 评审版本：v1
> 评审模型：GPT-5.5
> 评审日期：2026-05-14
> 输入材料：`docs/v1-通用大模型对话/调研` 下 6 篇 Hermes Agent / OpenClaw 调研报告
> 对照范围：`01-需求`、`02-技术方案`、`03-原型设计`、`04-详细设计`

## 1. 评审结论

6 篇调研报告整体形成了较稳定的共识：

- Hermes Agent 更适合借鉴 **模型 provider 抽象、分层记忆、session lineage、上下文压缩、技能渐进式加载、子代理约束**。
- OpenClaw 更适合借鉴 **Gateway 控制平面、Lane Queue、Context Engine、工具/技能/插件三层能力、安全边界、sandbox、幂等与重试语义、streaming chunking**。
- 当前 V1 是 Web 端通用对话，不应引入完整 Agent runtime、工具执行、多渠道 Gateway、子代理、闭环学习或沙箱系统。
- 当前 01/02/04 已经吸收了一部分评审建议，例如 Spring Boot 4.0.5 基线、WebFlux 阻塞约束、agentscope 事件拆分、AbortController/doOnCancel、排队临时状态、失败态、CORS/限流/Flyway/日志等。
- 仍有若干低成本、高收益的设计点值得补入文档，尤其是 `ContextAssembler`、`LlmProviderAdapter`、Prompt 稳定性、队列 cap/overflow、日志脱敏、鉴权边界说明、上下文压缩语义。

综合判断：**应继续保持 V1 的 Web 对话交付边界，但把 Hermes / OpenClaw 的工程原则写入“设计约束”和“后续演进”章节，避免后续 Agent 平台化时返工。**

## 2. 六篇调研报告共识

### 2.1 Hermes Agent 共识

三篇 Hermes 调研一致认为，Hermes 的核心价值不在 UI 或表结构，而在长期运行 Agent 的内层机制：

- 多 provider / 多 API mode 通过适配器归一化。
- system prompt、memory、skills 要分层，并控制 token 注入成本。
- 会话内 prompt 应保持稳定，避免破坏 prompt cache。
- 记忆与技能应渐进式加载，不一次性注入全部内容。
- 上下文压缩应在请求前 preflight 阶段触发，而不是响应后补救。
- `summary_message_id` 应明确表示摘要边界，而不是仅作为空字段预留。
- 子代理委派需要深度限制、工具白名单、独立预算和结果摘要。
- 自动技能创建、自我修补、Cron、RL、文件 checkpoint 不适合当前 V1。

### 2.2 OpenClaw 共识

三篇 OpenClaw 调研一致认为，OpenClaw 的价值在 Gateway-first 与运行时治理：

- Gateway 是运行时控制平面，统一处理多入口、事件、运行状态和任务路由。
- 同一 session 串行、跨 session 并行是并发控制核心。
- 后续引入后台任务或工具时，应从简单锁升级为 Lane Queue。
- Context Engine 应独立于发送消息接口，负责上下文组装、预算估算、压缩和检索。
- 工具、技能、插件应分层：Tool 是执行能力，Skill 是行为指导，Plugin 是交付单元。
- 工具系统必须有 allow/deny、sandbox、审批、审计和日志脱敏。
- 重试只应重试当前步骤，不能重复执行完整流程或重复写入副作用。
- 多渠道、浏览器自动化、Canvas、语音、移动节点、delegate 体系不应进入 V1。

## 3. 当前文档已覆盖的内容

### 3.1 已覆盖且方向正确

当前文档已经覆盖以下调研建议：

- Spring Boot 版本以 demo 已验证的 `4.0.5` 起步，并锁定 agentscope 1.0.12。
- WebFlux 下 JDBC、锁等待、agentscope 调用不能阻塞 Netty 事件循环。
- 同一会话通过前端排队 + 服务端锁兜底，避免并发打乱上下文。
- 排队消息为前端临时状态，刷新关闭后丢失，可移除，有数量上限建议。
- 流式失败、SSE 解析失败、自动发送失败均进入失败态，不静默丢弃。
- 前端使用 `AbortController`，后端通过 `doOnCancel()` 处理客户端断开。
- agentscope stream 事件逐 `ContentBlock` 拆分，区分 `ThinkingBlock` 与 `TextBlock`。
- thinking 不进入下一轮上下文。
- `system-prompt` 已配置化。
- parts 化存储与 JSONL 事件流的区别已经说明。
- CORS、限流、关键日志、Flyway 命名规范已有基础说明。
- `agent_session_event` 被明确列为非首版范围。

### 3.2 已覆盖但还不够精确

以下内容已有雏形，但还需要更明确：

- `summary_message_id` 目前只写“预留上下文压缩”，未定义其精确语义。
- 会话串行目前是 `Semaphore tryAcquire + 409`，但未明确“同 session 串行、不同 session 并行”的不变量。
- 排队数量上限有建议，但未给出错误码和前端提示策略。
- `system-prompt` 已配置化，但未写“会话内 Prompt 不变性”。
- LLM 配置仍是 `baseUrl/apiKey/modelName`，缺少 provider/runtime 抽象边界。
- 日志已有关键事件说明，但缺少脱敏规则。
- V1 鉴权是 mock 隔离，但文档尚未足够醒目标注“不构成生产鉴权”。

## 4. 建议采纳项

### 4.1 高优先级：建议补入 V1 设计文档

#### 4.1.1 增加 `LlmProviderAdapter`

调研来源：Hermes 多 provider 抽象、OpenClaw model/runtime policy。

建议在 `02` 和 `04` 中补充：

```text
llm/provider
  LlmProviderAdapter
  OpenAiCompatibleAdapter
  LlmStreamEventMapper
```

V1 只实现 OpenAI-compatible / agentscope-java 适配，但配置预留：

- `provider`
- `modelName`
- `apiMode`
- `baseUrl`
- `apiKey`

价值：

- 隔离 agentscope-java 与业务服务。
- 后续接 DeepSeek、Anthropic、Ollama、vLLM 时减少核心改动。
- 为不同 provider 的 thinking 字段、stream 格式、fallback 行为预留边界。

建议落点：

- `02-技术方案`：模型配置和后端设计章节。
- `04-详细设计`：`llm` 包职责和接口草案。

#### 4.1.2 增加 `ContextAssembler`

调研来源：OpenClaw Context Engine、Hermes preflight compaction。

建议把上下文组装从发送接口步骤中抽出为独立职责：

```text
llm/context
  ContextAssembler
  ContextBudgetEstimator
  ContextMessageMapper
```

V1 行为：

- 只读取 `completed` 消息。
- 只取 user/assistant 的 text part。
- 排除 thinking。
- 当前用户消息放在最后。
- 暂不压缩，但预留 token 估算。

价值：

- 后续可自然加入摘要、检索、token 预算、上下文裁剪。
- 避免发送消息接口承担过多职责。
- 与 `summary_message_id`、`prompt_tokens`、`completion_tokens` 字段形成闭环。

建议落点：

- `04-详细设计` 第 6.2 上下文组装。
- `10-开发任务拆分` 增加 ContextAssembler 实现任务。

#### 4.1.3 明确 `summary_message_id` 语义

调研来源：Hermes/OpenClaw compaction。

建议将字段说明改为：

> `summary_message_id` 指向一条摘要消息。该消息之前的历史消息在上下文组装时由摘要替代；上下文 = 摘要消息 + 摘要之后的完整消息。V1 不生成摘要，但字段语义按该规则预留。

价值：

- 避免后续把它误用为普通“最近摘要 ID”。
- 为 preflight compaction 提前定义数据边界。
- 与历史回放和上下文组装分离：历史仍可完整展示，模型输入可压缩。

建议落点：

- `02` 的 `agent_session` 字段说明。
- `04` 的 `agent_session` 字段规则。

#### 4.1.4 增加 Prompt 稳定性约束

调研来源：Hermes prompt cache / frozen snapshot。

建议补充：

- 会话内系统级 prompt 保持稳定。
- `systemPrompt`、用户偏好、长期记忆、项目上下文等系统级材料的变更，在新会话或新上下文窗口中生效。
- 当前 V1 仅有静态 `system-prompt`，先写为设计原则。

价值：

- 为后续 prompt cache、长期记忆和用户偏好注入留出正确约束。
- 避免在每轮动态拼接系统 prompt 时破坏缓存或引入不可解释行为。

建议落点：

- `02` 新增“架构设计原则”。
- `04` 第 6.2 上下文组装。

#### 4.1.5 明确队列 cap / overflow / 错误码

调研来源：OpenClaw Command Queue / Lane Queue。

当前文档只写“建议设置排队数量上限”。建议明确：

- V1 前端排队上限默认 10 条。
- 超出上限不入队，提示用户等待当前回复完成。
- 错误码或前端状态命名：`QUEUE_LIMIT_EXCEEDED`。
- 服务端 `409 SESSION_BUSY` 表示服务端兜底锁失败，不等同于前端排队上限。

价值：

- 让前端实现、测试和 UX 预期一致。
- 防止用户连续发送大量消息导致内存和体验问题。

建议落点：

- `01` 页面体验 / 消息能力。
- `04` 前端状态、发送与排队流程、测试计划。

#### 4.1.6 加强安全边界与日志脱敏

调研来源：OpenClaw access control before intelligence、Hermes prompt injection scan。

建议补充：

- V1 的 `X-Token` 非空校验只适合验证期，不构成生产鉴权。
- `appCode` mock `userCode` 只适合内部验证。
- 生产接入前必须替换为真实身份、权限和租户隔离。
- 日志默认脱敏：
  - `X-Token`
  - `apiKey`
  - Authorization
  - 请求头
  - 模型原始异常中的敏感内容
- thinking、trace、errorMessage 不应跨用户展示或进入公开渠道。

价值：

- 当前系统保存历史消息和 thinking，数据泄漏风险已经存在。
- 即使 V1 无工具，也应建立安全边界表述。

建议落点：

- `01` 用户身份与鉴权。
- `02` 安全、限流与日志。
- `04` 错误展示、日志、测试计划。

### 4.2 中优先级：建议写入后续演进

#### 4.2.1 长期记忆与跨会话检索

建议作为后续方向记录：

- PostgreSQL full-text search / `pg_trgm` 支持关键词和模糊检索。
- 后续可增加 `message_embedding` 或 `memory_fact` 表支持语义检索。
- 记忆检索失败不影响主对话，采用 graceful degradation。
- V1 不做会话搜索、长期记忆和自动记忆写入。

建议落点：

- `02` 数据扩展说明。
- `04` 非首版范围。

#### 4.2.2 上下文压缩策略

建议后续演进说明：

- 压缩发生在请求前 preflight 阶段。
- 保留最近 N 轮完整消息。
- 旧消息摘要为一条摘要消息。
- 历史回放仍查询原始消息，不因模型输入压缩而丢失。
- 后续工具调用场景下，tool call 与 tool result 必须成对保留。
- 辅助摘要任务可使用独立小模型。

建议落点：

- `02` 数据设计后续演进。
- `04` 第 6.2 / 非首版范围。

#### 4.2.3 Gateway / Lane Queue 演进

建议写明：

- V1 使用 REST + SSE，不引入 WebSocket Gateway。
- 如后续支持多端、多渠道、后台任务、工具调用，可演进为 Gateway 控制平面。
- 当前 `tryAcquire + 409` 是 V1 简化版单 lane 串行。
- 引入后台任务或工具后，应升级为 Lane Queue：主会话 lane 串行，后台 lane 可并行。

建议落点：

- `02` 总体架构后续演进。
- `04` 并发控制章节。

#### 4.2.4 Tool / Skill / Plugin 路线

建议作为平台后续能力记录：

- Tool：结构化执行能力。
- Skill：Markdown/YAML frontmatter 形式的操作指导。
- Plugin：能力交付和注册单元。
- 后续采用 manifest-first + registry pattern。
- 默认 deny，按应用/用户/agent 授权。
- 工具执行需要审批、审计和 sandbox。

建议落点：

- `01` 非首版范围。
- `02` 后续演进参考。
- `04` 非首版范围。

#### 4.2.5 幂等与重试语义

建议补充：

- V1 不自动重试完整对话流程。
- 用户消息写入后，模型失败只更新助手失败态。
- 后续如做重试，只重试模型调用步骤，不重复写用户消息。
- 未来工具执行、外部消息发送必须带 idempotency key。

当前 `02` 已有“不自动重试”说明，但可补“只重试当前步骤”的原则。

## 5. 不建议进入 V1 的能力

以下能力在调研中多次出现，但不建议进入当前 V1：

- 多渠道 Gateway：Telegram、Slack、WhatsApp、微信、QQ 等。
- WebSocket 控制平面。
- Agent 工具执行：shell、文件、浏览器、API 调用。
- Docker / SSH / remote sandbox。
- Plugin marketplace / ClawHub / Skills Hub。
- 自动创建技能、自我修补技能。
- 子代理委派、多 Agent 路由、delegate 架构。
- Cron / heartbeat / proactive autonomous tasks。
- 文件 checkpoint / rollback。
- Canvas / A2UI / voice / mobile node。
- 长期记忆自动写入、Dreaming、闭环学习。

理由：这些能力会把项目从“通用对话 V1”拉到“Agent 平台 runtime”，显著增加安全、权限、审计、沙箱、提示词注入、任务调度和运营复杂度。

## 6. 按文档映射的修改建议

### 6.1 `01-通用大模型对话需求.md`

建议补充：

- V1 鉴权只适合内部验证，不是生产鉴权。
- 非首版范围增加：
  - 停止生成按钮。
  - 长期记忆。
  - 上下文压缩。
  - 历史会话检索。
  - 多端/多渠道接入。
  - Agent 工具调用。
  - 插件化能力。
  - 子 Agent 委派。
  - 定时自主任务。
- 排队上限明确为默认 10 条，超出提示用户等待。

### 6.2 `02-通用大模型对话技术方案.md`

建议新增“架构设计原则”：

- Core 层与 Web 层解耦。
- 会话内 Prompt 不变性。
- 同 session 串行、跨 session 并行。
- 可选模块采用 register + availability check，不硬耦合核心对话链路。
- Access control before intelligence。

建议新增或补充“后续演进参考”：

- `LlmProviderAdapter`。
- `ContextAssembler`。
- Gateway 控制平面。
- Lane Queue。
- 长期记忆与跨会话检索。
- Tool / Skill / Plugin 三层能力。
- 工具执行 sandbox 与审计。
- 幂等 key 与步骤级重试。

### 6.3 `04-通用大模型对话详细设计.md`

建议补充：

- `llm/provider` 包和 `LlmProviderAdapter` 接口草案。
- `llm/context` 包和 `ContextAssembler`、`ContextBudgetEstimator`、`ContextMessageMapper`。
- `summary_message_id` 精确定义。
- `QUEUE_LIMIT_EXCEEDED` 前端错误或状态。
- 日志脱敏规则。
- `source_type` 字段作为后续 WEB/API/MOBILE 来源标识，可列为后续字段，不建议 V1 必建。
- `message_embedding` / `memory_fact` 作为后续表，不建议 V1 建表。
- 流式渲染 coalescing 和 Markdown/code fence 完整性作为前端优化项。

### 6.4 `03-原型设计/chat-prototype.html`

当前不建议更新。

停止生成、搜索、工具面板、插件管理、多端状态都不是 V1 必做。原型应保持当前通用对话 V1 范围，避免误导实现。

## 7. 优先级建议

### 7.1 建议立即更新到文档

1. `LlmProviderAdapter` 抽象。
2. `ContextAssembler` 抽象。
3. `summary_message_id` 精确定义。
4. Prompt 稳定性约束。
5. 队列 cap / overflow / `QUEUE_LIMIT_EXCEEDED`。
6. V1 鉴权边界与日志脱敏。

这些内容不扩大实现范围，主要是明确边界和降低后续返工。

### 7.2 建议作为后续演进写入

1. Gateway 控制平面。
2. Lane Queue。
3. 长期记忆与跨会话检索。
4. 上下文压缩 preflight 策略。
5. Tool / Skill / Plugin 体系。
6. 工具 sandbox / 审批 / 审计。
7. 子代理委派约束。
8. 幂等 key 与步骤级重试。

### 7.3 建议暂不写入或仅保留远期一句话

1. 自动技能创建和自我修补。
2. Dreaming / heartbeat / proactive agent。
3. Canvas / A2UI。
4. 多渠道 messaging gateway。
5. 文件 checkpoint / rollback。
6. RL / batch trajectory / MLOps 能力。

## 8. 综合评分

| 维度 | 评分 | 说明 |
|------|------|------|
| V1 边界控制 | ★★★★★ | 当前文档没有盲目引入 Agent 平台能力，边界稳定 |
| 对调研吸收程度 | ★★★★☆ | 已吸收流式、并发、错误、安全部分，provider/context 抽象仍可补 |
| 后续演进清晰度 | ★★★☆☆ | 有字段预留，但缺少 Hermes/OpenClaw 风格的路线说明 |
| 工程可落地性 | ★★★★☆ | 当前方案可直接指导 V1 开发，少量抽象补充后更稳 |
| 安全边界表达 | ★★★☆☆ | 已有 CORS/限流/日志，但鉴权 mock 与脱敏规则需更醒目 |

## 9. 最终建议

建议不要把 6 篇调研报告中的能力逐项塞入 V1。正确做法是：

1. **V1 保持通用 Web 对话**：会话、消息、流式、thinking、历史续聊、排队、失败态。
2. **补充架构抽象**：`LlmProviderAdapter` 和 `ContextAssembler`。
3. **补清语义边界**：`summary_message_id`、Prompt 稳定性、队列上限、安全和日志脱敏。
4. **把 Agent 平台能力放入后续演进**：Gateway、Lane Queue、Tool/Skill/Plugin、长期记忆、sandbox、子代理。
5. **原型不扩张**：`03` 暂不增加停止生成、搜索、工具面板等非 V1 功能。

这能在不扩大首版实现成本的前提下，把 Hermes Agent 和 OpenClaw 的成熟设计原则沉淀到文档中，为后续平台化留出清晰路线。
