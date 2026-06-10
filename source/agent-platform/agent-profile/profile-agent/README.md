# profile-agent

## 模块作用

Agent Profile 主业务模块，负责编排智能体定义、ChatKey、子 Agent、A2A 和能力计划贡献器。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 23 个 Java 源文件。
- 主要包：
  - `com.htam.agent.profile.a2a.service`
  - `com.htam.agent.profile.agent`
  - `com.htam.agent.profile.agent.service`
  - `com.htam.agent.profile.agent.service.impl`
- 阅读入口：
  - `AgentA2aService`
  - `AgentA2aServiceImpl`
  - `ChatKeyInit`
  - `AgentChatKeyService`
  - `AgentDefinitionService`
  - `AgentStatisticsService`
  - `AgentSubAgentService`
  - `AgentChatKeyServiceImpl`
  - `AgentDefinitionServiceImpl`
  - `AgentStatisticsServiceImpl`
  - 其余 13 项见源码目录。
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Profile 模块聚合智能体配置态，不直接暴露 AgentScope 等具体运行时对象。

## 阅读建议

先看 `com.htam.agent.profile.a2a.service` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
