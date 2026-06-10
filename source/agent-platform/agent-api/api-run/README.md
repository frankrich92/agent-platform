# api-run

## 模块作用

运行域 API 契约模块，提供会话、消息和分页查询 DTO/VO。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 6 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.dto`
  - `com.htam.agent.common.vo`
- 阅读入口：
  - `ChatMessageAppendDTO`
  - `ChatSessionCreateDTO`
  - `ChatSessionQueryDTO`
  - `ChatMessagePageVO`
  - `ChatMessageVO`
  - `ChatSessionVO`

## 依赖边界

只承载契约对象，不放 Controller、Service 实现或具体运行时 SDK 类型。

## 阅读建议

先看 `com.htam.agent.common.dto` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
