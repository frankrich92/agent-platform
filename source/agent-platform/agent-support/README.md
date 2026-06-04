# agent-support

## 模块作用

跨模块支撑库，放置不绑定具体业务域的 Spring 或通用辅助能力。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `support-spring`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

只放跨模块通用支撑，避免形成新的重型 common 模块。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
