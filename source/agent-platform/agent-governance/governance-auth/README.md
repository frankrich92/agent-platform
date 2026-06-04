# governance-auth

## 模块作用

认证与系统参数模块，负责请求鉴权、Token、ChatKey/SK 访问、路径重写和参数适配。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 28 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.config`
  - `com.htam.agent.common.config.auth`
  - `com.htam.agent.common.message`
  - `com.htam.agent.common.util`
  - `com.htam.agent.governance.system.params.core`
  - `com.htam.agent.governance.system.params.core.impl`
  - `com.htam.agent.governance.system.params.service`
- 阅读入口：
  - `AgentSpringContextHolder`
  - `ApiPathRewriteFilter`
  - `AuthInterceptor`
  - `ChatKeyAccess`
  - `InterceptorConfig`
  - `PassAuth`
  - `RoleNeed`
  - `SkAccess`
  - `SkIdSyncPublisher`
  - `SkIdSyncSubscriber`
  - 其余 18 项见源码目录。
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先看 `com.htam.agent.common.config` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
