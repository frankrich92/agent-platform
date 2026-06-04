# runtime-core

## 模块作用

运行时核心装饰模块，负责运行治理包装和交互轨迹记录。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 3 个 Java 源文件。
- 主要包：
  - `com.htam.agent.runtime.core`
- 阅读入口：
  - `GovernedRuntimeRunner`
  - `RuntimeInteractionRecorder`
  - `RuntimeInteractionTrace`
- `src/test/java`：包含 2 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

业务侧通过 runtime-spi 访问运行时，具体 AgentScope/Hermes/LangGraph 类型应限制在适配模块内。

## 阅读建议

先看 `com.htam.agent.runtime.core` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
