# worker-spi

## 模块作用

Worker SPI 模块，定义 WorkerRuntime、任务、状态、类型、风险策略和规划接口。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 7 个 Java 源文件。
- 主要包：
  - `com.htam.agent.worker.spi`
- 阅读入口：
  - `WorkerRiskPolicy`
  - `WorkerRuntime`
  - `WorkerTask`
  - `WorkerTaskPlanner`
  - `WorkerTaskResult`
  - `WorkerTaskStatus`
  - `WorkerTaskType`

## 依赖边界

Worker 通过 worker-spi 暴露任务协议，具体 CLI、文件和沙箱实现留在各适配子模块。

## 阅读建议

先看 `com.htam.agent.worker.spi` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
