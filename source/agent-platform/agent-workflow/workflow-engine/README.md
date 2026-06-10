# workflow-engine

## 模块作用

工作流编排模块，负责子 Agent handoff 和 Worker 选择计划。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 7 个 Java 源文件。
- 主要包：
  - `com.htam.agent.workflow`
- 阅读入口：
  - `SubAgentHandoffPlan`
  - `SubAgentHandoffPlanner`
  - `SubAgentHandoffRequest`
  - `WorkflowWorkerPlan`
  - `WorkflowWorkerSelection`
  - `WorkflowWorkerSelector`
  - `WorkflowWorkerType`
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

Workflow 模块描述编排和状态流转，节点具体能力通过 run/runtime/worker/capability 接入。

## 阅读建议

先看 `com.htam.agent.workflow` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
