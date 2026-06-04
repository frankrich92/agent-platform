# repo-migration

## 模块作用

迁移执行模块，抽象迁移步骤、迁移历史和 SQL 资源迁移处理器。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 8 个 Java 源文件。
- 主要包：
  - `com.htam.agent.repo.migration`
- 阅读入口：
  - `InMemoryMigrationHistory`
  - `JdbcMigrationHistory`
  - `MigrationExecutor`
  - `MigrationHistory`
  - `MigrationPlan`
  - `MigrationStep`
  - `MigrationStepHandler`
  - `SqlResourceMigrationStepHandler`
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

业务模块依赖 repo-spi，具体 MyBatis、缓存或向量实现由装配层选择。

## 阅读建议

先看 `com.htam.agent.repo.migration` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
