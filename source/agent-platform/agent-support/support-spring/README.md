# support-spring

## 模块作用

Spring 支撑模块，提供跨模块复用的 Spring Bean 工具。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 1 个 Java 源文件。
- 主要包：
  - `com.htam.agent.common.util`
- 阅读入口：
  - `BeanUtils`

## 依赖边界

只放跨模块通用支撑，避免形成新的重型 common 模块。

## 阅读建议

先看 `com.htam.agent.common.util` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
