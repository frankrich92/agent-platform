# TASK-V3-012 runtime-spi 契约

## 状态

Review

## 背景

运行时适配层需要统一 `agentscope-java`、`agentscope-runtime-java` 和未来 `runtime-hermes` 占位的请求、事件、能力计划和错误处理。

## 目标

- 定义 Runtime SPI、RuntimeRequest、RuntimeEvent、RuntimeCapabilityPlan、RuntimeResult 等最小契约。
- 明确普通 Chat 与高风险 sandbox 执行的边界。
- 参考 AgentScope Java 和 AgentScope Runtime Java 的实际接口，不暴露原始事件给前端。
- 在 `contract-notes.md` 中记录 Runtime SPI、RuntimeRequest、RuntimeEvent、状态枚举和流式事件参考的具体来源，必须精确到 commit、源码路径、实际方法签名/字段和本任务取舍。

## 非目标

- 不实现 `runtime-agentscope`。
- 不实现 `runtime-sandbox`。
- 不接入 Hermes。
- 不实现 MCP/Skills 业务逻辑。

## 必须阅读

```text
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/调研/README.md
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamableAgent.java
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamOptions.java
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/EventType.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/AgentHandler.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/AgentRequest.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Event.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Message.java
source/v3-agent-platform/AGENTS.md
```

## owned modules

```text
agent-runtimes/runtime-spi
```

## owned files

```text
source/v3-agent-platform/agent-runtimes/runtime-spi/**
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-012-runtime-spi契约/contract-notes.md
```

## 禁止修改

```text
source/v3-agent-platform/agent-runtimes/runtime-agentscope/**
source/v3-agent-platform/agent-runtimes/runtime-sandbox/**
source/v3-agent-platform/agent-runtimes/runtime-hermes/**
```

## 输入契约

`chat-minimal` Runtime 契约切片和 V3 API/SSE/Runtime 全局候选基线。

## 输出契约

Runtime SPI Java 契约和最小单元测试。

`contract-notes.md` 必须说明 `StreamableAgent.stream(...)`、`StreamOptions`、`EventType`、`AgentRequest`、`Event`、`Message`、`AgentHandler.streamQuery(...)` 与 V3 Runtime SPI 的映射关系。

## 目标验收等级

L1

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn -pl agent-runtimes/runtime-spi test
```

## 真实 E2E 要求

无。

## 风险

SPI 过度绑定 AgentScope 或 Hermes 会影响后续替换和测试。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
