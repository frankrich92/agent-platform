# worker-core

## 模块作用

Worker 核心模块，负责任务协调、租约、registry、远程派发和代码执行配置服务。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 13 个 Java 源文件。
- 主要包：
  - `com.htam.agent.worker.code.service`
  - `com.htam.agent.worker.code.service.impl`
  - `com.htam.agent.worker.core`
- 阅读入口：
  - `AgentCodeExecutionService`
  - `CodeExecutionConfigService`
  - `AgentCodeExecutionServiceImpl`
  - `CodeExecutionConfigServiceImpl`
  - `GovernedWorkerRuntime`
  - `InMemoryWorkerRegistry`
  - `InMemoryWorkerTaskLedger`
  - `RemoteWorkerDispatcher`
  - `WorkerExecutionCoordinator`
  - `WorkerExecutionLease`
  - 其余 3 项见源码目录。
- `src/test/java`：包含 3 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Worker 通过 worker-spi 暴露任务协议，具体 CLI、文件和沙箱实现留在各适配子模块。

## 阅读建议

先看 `com.htam.agent.worker.code.service` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
