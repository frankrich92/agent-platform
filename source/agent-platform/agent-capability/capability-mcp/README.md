# capability-mcp

## 模块作用

MCP 能力模块，管理 MCP Server/Tool、Agent 绑定、暴露计划、运行降级和审计事件。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 17 个 Java 源文件。
- 主要包：
  - `com.htam.agent.capability.mcp`
  - `com.htam.agent.capability.mcp.service`
  - `com.htam.agent.capability.mcp.service.impl`
- 阅读入口：
  - `AgentMcpServerExposure`
  - `McpCallAuditEvent`
  - `McpExtensionPlan`
  - `McpExtensionPlanner`
  - `McpPromptDescriptor`
  - `McpResourceDescriptor`
  - `McpServerBinding`
  - `AgentMcpServerService`
  - `AgentMcpToolService`
  - `McpServerService`
  - 其余 7 项见源码目录。
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先看 `com.htam.agent.capability.mcp` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
