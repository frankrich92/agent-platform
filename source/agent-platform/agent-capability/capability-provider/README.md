# capability-provider

## 模块作用

模型供应商能力模块，管理模型供应商和模型配置。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 4 个 Java 源文件。
- 主要包：
  - `com.htam.agent.capability.provider.service`
  - `com.htam.agent.capability.provider.service.impl`
- 阅读入口：
  - `ModelConfigService`
  - `ModelProviderService`
  - `ModelConfigServiceImpl`
  - `ModelProviderServiceImpl`

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先看 `com.htam.agent.capability.provider.service` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
