# domain-common

## 模块作用

通用领域模型模块，承载基础实体、枚举、常量、分页辅助、消息和运行态轻量对象。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 65 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common`
  - `com.htam.agent.common.config`
  - `com.htam.agent.common.consts`
  - `com.htam.agent.common.entity`
  - `com.htam.agent.common.enums`
  - `com.htam.agent.common.key`
  - `com.htam.agent.common.message`
  - `com.htam.agent.common.mp.annotation`
  - 其余 5 项见源码目录。
- 阅读入口：
  - `KvMap`
  - `UserDetail`
  - `SerializableEnable`
  - `DataSourceConst`
  - `RedisChannelTopic`
  - `SysConst`
  - `TableConst`
  - `BaseEntity`
  - `A2aType`
  - `AgentType`
  - 其余 55 项见源码目录。
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

保持领域对象轻量，避免把 Web、具体运行时或数据库实现细节继续向上泄漏。

## 阅读建议

先看 `com.htam.agent.common` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
