# worker-sandbox

## 模块作用

Worker 沙箱模块，按策略生成沙箱执行计划。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 3 个 Java 源文件。
- 主要包：
  - `com.htam.agent.worker.sandbox`
- 阅读入口：
  - `SandboxExecutionPlan`
  - `WorkerSandboxPlanner`
  - `WorkerSandboxPolicy`

## 依赖边界

Worker 通过 worker-spi 暴露任务协议，具体 CLI、文件和沙箱实现留在各适配子模块。

## 阅读建议

先看 `com.htam.agent.worker.sandbox` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
