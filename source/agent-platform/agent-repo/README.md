# agent-repo

## 模块作用

仓储聚合层，拆分仓储 SPI、MyBatis 实现、缓存、向量存储和迁移执行能力。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `repo-spi`
  - `repo-mybatis`
  - `repo-vector`
  - `repo-cache`
  - `repo-migration`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

业务模块依赖 repo-spi，具体 MyBatis、缓存或向量实现由装配层选择。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
