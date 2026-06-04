# runtime-spi

## 模块作用

运行时 SPI 模块，定义业务侧调用运行时所需的请求、结果、事件、工具调用和能力装配接口。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 22 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.file`
  - `com.htam.agent.common.knowledge`
  - `com.htam.agent.common.mcp`
  - `com.htam.agent.runtime`
- 阅读入口：
  - `AttachmentContentReader`
  - `KnowledgeRetrievalService`
  - `McpRuntimeDegradeRecorder`
  - `ToolSchemaRefreshResult`
  - `ToolSchemaRefresher`
  - `AgentRunRequest`
  - `AgentRunResult`
  - `AgentRunStatus`
  - `AgentRuntimeRunner`
  - `AgentRuntimeSessionService`
  - 其余 12 项见源码目录。

## 依赖边界

业务侧通过 runtime-spi 访问运行时，具体 AgentScope/Hermes/LangGraph 类型应限制在适配模块内。

## 阅读建议

先看 `com.htam.agent.common.file` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
