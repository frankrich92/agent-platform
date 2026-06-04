# run-message

## 模块作用

运行消息模块，维护消息台账和聊天消息服务。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 6 个 Java 源文件。
- 主要包：
  - `com.htam.agent.run.message`
  - `com.htam.agent.run.message.service`
  - `com.htam.agent.run.message.service.impl`
- 阅读入口：
  - `InMemoryRunMessageLedger`
  - `RunMessageLedger`
  - `RunMessageRecord`
  - `RunMessageRole`
  - `ChatMessageService`
  - `ChatMessageServiceImpl`

## 依赖边界

Run 模块记录运行过程和任务状态，具体执行交给 runtime/worker，持久化交给 repo。

## 阅读建议

先看 `com.htam.agent.run.message` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
