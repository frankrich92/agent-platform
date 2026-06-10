# governance-audit

## 模块作用

审计治理模块，负责审计事件、严重级别、台账实现和运行时审计投影。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 6 个 Java 源文件。
- 主要包：
  - `com.htam.agent.governance.audit`
- 阅读入口：
  - `AuditEvent`
  - `AuditLedger`
  - `AuditSeverity`
  - `InMemoryAuditLedger`
  - `JdbcAuditLedger`
  - `RuntimeAuditProjector`
- `src/test/java`：包含 2 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先看 `com.htam.agent.governance.audit` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
