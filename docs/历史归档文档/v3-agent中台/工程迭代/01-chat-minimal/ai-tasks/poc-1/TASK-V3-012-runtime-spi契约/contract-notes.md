# TASK-V3-012 契约补充

> 本文为 POC-1 规划阶段预填。POC-1 执行完成后，必须复核并更新所有“本任务采纳”“本任务不采纳”和待人工确认项。

## 本任务涉及接口

涉及 Runtime SPI Java 接口。执行后需要补充最终 V3 接口名、方法签名和包路径。

建议最小接口语义：

```text
RuntimeClient.stream(RuntimeRequest, RuntimeCapabilityPlan) -> Flux<RuntimeEvent>
RuntimeClient.cancel(runId)
RuntimeClient.health()
```

## 本任务涉及 SSE event

不直接定义前端 SSE DTO，但需要输出可映射到以下 `chat-minimal` SSE event 的 runtime event：

```text
run.started
assistant.thinking.delta
assistant.thinking.done
assistant.delta
assistant.done
tool.call.started
tool.call.delta
tool.call.done
tool.call.failed
run.completed
run.failed
run.cancelled
```

## 本任务涉及 RuntimeEvent

涉及 `RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent`、`RuntimeResult`、runtime status、runtime error 和 cancel / health 契约。

## 本任务涉及错误码

Runtime SPI 不直接返回 HTTP status，但必须能承载并向上层映射：

```text
CAPABILITY_NOT_APPROVED
CAPABILITY_NOT_AUTHORIZED
RUNTIME_UNAVAILABLE
RUNTIME_EXECUTION_FAILED
RUN_CANCELLED
```

## 本任务涉及 DTO / 数据模型 / 表结构 / 状态机

- DTO：定义 runtime-spi 内部 Java DTO，不定义 HTTP DTO。
- 数据模型：不定义 DB entity，但 `RuntimeEvent` 必须携带 runId、messageId、sequence、eventType、payload、error、metadata 等足够字段。
- 表结构：不定义 Flyway SQL。
- 状态机：涉及 runtime health、run created / in_progress / completed / failed / rejected / canceled 的适配。

## 参考开源产品与 API / 模型依据

| 参考项目 | 版本/commit | 源码或文档定位 | 实际 API / 模型 / 字段 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamableAgent.java` | `stream(Msg)`、`stream(Msg, StreamOptions)`、`stream(List<Msg>, StreamOptions)`、`stream(List<Msg>, StreamOptions, Class<?>)`、`stream(List<Msg>, StreamOptions, JsonNode)` | 采纳 `Flux<Event>` 流式执行模型和 `List<Msg>` 输入方式 | POC-1 Runtime SPI 不暴露 structured output overload | `chat-minimal` 当前只做普通文本 Chat |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamOptions.java` | `eventTypes(EventType...)`、`incremental(boolean)`、`includeReasoningChunk(boolean)`、`includeReasoningResult(boolean)`、`includeActingChunk(boolean)` | 采纳 event filter、incremental、reasoning/tool chunk 控制思路 | 不把 `StreamOptions` 作为 V3 公共 API 类型 | 平台需要先经过 Capability gate 和脱敏映射 |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/EventType.java` | `REASONING`、`TOOL_RESULT`、`HINT`、`AGENT_RESULT`、`SUMMARY`、`ALL` | 映射到 V3 `assistant.thinking.*`、`assistant.*`、`tool.call.*` 和 run 终态 | 不直接暴露 `REASONING` / `TOOL_RESULT` 给前端 | 前端只依赖平台 SSE envelope |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/AgentRequest.java` | `model`、`top_p`、`temperature`、`input`、`session_id`、`user_id` | 采纳模型参数、输入消息、sessionId、userId 的请求结构 | 不直接使用 snake_case 字段作为 V3 Java DTO | V3 Java 侧使用 camelCase，HTTP/API 层再做序列化策略 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Event.java` | `sequence_number`、`object`、`status`、`error`，`created()`、`inProgress()`、`completed()`、`failed(Error)`、`rejected()`、`canceled()` | 采纳 sequence 和运行状态流转 | 不直接采用 `object` 开放字段和 runtime 原始 status 字符串 | V3 需要稳定 enum 和错误码映射 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Message.java` | `id`、`role`、`content`、`usage`、`metadata`、`status`、`addDeltaContent(Content)` | 采纳 delta 聚合、usage、metadata 和消息状态参考 | 不把 runtime `Message` 暴露到 `agent-api` | 避免前端绑定 runtime 实现 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/AgentHandler.java` | `streamQuery(AgentRequest, Object)`、`isHealthy()`、`getStreamAdapter()`、`getMessageAdapter()` | 采纳 adapter 生命周期、streamQuery、health、消息/stream adapter 边界 | 不把 `SandboxService` 暴露到 `chat-minimal` 默认路径 | 高风险 sandbox 后续小工程再落地 |

## 是否需要修改主契约

默认不需要。若执行时发现 Runtime SPI 需要新增公共 event type、错误码或请求字段，先写入本文，再由主 Agent 或 Review Agent 判断是否回写 `02-工程契约切片.md` 和全局基线。

## 待人工确认

- POC-1 是否冻结 `RuntimeEvent.payload` 的强类型分层，还是先使用受控 `Map<String,Object>` 并在 `agent-api` 侧定义前端 DTO。
- cancel 返回值是否只表达 accepted / already_terminal / not_found，还是在 POC-1 同步冻结更细状态。
