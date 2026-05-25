# AgentScope Java 与 AgentScope Runtime Java 区别调研

## 结论

`agentscope-java` 与 `agentscope-runtime-java` 不是包含关系。`agentscope-java` 是 Agent 开发框架 / SDK，负责 Agent 如何思考、调用模型、使用工具和产生流式事件；`agentscope-runtime-java` 是 Agent 部署运行时 / 服务化基础设施，负责 Agent 如何被托管、暴露成服务、隔离工具执行、管理会话和沙箱。

因此，`AgentScope Java` 框架没有完整包括 `agentscope-runtime-java` 的内容。两者有一些概念重叠，例如 Session、Memory、Tool，但抽象层次和职责边界不同。

## 核心差异

| 维度 | `agentscope-java` | `agentscope-runtime-java` |
| --- | --- | --- |
| 定位 | Java 版 Agent 编程框架 / SDK | Java 版 Agent Runtime / 部署框架 |
| 主要用途 | 构建 Agent：`ReActAgent`、模型调用、工具、Skills、Memory、Session、流式事件等 | 运行和托管 Agent：Agent-as-API、A2A 服务、会话 / 记忆服务化、沙箱管理、安全工具执行等 |
| 类比 | Agent 应用内核 | Agent 托管平台 / 运行容器 |
| 框架关系 | 自身就是 AgentScope Java 框架 | 框架无关，可集成 AgentScope Java、Spring AI Alibaba、LangChain4j 等 |
| 工具执行 | 关注 Agent 内部工具调用链路 | 更强调沙箱隔离和受控执行 |
| Session / Memory | Agent 内部能力 | 服务化运行时能力 |
| API 服务 | 可通过 starter 或应用代码接入 Web 服务 | 核心目标之一 |
| 部署能力 | 由业务应用自行负责 | Runtime 提供更外层部署基础设施 |

## 关系理解

可以简化理解为：

```text
agentscope-java
= 写 Agent 的 SDK / 框架

agentscope-runtime-java
= 托管、暴露、隔离、运行 Agent 的服务层
```

`agentscope-runtime-java` 可以集成 `agentscope-java` 写出的 Agent，但 `agentscope-java` 本身不等于 runtime，也不内置完整的 runtime 服务化、沙箱和托管能力。

## 对当前项目的影响

当前仓库的 V1 / V2 平台实现更直接相关的是 `agentscope-java` / `agentscope-core`：

- 平台需要优先复用 `agentscope-java` 的 `ReActAgent`、`Toolkit`、`SkillBox`、MCP tool、Memory、Session、`StreamOptions` 和事件体系。
- 平台自身主要承担用户管理、Agent 授权、用户隔离、记忆数据治理、能力配置、审计观测、Run / Span 记录、SSE 协议适配和前端体验。
- 对 Agent 编排、ReAct 循环、工具调用、MCP 调用、Skills 加载、会话记忆执行引擎或流式事件引擎，不应在平台侧默认自研。

当前继续以 `agentscope-java` / `agentscope-core` 作为执行内核是合理的。后续如果平台需要标准化 Agent 服务托管、Agent-as-API、A2A 协议暴露、工具执行沙箱、跨框架运行时接入等能力，再单独评估引入 `agentscope-runtime-java`。

## 建议

1. V1 / V2 当前开发继续使用 `agentscope-java` / `agentscope-core` 作为 Agent 执行层。
2. 平台侧只做适配层和配置装配层，不复制 AgentScope Java 已提供的执行逻辑。
3. 暂不把 `agentscope-runtime-java` 作为 `agentscope-java` 的替代品。
4. 当出现以下需求时，再开启 `agentscope-runtime-java` 专项调研：
   - Agent 需要统一暴露为标准 API 或 A2A 服务。
   - 工具执行需要独立沙箱和更强的安全隔离。
   - Agent 需要跨框架统一托管，例如同时接入 AgentScope Java、Spring AI Alibaba、LangChain4j。
   - 会话、记忆、运行状态需要从应用内部能力提升为独立运行时服务。

## 参考

- `agentscope-java`: <https://github.com/agentscope-ai/agentscope-java>
- `agentscope-runtime-java`: <https://github.com/agentscope-ai/agentscope-runtime-java>
- AgentScope Runtime 文档: <https://runtime.agentscope.io/en/intro.html>
