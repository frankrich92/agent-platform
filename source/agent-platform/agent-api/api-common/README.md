# api-common

## 模块作用

通用 API 契约模块，提供统一响应、错误码和业务异常类型。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 7 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.exception`
  - `com.htam.agent.common.r`
- 阅读入口：
  - `BaseException`
  - `BusinessException`
  - `NotAuthException`
  - `RoleNeedException`
  - `IResultCode`
  - `R`
  - `ResultCode`

## 依赖边界

只承载契约对象，不放 Controller、Service 实现或具体运行时 SDK 类型。

## 阅读建议

先看 `com.htam.agent.common.exception` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
