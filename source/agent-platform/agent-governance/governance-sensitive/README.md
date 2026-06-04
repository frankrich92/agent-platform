# governance-sensitive

## 模块作用

敏感词治理模块，维护敏感词配置服务。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 2 个 Java 源文件。
- 主要包：
  - `com.htam.agent.governance.sensitive.service`
  - `com.htam.agent.governance.sensitive.service.impl`
- 阅读入口：
  - `SensitiveWordConfigService`
  - `SensitiveWordConfigServiceImpl`

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先看 `com.htam.agent.governance.sensitive.service` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
