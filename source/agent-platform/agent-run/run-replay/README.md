# run-replay

## 模块作用

运行回放模块，按运行事件生成可回放帧和回放计划。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 3 个 Java 源文件。
- 主要包：
  - `com.htam.agent.run.replay`
- 阅读入口：
  - `RunReplayFrame`
  - `RunReplayPlan`
  - `RunReplayService`
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Run 模块记录运行过程和任务状态，具体执行交给 runtime/worker，持久化交给 repo。

## 阅读建议

先看 `com.htam.agent.run.replay` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
