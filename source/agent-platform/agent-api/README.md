# agent-api

## 模块作用

接口契约聚合层，承载 REST/内部调用复用的 DTO、VO、请求响应对象和轻量 facade 契约。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `api-common`
  - `api-profile`
  - `api-run`
  - `api-capability`
  - `api-workflow`
  - `api-worker`
  - `api-governance`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

只承载契约对象，不放 Controller、Service 实现或具体运行时 SDK 类型。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
