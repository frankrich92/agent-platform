# boot-app

## 模块作用

Spring Boot 可运行应用模块，聚合所有后端能力并暴露启动入口和配置文件。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 1 个 Java 源文件。
- 主要包：
  - `com.htam.agent`
- 阅读入口：
  - `Application`
- `src/main/resources`：
  - `src/main/resources/application-dev.yml`
  - `src/main/resources/application.yml`
  - `src/main/resources/db/migration/V1__init_schema.sql`
  - `src/main/resources/db/migration/V2__normalize_mcp_protocol_config.sql`
  - `src/main/resources/db/migration/V3__repair_escaped_mcp_protocol_config.sql`
  - `src/main/resources/db/migration/V4__normalize_tool_need_confirm.sql`
  - `src/main/resources/db/migration/V5__run_ledger_schema.sql`
  - `src/main/resources/db/migration/V6__run_event_idempotency.sql`
  - 其余 2 项见源码目录。
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

只做应用启动和模块装配，不承载具体业务规则。

## 阅读建议

先看 `com.htam.agent` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
