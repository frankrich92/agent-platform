# domain-capability

## 模块作用

能力域实体模块，承载模型、工具、MCP、Skill、知识库、RAG 文档等数据库实体。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 14 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.entity`
  - `com.htam.agent.common.skill`
  - `com.htam.agent.common.wrapper`
- 阅读入口：
  - `HookConfig`
  - `KnowledgeBaseConfig`
  - `McpServer`
  - `McpTool`
  - `ModelConfig`
  - `ModelProvider`
  - `RagDocument`
  - `RagDocumentChunk`
  - `SkillFile`
  - `SkillPackage`
  - 其余 4 项见源码目录。

## 依赖边界

保持领域对象轻量，避免把 Web、具体运行时或数据库实现细节继续向上泄漏。

## 阅读建议

先看 `com.htam.agent.common.entity` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
