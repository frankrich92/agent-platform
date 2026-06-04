# profile-model-policy

## 模块作用

模型策略模块，描述模型选择策略计划和规划器。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.profile.modelpolicy`
- 阅读入口：
  - `ModelPolicyPlan`
  - `ModelPolicyPlanner`

## 依赖边界

Profile 模块聚合智能体配置态，不直接暴露 AgentScope 等具体运行时对象。

## 阅读建议

先看 `com.htam.agent.profile.modelpolicy` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
