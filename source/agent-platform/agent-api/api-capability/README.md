# api-capability

## 模块作用

能力域 API 契约模块，提供模型、工具、MCP、Skill、知识库和 RAG 的 DTO/VO。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 20 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.dto`
  - `com.htam.agent.common.vo`
- 阅读入口：
  - `HookConfigDTO`
  - `KnowledgeBaseConfigDTO`
  - `McpServerDTO`
  - `McpToolEnabledDTO`
  - `ModelConfigDTO`
  - `ModelProviderDTO`
  - `SkillPackageDTO`
  - `ToolDTO`
  - `CheckModelResult`
  - `HookConfigVO`
  - 其余 10 项见源码目录。

## 依赖边界

只承载契约对象，不放 Controller、Service 实现或具体运行时 SDK 类型。

## 阅读建议

先看 `com.htam.agent.common.dto` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
