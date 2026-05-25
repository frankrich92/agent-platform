# TASK-V3-022 contract notes

## 涉及契约

- HTTP API：Agent、Session、Chat、Execution。
- 错误码：`AUTH_REQUIRED`、`VALIDATION_FAILED`、`SESSION_BUSY`、`RUN_NOT_FOUND`、`STREAM_RESUME_FAILED`、`CAPABILITY_NOT_APPROVED`。
- 状态机：`RunStatus.RUNNING/COMPLETED/FAILED/CANCELLED`、`MessageStatus.STREAMING/COMPLETED/FAILED/CANCELLED`。

## 参考依据

| 来源 | 版本 / commit | 路径或 API | 采纳点 | 不采纳点 | 原因 |
| --- | --- | --- | --- | --- | --- |
| 本地 V2 后端 | 当前工作区 | `source/htam-agent-platform/agent-web/src/main/java/com/htam/agent/web/chat/ChatController.java` | 采纳会话、流式发送、重新生成、编辑重发的体验边界 | 不沿用旧 `/api/agent/sessions` 路径，也不采用一次请求直接 SSE 的接口形态 | V3 需要 send / stream 分离，便于恢复、取消和执行详情查询 |
| AgentScope Runtime Java | 本地 fork `7b9032b` | `engine-core/.../schemas/Event.java`、`Message.java`、`Session.java` | 采纳 run/message/session/status 拆分和 sequence 追踪 | 不直接暴露 runtime snake_case DTO | V3 使用稳定 Java record 和点分事件名 |

## 当前取舍

- `regenerate` 当前用固定提示“请重新回答上一条消息。”触发新 run，后续可基于历史上下文优化。
- `edit-resend` 复用原 session 和 agent，生成新 user / assistant message。
