# adapter-agui

## 模块作用

AGUI/SSE 适配模块，把运行事件投影到 AGUI 协议，并提供兼容 AgentScope AGUI 控制器的接入点。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 4 个 Java 源文件。
- 主要包：
  - `com.htam.agent.adapter.agui`
  - `com.htam.agent.adapter.agui.run`
  - `io.agentscope.spring.boot.agui.mvc`
- 阅读入口：
  - `AguiRestControllerConfig`
  - `AguiRunLedgerProjector`
  - `UserUtilsAguiRequestUserProvider`
  - `AguiRestController`
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

适配器负责协议转换和参数校验，业务规则应下沉到 profile/run/capability/governance/worker 等模块。

## 阅读建议

先看 `com.htam.agent.adapter.agui` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
