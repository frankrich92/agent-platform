# boot-starter

## 模块作用

平台 starter 模块，为外部或后续集成方提供一站式依赖入口。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 1 个 Java 源文件。
- 主要包：
  - `com.htam.agent.boot.starter`
- 阅读入口：
  - `AgentPlatformStarter`

## 依赖边界

只做应用启动和模块装配，不承载具体业务规则。

## 阅读建议

先看 `com.htam.agent.boot.starter` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
