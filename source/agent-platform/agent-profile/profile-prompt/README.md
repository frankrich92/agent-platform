# profile-prompt

## 模块作用

提示词模板模块，维护系统提示词模板服务。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.profile.prompt.service`
  - `com.htam.agent.profile.prompt.service.impl`
- 阅读入口：
  - `SystemPromptTemplateService`
  - `SystemPromptTemplateServiceImpl`

## 依赖边界

Profile 模块聚合智能体配置态，不直接暴露 AgentScope 等具体运行时对象。

## 阅读建议

先看 `com.htam.agent.profile.prompt.service` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
