# capability-core

## 模块作用

能力规划核心模块，定义能力项、能力计划、风险等级和默认规划服务。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 7 个 Java 源文件。
- 主要包：
  - `com.htam.agent.capability`
- 阅读入口：
  - `CapabilityItem`
  - `CapabilityKind`
  - `CapabilityPlan`
  - `CapabilityPlanService`
  - `CapabilityRiskLevel`
  - `CapabilityRiskPolicy`
  - `DefaultCapabilityPlanService`

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先看 `com.htam.agent.capability` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
