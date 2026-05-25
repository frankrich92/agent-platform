# 调研结论索引

> 本目录保存 AgentScope、Hermes Agent 等方案调研。当前 V3 架构以本文件的“最终采纳状态”为准，单篇旧调研中的讨论性结论不应被直接当作最新路线。

## 1. 当前最终采纳状态

### agentscope-java

采纳状态：V3 普通 Chat 默认执行底座。

用途：

- 普通大模型对话。
- ReActAgent。
- MCP 调用。
- Skills 调用。
- 低风险 Tool。
- Memory。
- Session。
- StreamOptions。

约束：

- 平台优先写适配层和配置装配层。
- 不在平台侧自研 ReAct 循环。
- 不通过关键词规则绕过 AgentScope 触发工具。
- 运行时只能接收平台过滤后的 `RuntimeCapabilityPlan`。

### agentscope-runtime-java

采纳状态：V3 高风险沙箱执行底座。

用途：

- Shell。
- Python。
- Browser。
- 文件写入。
- 外部副作用工具。
- 沙箱执行。
- 未来 Agent-as-API / A2A PoC。

约束：

- 不作为普通 Chat 默认路径。
- 未配置时必须明确返回 unavailable。
- 所有执行必须带权限、审计、Run/Span。

### Hermes Agent

采纳状态：参考和占位，不作为 V3 默认执行底座。

参考方向：

- OpenAI-compatible chat API。
- Responses / conversation continuation。
- Runs API。
- Capability API。
- Health API。
- Run-first 事件流建模。
- AI Native 开发方法。

不采纳为 V3 主路径：

- 不用 Hermes Profiles 作为用户体系。
- 不让前端直接连接 Hermes。
- 不默认开放工具集。
- 不在 V3 引入自动 skill 创建、自修复、自主扩展能力。
- 不用 Hermes 替代 `agentscope-java` / `agentscope-runtime-java` 的执行职责。

## 2. 调研文档说明

`AgentScope-Java与AgentScope-Runtime-Java区别.md`

- 说明 `agentscope-java` 与 `agentscope-runtime-java` 的定位差异。
- 用于理解普通 Chat 与高风险沙箱执行的分工。

`AgentScope源码对比Hermes与V1V2开发复杂度评估.md`

- 基于本地源码对比 AgentScope、Hermes、V1/V2 功能差距。
- 用于判断哪些能力是简单适配，哪些需要平台工程，哪些应暂缓。

`HermesAgent替代AgentScope执行侧方案对比.md`

- 记录曾讨论过的 Hermes 替代 AgentScope 路线。
- 当前结论已经收敛为：Hermes 只参考和占位，不替代 AgentScope 执行侧。

## 3. 防误读说明

如果后续 AI agent 读到“用 Hermes Agent 替代 agentscope-java / agentscope-runtime-java”的旧讨论，应按当前 V3 结论处理：

```text
普通 Chat：agentscope-java
高风险执行：agentscope-runtime-java
Hermes：API / Run / Capability / AI Native 方法参考，runtime-hermes 仅占位
```

如需改变该路线，必须新增架构决策文档并经过人工评审。
