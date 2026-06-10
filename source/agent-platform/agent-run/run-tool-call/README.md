# run-tool-call

## 模块作用

工具调用台账模块，提供内存和 JDBC 工具调用记录。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.run.toolcall`
- 阅读入口：
  - `InMemoryToolCallLedger`
  - `JdbcToolCallLedger`

## 依赖边界

Run 模块记录运行过程和任务状态，具体执行交给 runtime/worker，持久化交给 repo。

## 阅读建议

先看 `com.htam.agent.run.toolcall` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
