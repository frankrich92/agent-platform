# TASK-V3-020 runtime-agentscope 最小适配

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

`chat-minimal` 需要把普通 Chat 的执行底座接到 `agentscope-java`，同时在本地缺少 `.env` 时保留可验证的 deterministic runtime。

## 目标

- 实现 `runtime-agentscope` 对 `runtime-spi` 的最小适配。
- `.env` 完整时调用 AgentScope Java `ReActAgent`。
- `.env` 缺失或显式 `HTAM_AGENT_FALLBACK_ONLY=true` 时使用本地 deterministic stream。
- 真实 provider 已配置但执行失败时返回失败，不静默降级。

## 非目标

- 不实现工具调用、MCP、Skills 或沙箱执行。
- 不实现 input-ocr。

## 必须阅读

```text
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
docs/v3-agent中台/工程迭代/01-chat-minimal/03-工程技术设计.md
docs/v3-agent中台/工程迭代/01-chat-minimal/04-工程测试策略与真实E2E.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-012-runtime-spi契约/contract-notes.md
```

## owned modules

```text
source/v3-agent-platform/agent-runtimes/runtime-agentscope
```

## owned files

```text
source/v3-agent-platform/agent-runtimes/runtime-agentscope/**
```

## 禁止修改

```text
source/fork_source/
source/htam-agent-platform/
source/htam-agent-platform-ui/
```

## 输入契约

`RuntimeRequest`、`RuntimeEvent`、`RuntimeClient`。

## 输出契约

返回 `ASSISTANT_DELTA`、`ASSISTANT_DONE` 等平台 runtime 事件，供 `platform-stream` 映射为 SSE。

## 目标验收等级

L5；当前本地验收达到 L3/L4，真实 L5 依赖 `.env`。

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn test
```

## 真实 E2E 要求

`.env` 完整时必须使用真实 AgentScope Java / LLM；无 `.env` 时只能声明 L5 未执行。

## 完成后输出

- `AgentScopeRuntimeClient`。
- 测试报告和交接记录。
