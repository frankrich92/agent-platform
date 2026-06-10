# agent-domain

## 模块作用

领域模型聚合层，承载实体、枚举、值对象和领域侧基础对象，是其他服务端模块共同依赖的底座。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `domain-common`
  - `domain-profile`
  - `domain-run`
  - `domain-capability`
  - `domain-workflow`
  - `domain-worker`
  - `domain-governance`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

保持领域对象轻量，避免把 Web、具体运行时或数据库实现细节继续向上泄漏。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
