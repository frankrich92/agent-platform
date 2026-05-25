# TASK-V3-015 契约补充

> 本文为 POC-1 规划阶段预填。POC-1 执行完成后，必须复核并更新所有“本任务采纳”“本任务不采纳”和待人工确认项。

## 本任务涉及接口

本任务不新增接口，只为 `chat-minimal` 已冻结接口定义 Java DTO：

```text
GET  /api/agents
GET  /api/agents/{agentId}
GET  /api/agents/tags

POST /api/sessions
GET  /api/sessions
GET  /api/sessions/{sessionId}
PATCH /api/sessions/{sessionId}
DELETE /api/sessions/{sessionId}

POST /api/chat/send
POST /api/chat/{runId}/cancel
POST /api/chat/{messageId}/regenerate
POST /api/chat/{messageId}/edit-resend
GET  /api/chat/{runId}/stream

GET  /api/executions/{runId}
```

## 本任务涉及 SSE event

本任务定义 `chat-minimal` 已冻结 SSE envelope 和 Chat event payload 类型，不新增事件类型：

```text
run.started
message.created
assistant.thinking.delta
assistant.thinking.done
assistant.delta
assistant.done
tool.call.started
tool.call.delta
tool.call.done
tool.call.failed
message.completed
run.completed
run.failed
run.cancelled
heartbeat
```

## 本任务涉及 RuntimeEvent

本任务不定义 runtime 执行 SPI。`RuntimeEvent` 的 Java SPI 由 `TASK-V3-012` 负责；`agent-api` 只定义对前端稳定的 SSE DTO。

## 本任务涉及错误码

本任务定义 `chat-minimal` 错误码枚举和错误响应 DTO：

```text
AUTH_REQUIRED
PERMISSION_DENIED
AGENT_NOT_FOUND
AGENT_DISABLED
AGENT_NOT_AUTHORIZED
CAPABILITY_NOT_APPROVED
CAPABILITY_NOT_AUTHORIZED
RUNTIME_UNAVAILABLE
RUNTIME_EXECUTION_FAILED
SESSION_NOT_FOUND
SESSION_BUSY
RUN_NOT_FOUND
RUN_CANCELLED
STREAM_RESUME_FAILED
STREAM_CHECKPOINT_FAILED
VALIDATION_FAILED
RATE_LIMITED
INTERNAL_ERROR
```

Attachment / OCR、Task、Workflow、ExternalAgent 错误码保留为全局候选，不进入本任务实现范围。

## 本任务涉及 DTO / 数据模型 / 表结构 / 状态机

- DTO：Agent、Tag、Session、Chat send/cancel/regenerate/edit-resend、Execution、分页响应、错误响应、SSE envelope 和 payload。
- 数据模型：只通过 DTO 表达 `02-工程契约切片.md` 已冻结数据语义，不定义 DB entity。
- 表结构：不定义 Flyway SQL。
- 状态机：需要定义面向前端稳定的 Agent status、Session status、Message status、Run status、ToolCall status 和 SSE event type。

## 参考开源产品与 API / 模型依据

| 参考项目 | 版本/commit | 源码或文档定位 | 实际 API / 模型 / 字段 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |
| V3 全局 API/SSE 基线 | 当前文档版本 | `docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md` | 通用响应、分页响应、API 分组、SSE envelope、错误码 | 采纳公共 DTO 语义和错误结构 | 不实现未进入 `chat-minimal` 的候选接口 | 小工程 contract slice 是当前实现权威 |
| chat-minimal 工程契约切片 | 当前文档版本 | `docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md` | Agent、Session、Chat、Execution API；`run.started`、`assistant.delta`、`tool.call.*`、`heartbeat`；错误码切片 | 采纳为本任务冻结范围 | 不扩大到 Attachment / OCR、Task、Workflow、ExternalAgent | 避免 POC-1 扩大 |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/EventType.java` | `REASONING`、`TOOL_RESULT`、`HINT`、`AGENT_RESULT`、`SUMMARY`、`ALL` | 作为 SSE payload 与 RuntimeEvent 映射参考，映射到 reasoning、tool、assistant final、summary 类事件 | 不直接暴露 AgentScope 原始事件名给前端 | 前端只依赖平台 SSE envelope |
| 本地 V2 Chat Controller | 当前工作区 | `source/htam-agent-platform/agent-web/src/main/java/com/htam/agent/web/chat/ChatController.java` | `GET/POST/PATCH/DELETE /api/agent/sessions`，`POST /{sessionId}/messages/stream`，`POST /{assistantMessageId}/regenerate`，`POST /{userMessageId}/edit-and-resend`，`GET /{messageId}/run` | 采纳会话、流式发送、重新生成、编辑重发、执行详情的体验边界 | 不沿用旧 `/api/agent/sessions` 路径和发起即返回 SSE 的 API 形态 | V3 采用 `/api/chat/send` 创建 run，再 `GET /api/chat/{runId}/stream` 读取 stream，便于恢复和取消 |
| 本地 V2 SSE DTO | 当前工作区 | `source/htam-agent-platform/agent-api/src/main/java/com/htam/agent/api/chat/StreamEnvelope.java` | `eventId`、`type`、`runId`、`sessionId`、`messageId`、`sequence`、`createdAt`、`payload` | 采纳 envelope 核心字段 | 不沿用旧事件名 `run_started`、`thinking_delta`、`message_delta`、`run_completed` | V3 使用点分事件名，并要求 `Last-Event-ID` 恢复 |
| 本地 V2 前端 stream client | 当前工作区 | `source/htam-agent-platform-ui/src/api.ts` | `streamRequest(...)` 使用 `fetch`、`Accept: text/event-stream`、`ReadableStream.getReader()`、`TextDecoder`、`AbortSignal`、`dispatchSse(...)` | 采纳 fetch stream、AbortController、事件分发和错误解析方式 | 不沿用 `X-Token` / `X-App-Code` 和旧路径 | V3 统一使用 `Authorization`、`Last-Event-ID` 和 `/api/chat/*` |

## 是否需要修改主契约

默认不需要。若执行时发现 `agent-api` DTO 无法表达已冻结 contract slice，应先写入本文，再由主 Agent 或 Review Agent 判断是否回写 `03-API-SSE-运行时契约-全局基线.md` 或 `02-工程契约切片.md`。

## 待人工确认

- 是否需要在 POC-1 同步生成 OpenAPI / TypeScript 类型，还是留到 POC-2 前端任务处理。
- SSE payload 是否在 POC-1 全部强类型化，还是先冻结 envelope 与核心 payload 字段，复杂 tool payload 由 POC-2 stream 任务补齐。
