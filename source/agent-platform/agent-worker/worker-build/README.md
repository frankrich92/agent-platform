# worker-build

## 模块作用

构建测试 Worker 模块，规划构建与测试命令。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.worker.build`
- 阅读入口：
  - `BuildTestPlan`
  - `BuildTestPlanner`

## 依赖边界

Worker 通过 worker-spi 暴露任务协议，具体 CLI、文件和沙箱实现留在各适配子模块。

## 阅读建议

先看 `com.htam.agent.worker.build` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
