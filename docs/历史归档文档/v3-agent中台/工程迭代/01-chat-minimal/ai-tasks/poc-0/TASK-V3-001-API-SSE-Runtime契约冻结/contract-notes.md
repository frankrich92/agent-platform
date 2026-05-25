# TASK-V3-001 契约补充

## 本任务涉及接口

- `POST /api/chat/send`
- `GET /api/chat/{runId}/stream`
- `POST /api/chat/{runId}/cancel`
- `POST /api/chat/{messageId}/regenerate`
- `POST /api/chat/{messageId}/edit-resend`
- `GET /api/tasks/{taskRunId}/stream`
- `GET /api/workflow-runs/{workflowRunId}`

## 本任务涉及 SSE event

Chat / Task / Workflow SSE event 类型沿用主契约第 7 节。本任务补充 Chat event payload 最低字段、未知字段兼容、`heartbeat` 保活语义和 `Last-Event-ID` 恢复约束。

## 本任务涉及 RuntimeEvent

本任务明确 RuntimeEvent 与 SSE event 不强制一一对应，允许 `platform-stream` 聚合或拆分，但必须保留 `runId`、`spanId`、`sequence`、`traceId` 和可审计原始 runtime 事件引用。

## 本任务涉及错误码

本任务追加：

```text
EXTERNAL_AGENT_TIMEOUT
STREAM_RESUME_FAILED
STREAM_CHECKPOINT_FAILED
```

并冻结错误码兼容规则：只追加，不删除、不重命名、不改变既有语义。

## 参考开源产品与 API / 模型依据

| 参考项目 | 版本/commit | 源码或文档定位 | 实际 API / 模型 / 字段 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamableAgent.java` | `Flux<Event> stream(List<Msg> msgs, StreamOptions options)` | 采纳为 `runtime-agentscope` 普通 Chat 流式输入来源 | 不直接把 `Flux<Event>` 暴露给前端 | 前端只依赖平台 SSE envelope，runtime 事件需经过权限、审计和脱敏映射 |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/Event.java` | `getType()`、`getMessage()`、`isLast()`、`getSource()`、`getMessageId()` | 采纳 `isLast` 区分 delta/done，采纳 `source` 支持后续子 Agent 展示 | 不采纳 AgentScope `Msg` 作为平台 Message DTO | 平台 Message 需要用户隔离、审计、任务和工作流字段 |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/EventType.java` | `REASONING`、`TOOL_RESULT`、`HINT`、`AGENT_RESULT`、`SUMMARY`、`ALL` | 映射为平台 `assistant.*`、`tool.call.*`、Run/Span/Audit | 不把 `HINT` 默认展示给普通用户 | HINT 可能包含 RAG、Memory 或规划上下文，需脱敏和权限控制 |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamOptions.java` | `eventTypes(...)`、`incremental(true)`、`includeReasoningChunk`、`includeActingChunk` | 采纳增量流和事件过滤能力 | 不让前端直接配置 AgentScope `StreamOptions` | StreamOptions 属运行时装配细节，应由平台按 Agent 配置和权限生成 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/AgentHandler.java` | `streamQuery(AgentRequest request, Object messages)`、`isHealthy()`、`getStreamAdapter()` | 采纳为 `runtime-sandbox` adapter 参考，保留健康检查和 raw stream adapter 思路 | 不作为普通 Chat 默认路径 | 高风险执行需要沙箱和独立能力 gate；普通 Chat 默认 `agentscope-java` |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/StreamAdapter.java` | `Flux<Event> adaptFrameworkStream(Object sourceStream)` | 采纳“framework event -> runtime event”的适配边界 | 不采纳其 schema 作为平台公开 SSE DTO | 平台 SSE 需要稳定 envelope、Last-Event-ID 和前端兼容规则 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/AgentRequest.java` | `model`、`input`、`session_id`、`user_id`、`temperature`、`top_p` | 采纳 `input/session/user/model` 的最低请求语义 | 不直接采纳 snake_case 为平台 Java DTO 命名 | 平台 Java 契约保持 camelCase，adapter 负责外部协议转换 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Event.java` | `sequence_number`、`object`、`status`、`error`、`created()`、`inProgress()`、`completed()`、`failed(Error)`、`rejected()`、`canceled()` | 采纳 sequence/status/error 映射到 `RuntimeEvent` 和 SSE | 不直接暴露 runtime 原始 status 给前端 | 前端状态以平台 `eventType` 和 payload 为准 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/Runner.java` | `adapter.streamQuery(...)`、`streamAdapter.adaptFrameworkStream(...)`、`withSequenceNumber(Event)`、`isHealthy()` | 采纳“raw framework stream -> runtime Event -> sequence”的流水线 | 不采纳 runtime 自行决定平台持久化和用户权限 | V3 由 platform-stream/repo/observability 负责持久化、权限和审计 |

## 是否需要修改主契约

已修改 `docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md`：

- 标注 POC-0 API/SSE/Runtime 基线冻结状态。
- 补充 Chat send / stream URL 兼容规则。
- 补充 SSE envelope 稳定字段、payload 兼容和 heartbeat 规则。
- 补充 Chat event payload 最低字段。
- 补充工具事件脱敏约束。
- 明确 RuntimeEvent 与 SSE event 可聚合或拆分映射。
- 补充 AgentScope Java / AgentScope Runtime Java 到平台 RuntimeEvent 的最低映射。
- 追加 stream 和 external agent 相关错误码。

## 待人工确认

- 是否需要为“AgentScope 作为普通 Chat 默认执行底座”新增 ADR，对应 `RTK.md` 的 `OQ-V3-001`。
- 是否需要把 `runtime.raw` 作为正式 RuntimeEvent 类型长期保留，还是仅作为 POC-1 runtime-spi 的临时 adapter 事件。
- `assistant.thinking.*` 是否默认对所有业务老师可见，还是由 Agent 配置或用户权限控制。
- `tool.call.*` 在管理员执行详情中可见的原始参数范围，需要后续权限和审计任务继续细化。
