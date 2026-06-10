# run-session

## 模块作用

运行会话模块，维护会话台账、状态、锁和聊天会话服务。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 7 个 Java 源文件。
- 主要包：
  - `com.htam.agent.run.session`
  - `com.htam.agent.run.session.lock`
  - `com.htam.agent.run.session.service`
  - `com.htam.agent.run.session.service.impl`
- 阅读入口：
  - `InMemoryRunSessionLedger`
  - `RunSessionLedger`
  - `RunSessionRecord`
  - `RunSessionStatus`
  - `SessionLockManager`
  - `ChatSessionService`
  - `ChatSessionServiceImpl`

## 依赖边界

Run 模块记录运行过程和任务状态，具体执行交给 runtime/worker，持久化交给 repo。

## 阅读建议

先看 `com.htam.agent.run.session` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
