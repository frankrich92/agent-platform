# profile-core

## 模块作用

Profile 核心规划模块，描述 Profile 变更治理决策和配置描述符。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 3 个 Java 源文件。
- 主要包：
  - `com.htam.agent.profile.core`
- 阅读入口：
  - `GovernedProfileChangeDecision`
  - `GovernedProfileChangePlanner`
  - `ProfileDescriptor`
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Profile 模块聚合智能体配置态，不直接暴露 AgentScope 等具体运行时对象。

## 阅读建议

先看 `com.htam.agent.profile.core` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
