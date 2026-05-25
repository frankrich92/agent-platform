# TASK-V3-015 agent-api 契约

## 状态

Review

## 背景

`agent-api` 承载 `chat-minimal` 的 HTTP DTO、SSE envelope、错误结构和分页结构。POC-1 已包含 `agent-domain`、`runtime-spi`、`repo-spi` 和 architecture test，但还缺少独立的 API 契约任务，容易让 POC-2 前后端并行时出现 DTO 和事件字段分叉。

## 目标

- 定义 `chat-minimal` 所需的 Agent、Tag、Session、Chat、Execution 查询 DTO。
- 定义 SSE envelope、Chat SSE payload、错误响应和分页响应结构。
- 保持 `agent-api` 不依赖 Controller、业务服务、持久化实现或 AgentScope 实现类。
- 不在 `agent-api` 重复定义 `RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent`、`RuntimeResult`；这些 Java 契约归属 `runtime-spi`。
- 为 POC-2 后端、前端和 E2E 并行开发提供稳定 API / SSE 类型边界。
- 记录 OpenAPI 导出和 TypeScript 类型生成策略；如 POC-1 不生成 OpenAPI，必须在 `contract-notes.md` 写明 POC-2 前的手工对齐检查方式。
- 在 `contract-notes.md` 中记录 HTTP API、SSE envelope、错误结构、分页结构和前端 stream client 参考的具体来源，必须精确到源码路径、实际 endpoint/DTO/事件字段和本任务取舍。

## 非目标

- 不实现 Controller。
- 不实现业务流程。
- 不实现前端 API client。
- 不实现 Task、Workflow、ExternalAgent、Attachment / OCR 的完整 DTO。
- 不修改全局契约基线，除非发现必须回写的公共语义问题。

## 必须阅读

```text
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
docs/v3-agent中台/工程迭代/01-chat-minimal/03-工程技术设计.md
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/EventType.java
source/htam-agent-platform/agent-web/src/main/java/com/htam/agent/web/chat/ChatController.java
source/htam-agent-platform/agent-api/src/main/java/com/htam/agent/api/chat/StreamEnvelope.java
source/htam-agent-platform-ui/src/api.ts
source/v3-agent-platform/AGENTS.md
```

## owned modules

```text
agent-api
```

## owned files

```text
source/v3-agent-platform/agent-api/**
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-015-agent-api契约/contract-notes.md
```

## 禁止修改

```text
source/v3-agent-platform/agent-biz/**
source/v3-agent-platform/agent-admin/**
source/v3-agent-platform/agent-platform/**
source/v3-agent-platform/agent-runtimes/**
source/v3-agent-platform/agent-repo/**
source/v3-agent-platform/agent-boot/**
source/v3-agent-platform-ui/**
```

## 输入契约

`chat-minimal` 工程契约切片中的 API、SSE event、错误码和分页响应语义。

## 输出契约

`agent-api` Java DTO / enum / envelope 契约和最小单元测试。

如引入 OpenAPI 生成能力，输出契约还应包含 OpenAPI 导出方式；如暂不引入，输出契约必须包含前端 TypeScript 类型手工对齐清单。

## 目标验收等级

L1

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn -pl agent-api test
```

## 真实 E2E 要求

无。

## 风险

API DTO 如果过早暴露 Task、Workflow、Attachment / OCR 等后续能力，会扩大 `chat-minimal` 的 POC-1 实现和评审范围。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
