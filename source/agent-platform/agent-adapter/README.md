# agent-adapter

## 模块作用

外部协议适配层，承接 REST、WebSocket、AGUI、OpenAPI、Admin 和内部调用入口。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `adapter-rest`
  - `adapter-websocket`
  - `adapter-agui`
  - `adapter-openapi`
  - `adapter-admin`
  - `adapter-internal`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

适配器负责协议转换和参数校验，业务规则应下沉到 profile/run/capability/governance/worker 等模块。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
