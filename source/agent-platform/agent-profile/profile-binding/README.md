# profile-binding

## 模块作用

Profile 能力绑定模型模块，描述 Profile 与不同能力类型之间的绑定目录。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 3 个 Java 源文件。
- 主要包：
  - `com.htam.agent.profile.binding`
- 阅读入口：
  - `ProfileBindingCatalog`
  - `ProfileBindingType`
  - `ProfileCapabilityBinding`
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Profile 模块聚合智能体配置态，不直接暴露 AgentScope 等具体运行时对象。

## 阅读建议

先看 `com.htam.agent.profile.binding` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
