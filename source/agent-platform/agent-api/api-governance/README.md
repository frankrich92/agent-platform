# api-governance

## 模块作用

治理域 API 契约模块，提供账号、登录、密钥、敏感词和 WebSocket 用户对象。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 12 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.dto`
  - `com.htam.agent.common.vo`
- 阅读入口：
  - `AccountDTO`
  - `ChangePasswordRequest`
  - `LoginRequest`
  - `LoginResponse`
  - `RefreshTokenRequest`
  - `RegisterRequest`
  - `SensitiveWordConfigDTO`
  - `UpdateProfileRequest`
  - `AccountVO`
  - `SecretKeyVo`
  - 其余 2 项见源码目录。

## 依赖边界

只承载契约对象，不放 Controller、Service 实现或具体运行时 SDK 类型。

## 阅读建议

先看 `com.htam.agent.common.dto` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
