# TASK-V3-023 stream persistence

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

SSE 流式输出需要 checkpoint、`Last-Event-ID` 恢复和 final flush 语义，前端断线后不能只依赖内存事件。

## 目标

- 实现平台 SSE envelope 映射。
- 保存每个事件的 sequence、eventId、payload 和 accumulatedContent。
- 支持 `Last-Event-ID` 恢复。
- run 终态时回写 assistant final content。

## 非目标

- 不拆分独立 `repo-r2dbc-stream` adapter。
- 不实现多节点 fan-out。

## owned modules

```text
source/v3-agent-platform/agent-platform/platform-stream
source/v3-agent-platform/agent-repo/repo-spi
source/v3-agent-platform/agent-repo/repo-mybatis
source/v3-agent-platform/agent-repo/repo-r2dbc-stream
```

## 输入契约

`RuntimeEvent`、`SseEnvelope`、`MessageCheckpoint`。

## 输出契约

SSE event：`run.started`、`message.created`、`assistant.delta`、`assistant.done`、`message.completed`、`run.completed`、`run.failed`、`run.cancelled`。

## 目标验收等级

L3-L5；当前达到本地 L3/L4，真实持久化 L5 未完成。

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn test
```
