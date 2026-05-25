# TASK-V3-001 API/SSE/Runtime 契约冻结

## 状态

Review

## 背景

多子 Agent 并行开发前，API、SSE、RuntimeRequest、RuntimeEvent、错误码需要先形成稳定主契约。

## 目标

- 复核 `03-API-SSE-运行时契约-全局基线.md` 是否覆盖 Chat、Task、ExternalAgent、SSE、Runtime SPI。
- 补齐缺失的字段命名、错误码、事件兼容规则和契约冻结规则。
- 对照 AgentScope Java、AgentScope Runtime Java、本地历史实现和成熟产品接口，标注代码级参考依据。
- 输出需要人工确认的破坏性变更或开放问题。

## 非目标

- 不实现接口代码。
- 不生成 OpenAPI 文件。
- 不改前端或后端源码。

## 必须阅读

```text
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/调研/README.md
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamableAgent.java
source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/EventType.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/AgentHandler.java
source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/agentscope/AgentScopeAgentHandler.java
```

## owned modules

```text
docs
```

## owned files

```text
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/contract-notes.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/test-report.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/handoff.md
```

## 禁止修改

```text
source/
docs/v1-通用大模型对话/
docs/v2-通用大模型对话/
```

## 输入契约

现有 V3 API/SSE/Runtime 契约草案。

## 输出契约

冻结后的 API/SSE/Runtime 主契约，以及任务内 `contract-notes.md` 引用依据。

## 目标验收等级

L0

## 必须执行的验证命令

```bash
git diff --check
rg -n "TODO|FIXME|TBD" docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结 || true
```

## 真实 E2E 要求

无。

## 风险

契约冻结不充分会导致 POC-1/POC-2 多 Agent 并行开发冲突。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
