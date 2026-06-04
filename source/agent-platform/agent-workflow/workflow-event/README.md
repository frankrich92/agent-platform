# workflow-event

## 模块作用

工作流事件模块，维护事件类型、事件对象和事件 sink。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 4 个 Java 源文件。
- 主要包：
  - `com.htam.agent.workflow.event`
- 阅读入口：
  - `InMemoryWorkflowEventSink`
  - `WorkflowEvent`
  - `WorkflowEventSink`
  - `WorkflowEventType`

## 依赖边界

Workflow 模块描述编排和状态流转，节点具体能力通过 run/runtime/worker/capability 接入。

## 阅读建议

先看 `com.htam.agent.workflow.event` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
