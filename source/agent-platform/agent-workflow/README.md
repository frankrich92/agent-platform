# agent-workflow

## 模块作用

工作流聚合层，描述工作流定义、节点执行、事件、人审任务和运行态恢复。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `workflow-core`
  - `workflow-definition`
  - `workflow-engine`
  - `workflow-node`
  - `workflow-human-task`
  - `workflow-event`
  - `workflow-runtime`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

Workflow 模块描述编排和状态流转，节点具体能力通过 run/runtime/worker/capability 接入。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
