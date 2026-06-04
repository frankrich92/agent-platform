# agent-platform

## 模块作用

后端 Maven 根工程，统一版本、依赖管理和模块编排，是服务端 reactor 的入口。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `agent-domain`
  - `agent-api`
  - `agent-support`
  - `agent-repo`
  - `agent-profile`
  - `agent-run`
  - `agent-runtime`
  - `agent-workflow`
  - `agent-capability`
  - `agent-worker`
  - `agent-governance`
  - `agent-adapter`
  - `agent-boot`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

遵循 RTK.md 中的单向依赖和职责边界。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
