# workflow-runtime

## 模块作用

工作流运行时模块，负责启动、节点记录、人审完成、运行状态存储和恢复。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 9 个 Java 源文件。
- 主要包：
  - `com.htam.agent.workflow.runtime`
- 阅读入口：
  - `InMemoryWorkflowRuntime`
  - `InMemoryWorkflowStateRepository`
  - `WorkflowHumanTaskBinding`
  - `WorkflowNodeExecution`
  - `WorkflowRecoveryService`
  - `WorkflowRun`
  - `WorkflowRunStatus`
  - `WorkflowRuntime`
  - `WorkflowStateRepository`
- `src/test/java`：包含 2 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Workflow 模块描述编排和状态流转，节点具体能力通过 run/runtime/worker/capability 接入。

## 阅读建议

先看 `com.htam.agent.workflow.runtime` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
