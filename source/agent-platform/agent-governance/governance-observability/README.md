# governance-observability

## 模块作用

可观测性治理模块，维护 trace 上下文和 trace 规划。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.governance.observability`
- 阅读入口：
  - `AgentTraceContext`
  - `TraceContextPlanner`

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先看 `com.htam.agent.governance.observability` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
