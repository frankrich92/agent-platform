# TASK-V3-011 agent-domain 契约

## 状态

Review

## 背景

`agent-domain` 是后端依赖底座，必须先定义核心领域对象、枚举和值对象，避免上层模块各自定义重复模型。

## 目标

- 定义 `chat-minimal` 所需的 Agent、AgentVersion、VisibilityScope、RuntimeType、Session、Message、Run、Span、Capability、Provider、Model、ToolCall、UserGrant 等领域核心对象。
- 定义状态枚举和值对象，保持无基础设施依赖。
- 为 API、repo、runtime、biz-chat 等模块提供稳定领域语言。
- 在 `contract-notes.md` 中记录领域对象、状态枚举和消息模型参考的具体来源，必须精确到开源项目 commit、源码路径、实际类/字段和本任务取舍。

## 非目标

- 不引入 Spring、MyBatis、R2DBC、AgentScope。
- 不实现持久化。
- 不实现业务流程。
- 不定义 Task、Workflow、ExternalAgent、Memory 的完整领域模型。

## 必须阅读

```text
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/message/Msg.java
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/message/MsgRole.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Message.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/engine/schemas/Session.java
source/v3-agent-platform/AGENTS.md
```

## owned modules

```text
agent-domain
```

## owned files

```text
source/v3-agent-platform/agent-domain/**
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-011-agent-domain契约/contract-notes.md
```

## 禁止修改

```text
source/v3-agent-platform/agent-biz/**
source/v3-agent-platform/agent-admin/**
source/v3-agent-platform/agent-platform/**
source/v3-agent-platform/agent-runtimes/**
source/v3-agent-platform/agent-repo/**
```

## 输入契约

`chat-minimal` 契约切片中的领域对象、数据模型和运行链路。

## 输出契约

无基础设施依赖的 `agent-domain` 模型和最小单元测试。

`contract-notes.md` 必须说明 AgentScope `Msg` / `MsgRole`、AgentScope Runtime `Message` / `Session` 和 V3 数据切片之间的字段映射、采纳点、不采纳点及原因。

## 目标验收等级

L1

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn -pl agent-domain test
```

## 真实 E2E 要求

无。

## 风险

领域模型过早贴合数据库或外部运行时，会削弱模块隔离。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
