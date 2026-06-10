# boot-autoconfigure

## 模块作用

自动配置模块，封装异步线程、Jackson、运行收尾等 Spring Boot 装配。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 3 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.config`
  - `com.htam.agent.common.run`
- 阅读入口：
  - `AsyncConfig`
  - `JacksonConfig`
  - `RunAndEnd`

## 依赖边界

只做应用启动和模块装配，不承载具体业务规则。

## 阅读建议

先看 `com.htam.agent.common.config` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
