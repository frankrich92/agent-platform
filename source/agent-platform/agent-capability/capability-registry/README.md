# capability-registry

## 模块作用

能力注册表模块，提供内存目录用于登记和查询平台能力。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.capability.registry`
- 阅读入口：
  - `CapabilityRegistry`
  - `InMemoryCapabilityRegistry`

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先看 `com.htam.agent.capability.registry` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
