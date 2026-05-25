# TASK-V3-022 biz-chat 主流程

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

`chat-minimal` 的核心是 Agent 选择、Session、消息发送、重新生成、编辑重发、取消和执行详情可追踪的最小 Chat 闭环。

## 目标

- 实现 Chat send / stream 两步模型。
- 实现 Session 创建、查询、重命名、删除。
- 实现运行互斥、Run / Span 状态流转、执行详情查询。
- 实现 cancel、regenerate、edit-resend。

## 非目标

- 不实现完整登录、Admin 配置、工作流、任务中心和 input-ocr。

## owned modules

```text
source/v3-agent-platform/agent-biz/biz-agent
source/v3-agent-platform/agent-biz/biz-session
source/v3-agent-platform/agent-biz/biz-chat
source/v3-agent-platform/agent-biz/biz-execution
source/v3-agent-platform/agent-boot
```

## 输入契约

`agent-api` DTO、`runtime-spi` 事件、`ChatRepository`、`CapabilityGate`、`SseEventMapper`。

## 输出契约

HTTP API：

```text
GET /api/agents
GET /api/agents/{agentId}
GET /api/agents/tags
POST /api/sessions
GET /api/sessions
GET /api/sessions/{sessionId}
PATCH /api/sessions/{sessionId}
DELETE /api/sessions/{sessionId}
POST /api/chat/send
GET /api/chat/{runId}/stream
POST /api/chat/{runId}/cancel
POST /api/chat/{messageId}/regenerate
POST /api/chat/{messageId}/edit-resend
GET /api/executions/{runId}
```

## 目标验收等级

L5；当前本地 API/SSE 和 UI 验收达到 L4，真实 L5 依赖 `.env` 与真实持久化。

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn test
```

## 真实 E2E 要求

所有 E2E 请求必须携带 `Authorization` 和 `X-Test-Run-Id`。
