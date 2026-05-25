# ADR-0001 AgentScope 普通 Chat 默认执行底座

## 状态

Accepted

## 日期

2026-05-19

## 背景

`chat-minimal` 需要先跑通业务老师可用的最小 Chat 闭环。V3 已有调研结论和本地源码参考，普通 Chat 可以复用 `agentscope-java` 的 `ReActAgent`、Toolkit、SkillBox、MCP tool、Memory、Session、StreamOptions 和事件体系。

如果平台在 POC 阶段自研 ReAct 循环、工具调用循环或流式事件引擎，会扩大实现范围，并削弱后续与 AgentScope 生态的兼容性。

## 决策

普通 Chat 默认使用 `agentscope-java` 作为执行底座，优先通过 `ReActAgent` 和 AgentScope 事件体系产出运行时事件。

平台只负责：

- Agent 授权和用户隔离。
- Capability gate 和 `RuntimeCapabilityPlan`。
- AgentScope runtime adapter。
- RuntimeEvent 到 SSE envelope 的映射。
- Run / Span / Audit、持久化和前端展示。

平台不自研默认 ReAct 循环，不通过关键词规则绕过 AgentScope 直接触发工具调用。高危脚本、浏览器自动化、文件写入、系统命令等系统级动作由 Capability gate 判定进入 `runtime-sandbox` 或 `disabled`。

## 备选方案

- 平台自研 ReAct / Tool loop：灵活但实现复杂，容易偏离 POC 最小闭环。
- Hermes Agent 作为默认执行底座：API 和 Run 模型有参考价值，但不作为 V3 普通 Chat 默认路径。
- 全部外部 Agent API 化：短期接入快，但无法验证 V3 中台自身的 AgentScope 执行、SSE、Run/Span 和审计链路。

## 取舍分析

采纳 AgentScope Java 可以降低普通 Chat 执行侧复杂度，把 V3 平台精力集中在授权、能力治理、输入治理、事件映射、持久化和体验验收上。

不采纳自研 ReAct 循环，是为了避免 POC 阶段把运行时问题和中台治理问题混在一起。

## 影响范围

- `02-Agent中台技术架构方案.md`
- `03-API-SSE-运行时契约-全局基线.md`
- `工程迭代/01-chat-minimal/02-工程契约切片.md`
- `TASK-V3-012-runtime-spi契约`
- `TASK-V3-020-runtime-agentscope最小适配`

## 验收方式

- POC-1：`runtime-spi` 不暴露 AgentScope 原始事件给前端。
- POC-2：真实 Chat 通过 AgentScope Java ReActAgent 执行，并映射为平台 RuntimeEvent / SSE。
- L5：具备 `.env` 时跑通真实 LLM + AgentScope Java + PostgreSQL + SSE + 前端页面。

## 后续事项

- `runtime-hermes` 仅保留占位和外部复杂 Agent 参考。
- 如后续替换默认执行底座，必须新增 ADR supersede 本决策。
