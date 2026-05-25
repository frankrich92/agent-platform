# TASK-V3-020 contract notes

## 涉及契约

- Runtime SPI：`RuntimeClient.stream(RuntimeRequest)`、`RuntimeClient.cancel(String)`、`RuntimeHealth`。
- RuntimeEvent：`ASSISTANT_DELTA`、`ASSISTANT_DONE`；`ASSISTANT_THINKING_DELTA` 保留在 SPI，但普通 Chat SSE 不暴露 AgentScope `ThinkingBlock`。
- 配置：`HTAM_LLM_BASE_URL`、`HTAM_LLM_API_KEY`、`HTAM_LLM_MODEL_NAME`、`HTAM_LLM_MODEL`、`HTAM_AGENT_FALLBACK_ONLY`。

## 参考依据

| 来源 | 版本 / commit | 路径或 API | 采纳点 | 不采纳点 | 原因 |
| --- | --- | --- | --- | --- | --- |
| AgentScope Java | Maven `io.agentscope:agentscope-core:1.0.12`；本地 fork `13a71676` | `io.agentscope.core.ReActAgent`、`StreamableAgent.stream(...)`、`StreamOptions.builder()`、`EventType.REASONING/TOOL_RESULT/AGENT_RESULT`、`TextBlock`、`ThinkingBlock`、`Msg.builder().textContent(...)` | 使用 `ReActAgent` 作为普通 Chat 默认执行底座，使用 `TextBlock` 生成平台 `RuntimeEvent`，必要时用 `AGENT_RESULT` 兜底最终文本 | 不把 AgentScope 原始 event、`ContentBlock` 或 `ThinkingBlock` 暴露给前端 | 平台需要稳定 RuntimeEvent / SSE envelope，并避免普通 Chat 暴露模型 thinking 内容 |
| AgentScope Java OpenAI model | Maven `1.0.12` | `OpenAIChatModel.builder().baseUrl(...).apiKey(...).modelName(...).stream(true)` | 通过环境变量接入 OpenAI-compatible provider | 不在代码、文档或测试报告中保存密钥 | 符合安全配置约束 |

## 当前取舍

- 无 `.env` 时 deterministic runtime 只用于本地 L3/L4 验收。
- 有真实 provider 配置时不再 `onErrorResume` 到 fallback，避免 L5 假通过。
- 模型名称优先读取 `HTAM_LLM_MODEL_NAME`，兼容旧变量 `HTAM_LLM_MODEL`。
