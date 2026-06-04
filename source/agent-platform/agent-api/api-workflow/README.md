# api-workflow

## 模块作用

工作流 API 契约模块，提供工作流运行请求对象。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 1 个 Java 源文件。
- 主要包：
  - `com.htam.agent.workflow.api`
- 阅读入口：
  - `WorkflowRunDTO`

## 依赖边界

只承载契约对象，不放 Controller、Service 实现或具体运行时 SDK 类型。

## 阅读建议

先看 `com.htam.agent.workflow.api` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
