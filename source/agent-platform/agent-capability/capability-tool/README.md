# capability-tool

## 模块作用

工具与 Hook 能力模块，负责工具配置、Agent 工具绑定、Hook 生命周期和工具执行治理计划。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 19 个 Java 源文件。
- 主要包：
  - `com.htam.agent.capability.tool`
  - `com.htam.agent.capability.tool.hook.service`
  - `com.htam.agent.capability.tool.hook.service.impl`
  - `com.htam.agent.capability.tool.service`
  - `com.htam.agent.capability.tool.service.impl`
- 阅读入口：
  - `GovernedToolExecutionDecision`
  - `GovernedToolExecutionPlanner`
  - `HookLifecyclePhase`
  - `HookPolicy`
  - `ToolExecutionDecision`
  - `ToolExecutionOptions`
  - `ToolExecutionPlanner`
  - `ToolParallelExecutionPlan`
  - `ToolParallelExecutionPlanner`
  - `ToolResultSummary`
  - 其余 9 项见源码目录。
- `src/test/java`：包含 2 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先看 `com.htam.agent.capability.tool` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
