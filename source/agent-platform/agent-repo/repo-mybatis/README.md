# repo-mybatis

## 模块作用

MyBatis 仓储实现模块，承接数据库配置、Mapper、MyBatis Plus 支持和各领域 repository adapter。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 101 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.config.db`
  - `com.htam.agent.common.config.mybatis`
  - `com.htam.agent.common.mp.support`
  - `com.htam.agent.repo.mybatis.agent`
  - `com.htam.agent.repo.mybatis.agent.mapper`
  - `com.htam.agent.repo.mybatis.capability`
  - `com.htam.agent.repo.mybatis.capability.mapper`
  - `com.htam.agent.repo.mybatis.config`
  - 其余 15 项见源码目录。
- 阅读入口：
  - `ConversationDataSourceConfig`
  - `DynamicPostgreSqlDataSourceConfig`
  - `PostgreSqlDataSourceConfig`
  - `InterceptorFactory`
  - `MyMetaObjectHandler`
  - `MybatisPlusConfig`
  - `ConditionBuilder`
  - `MP`
  - `AgentA2aMybatisRepository`
  - `AgentChatKeyMybatisRepository`
  - 其余 91 项见源码目录。
- `src/main/resources`：
  - `src/main/resources/com/htam/agent/repo/mybatis/agent/mapper/ChatMessageMapper.xml`
  - `src/main/resources/com/htam/agent/repo/mybatis/agent/mapper/ChatSessionMapper.xml`
- `src/test/java`：包含 6 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

业务模块依赖 repo-spi，具体 MyBatis、缓存或向量实现由装配层选择。

## 阅读建议

先看 `com.htam.agent.common.config.db` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
