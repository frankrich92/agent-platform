# domain-workflow

## 模块作用

工作流领域模块，承载工作流状态引用等轻量领域对象。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 1 个 Java 源文件。
- 主要包：
  - `com.htam.agent.workflow.domain`
- 阅读入口：
  - `WorkflowStateRef`

## 依赖边界

保持领域对象轻量，避免把 Web、具体运行时或数据库实现细节继续向上泄漏。

## 阅读建议

先看 `com.htam.agent.workflow.domain` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
