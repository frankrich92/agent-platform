# agent-runtime

## 模块作用

Agent 运行时聚合层，定义运行时 SPI，并接入 AgentScope、Hermes、LangGraph、上下文、记忆和规划能力。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `runtime-spi`
  - `runtime-core`
  - `runtime-context`
  - `runtime-agentscope`
  - `runtime-memory`
  - `runtime-planning`
  - `runtime-hermes`
  - `runtime-langgraph`
  - `runtime-testkit`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

业务侧通过 runtime-spi 访问运行时，具体 AgentScope/Hermes/LangGraph 类型应限制在适配模块内。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
