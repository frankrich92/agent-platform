# agent-boot

## 模块作用

启动装配层，提供 Spring Boot 应用入口、自动配置和 starter。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `boot-app`
  - `boot-autoconfigure`
  - `boot-starter`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

只做应用启动和模块装配，不承载具体业务规则。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
