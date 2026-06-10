# agent-run

## 模块作用

运行记录业务层，负责一次 Agent 执行的会话、消息、事件、步骤、工具调用、任务和回放。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `run-event`
  - `run-message`
  - `run-session`
  - `run-core`
  - `run-step`
  - `run-tool-call`
  - `run-history`
  - `run-replay`
  - `run-task`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

Run 模块记录运行过程和任务状态，具体执行交给 runtime/worker，持久化交给 repo。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
