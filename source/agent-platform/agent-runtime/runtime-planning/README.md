# runtime-planning

## 模块作用

运行时规划模块，负责失败恢复和循环检测等执行规划。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 5 个 Java 源文件。
- 主要包：
  - `com.htam.agent.runtime.planning`
- 阅读入口：
  - `DoomLoopDecision`
  - `DoomLoopDetector`
  - `FailureRecoveryAction`
  - `FailureRecoveryPlan`
  - `FailureRecoveryPlanner`

## 依赖边界

业务侧通过 runtime-spi 访问运行时，具体 AgentScope/Hermes/LangGraph 类型应限制在适配模块内。

## 阅读建议

先看 `com.htam.agent.runtime.planning` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
