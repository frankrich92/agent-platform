# TASK-V3-013 repo-spi 契约

## 状态

Review

## 背景

`chat-minimal` 需要同时支持 MyBatis-Plus 普通 CRUD 和 R2DBC SSE 高频持久化，因此 repo 端口必须先稳定。

## 目标

- 定义 repo-spi 的仓储端口和事务边界。
- 区分普通业务仓储和 stream checkpoint/final flush 仓储。
- 为后续 `repo-mybatis` 和 `repo-r2dbc-stream` 并行实现提供稳定接口。
- 范围仅覆盖 Agent、AgentVersion、Session、Message、MessageCheckpoint、Run、Span、ToolCall、Audit、Provider、Capability 等 `chat-minimal` 数据对象。
- 在 `contract-notes.md` 中记录仓储端口、stream checkpoint、表结构和状态查询参考的具体来源，必须精确到 commit、源码路径、实际表字段/索引或接口语义和本任务取舍。

## 非目标

- 不实现 MyBatis-Plus mapper。
- 不实现 R2DBC adapter。
- 不写 Flyway SQL。
- 不定义 Task、Workflow、ExternalAgent、Memory 的仓储端口。

## 必须阅读

```text
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
source/fork_source/agentscope-java/agentscope-extensions/agentscope-extensions-session-mysql/src/main/java/io/agentscope/core/session/mysql/MysqlSession.java
source/htam-agent-platform/agent-web/src/main/resources/db/migration/V1__init_agent_chat_tables.sql
source/v3-agent-platform/AGENTS.md
```

## owned modules

```text
agent-repo/repo-spi
```

## owned files

```text
source/v3-agent-platform/agent-repo/repo-spi/**
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-013-repo-spi契约/contract-notes.md
```

## 禁止修改

```text
source/v3-agent-platform/agent-repo/repo-mybatis/**
source/v3-agent-platform/agent-repo/repo-r2dbc-stream/**
```

## 输入契约

`chat-minimal` 数据切片、安全审计和 stream 持久化边界。

## 输出契约

repo-spi Java 契约和最小单元测试。

`contract-notes.md` 必须说明 AgentScope `MysqlSession` 增量存储、V3 数据切片和本地历史 Flyway 表结构对 repo-spi 端口的影响。

## 目标验收等级

L1

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn -pl agent-repo/repo-spi test
```

## 真实 E2E 要求

无。

## 风险

repo-spi 如果泄漏具体 ORM 或 R2DBC 类型，会限制后续实现替换。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
