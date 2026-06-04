# run-core

## 模块作用

运行核心模块，负责 AgentRun 命令、摘要、启动治理、运行台账和异步写入队列。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 10 个 Java 源文件。
- 主要包：
  - `com.htam.agent.run`
- 阅读入口：
  - `AgentRunCommand`
  - `AgentRunLedgerRecorder`
  - `AgentRunLedgerWriteQueue`
  - `AgentRunService`
  - `AgentRunSummary`
  - `DefaultAgentRunLedgerWriteQueue`
  - `DefaultAgentRunService`
  - `GovernedRunStartDecision`
  - `GovernedRunStartPlanner`
  - `JdbcAgentRunLedger`
- `src/test/java`：包含 4 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Run 模块记录运行过程和任务状态，具体执行交给 runtime/worker，持久化交给 repo。

## 阅读建议

先看 `com.htam.agent.run` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
