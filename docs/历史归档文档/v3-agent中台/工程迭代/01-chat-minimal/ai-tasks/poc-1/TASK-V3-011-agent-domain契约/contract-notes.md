# TASK-V3-011 契约补充

> 本文为 POC-1 规划阶段预填。POC-1 执行完成后，必须复核并更新所有“本任务采纳”“本任务不采纳”和待人工确认项。

## 本任务涉及接口

无 HTTP 接口，涉及 Java 领域对象契约。执行后需要补充最终 V3 类名和包路径。

## 本任务涉及 SSE event

无直接 SSE event。领域模型必须能支撑 `message.created`、`assistant.delta`、`assistant.done`、`message.completed`、`run.completed`、`run.failed`、`run.cancelled` 等事件中的 message / run / status 语义。

## 本任务涉及 RuntimeEvent

不定义 Runtime SPI，但需要为 `RuntimeEvent` 映射提供领域侧实体和枚举基础，包括 `MessageRole`、`MessageStatus`、`RunStatus`、`SpanStatus`、`ToolCallStatus`。

## 本任务涉及错误码

不直接定义 HTTP 错误码。领域枚举必须能支持 `AGENT_DISABLED`、`AGENT_NOT_AUTHORIZED`、`CAPABILITY_NOT_APPROVED`、`CAPABILITY_NOT_AUTHORIZED`、`SESSION_BUSY`、`RUN_CANCELLED` 等业务错误的判定语义。

## 本任务涉及 DTO / 数据模型 / 表结构 / 状态机

- DTO：不直接定义 HTTP DTO。
- 数据模型：涉及 Agent、AgentVersion、Session、Message、Run、Span、Capability、Provider、Model、ToolCall、UserGrant 等领域模型。
- 表结构：不定义 Flyway SQL，但领域字段必须覆盖 `02-工程契约切片.md` 的最低字段语义。
- 状态机：涉及 Agent status、audit status、Session status、Message status、Run status、Span status、ToolCall status、Capability status。

## 参考开源产品与 API / 模型依据

| 参考项目 | 版本/commit | 源码或文档定位 | 实际 API / 模型 / 字段 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/message/Msg.java` | `Msg.id`、`name`、`role`、`content`、`metadata`、`timestamp` | 采纳消息 id、role、content、metadata、timestamp 的领域语义 | 不把 `Msg` 作为 V3 领域类，也不直接持久化 `ContentBlock` 列表 | 平台需要保留 userId、agentId、agentVersionId、sessionId、runId、status、testRunId 等治理字段 |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/message/MsgRole.java` | `USER`、`ASSISTANT`、`SYSTEM`、`TOOL` | 采纳为 `MessageRole` 的基础枚举语义 | 不开放 AgentScope 包名或枚举给 API / DB | 避免前端和持久化层绑定第三方实现 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Message.java` | `Message.id`、`object`、`type`、`role`、`content`、`code`、`message`、`usage`、`metadata`、`status` | 采纳 message 与 runtime event 共享 status / metadata / usage 的设计参考 | 不采纳 `object/type` 作为平台领域必选字段 | V3 领域模型按平台资源建模，runtime 原始字段只在适配层映射 |
| AgentScope Runtime Java | `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Session.java` | `Session.id`、`user_id`、`messages` | 采纳 session 归属用户并聚合消息的语义 | 不采纳 runtime session 直接携带完整历史作为 DB 模型 | V3 需要分页、授权、审计和 testRunId 隔离 |
| chat-minimal 工程契约切片 | 当前文档版本 | `docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md` | `agent_definition`、`agent_version`、`agent_session`、`agent_message`、`agent_run`、`agent_span`、`agent_tool_call`、`agent_audit_log` 最低字段 | 作为本任务领域字段的权威范围 | 不扩展到 Task、Workflow、ExternalAgent、Attachment / OCR、Memory 完整模型 | 保持 POC-1 范围可评审 |
| 本地 V2 API 模型 | 当前工作区 | `source/htam-agent-platform/agent-api/src/main/java/com/htam/agent/api/chat/AgentSession.java`、`AgentMessage.java`、`source/htam-agent-platform/agent-api/src/main/java/com/htam/agent/api/run/AgentRunDetail.java` | `AgentSession.id/agentId/title/status/messageCount`，`AgentMessage.id/agentId/role/status/sequence/parts`，`AgentRunDetail.id/agentId/sessionId/status/traceId/spans` | 采纳用户体验所需的会话、消息和执行详情概念 | 不沿用 `userCode`、旧 provider/model 字段命名和旧 DTO 包结构 | V3 以小工程契约和新模块边界为准 |

## 是否需要修改主契约

默认不需要。若执行时发现领域对象必须新增或重命名 `02-工程契约切片.md` 中的字段、枚举或状态语义，先写入本文，再由主 Agent 或 Review Agent 决定是否回写工程契约切片或全局基线。

## 待人工确认

- `MessageStatus` 是否需要在 POC-1 明确区分 `created / streaming / completed / failed / cancelled`，还是仅冻结 `created / completed / failed / cancelled`。
- `ToolCallStatus` 是否在 POC-1 只保留枚举，具体字段留给 POC-2 stream / execution 任务补全。
