# repo-spi

## 模块作用

仓储 SPI 模块，定义业务服务依赖的 repository 接口和分页/向量等基础仓储契约。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 58 个 Java 源文件。
- 主要包：
  - `com.htam.agent.repo.agent`
  - `com.htam.agent.repo.cache`
  - `com.htam.agent.repo.capability`
  - `com.htam.agent.repo.file`
  - `com.htam.agent.repo.iam`
  - `com.htam.agent.repo.knowledge`
  - `com.htam.agent.repo.provider`
  - `com.htam.agent.repo.support`
  - 其余 7 项见源码目录。
- 阅读入口：
  - `AgentA2aRepository`
  - `AgentChatKeyRepository`
  - `AgentCodeExecutionRepository`
  - `AgentDefinitionRepository`
  - `AgentScopeSessionRepository`
  - `AgentStudioRepository`
  - `AgentSubAgentRepository`
  - `ChatMessageRepository`
  - `ChatSessionRepository`
  - `CodeExecutionConfigRepository`
  - 其余 48 项见源码目录。

## 依赖边界

业务模块依赖 repo-spi，具体 MyBatis、缓存或向量实现由装配层选择。

## 阅读建议

先看 `com.htam.agent.repo.agent` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
