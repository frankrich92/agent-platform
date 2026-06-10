# runtime-agentscope

## 模块作用

AgentScope 运行时适配模块，负责 Agent 装配、AGUI、模型、工具、MCP、Hook、RAG、记忆和运行事件映射。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 101 个 Java 源文件。
- 主要包：
  - `com.htam.agent.runtime.agentscope`
  - `com.htam.agent.runtime.agentscope.agent`
  - `com.htam.agent.runtime.agentscope.agent.config`
  - `com.htam.agent.runtime.agentscope.agui`
  - `com.htam.agent.runtime.agentscope.attachment`
  - `com.htam.agent.runtime.agentscope.endpoint`
  - `com.htam.agent.runtime.agentscope.formatter`
  - `com.htam.agent.runtime.agentscope.hook`
  - 其余 34 项见源码目录。
- 阅读入口：
  - `AgentScopeCapabilityAssembler`
  - `AgentSessionConfig`
  - `ClearAgentMetadataStore`
  - `InstanceLoader`
  - `PostgresSession`
  - `A2aAgentHelper`
  - `IAgentFactory`
  - `ReActAgentHelper`
  - `NacosAgentConfig`
  - `WellKnownAgentConfig`
  - 其余 91 项见源码目录。
- `src/test/java`：包含 4 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

业务侧通过 runtime-spi 访问运行时，具体 AgentScope/Hermes/LangGraph 类型应限制在适配模块内。

## 阅读建议

先看 `com.htam.agent.runtime.agentscope` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
