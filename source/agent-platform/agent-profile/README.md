# agent-profile

## 模块作用

Agent 配置画像业务层，负责智能体定义、提示词、版本、绑定和后台配置等配置态能力。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `profile-core`
  - `profile-agent`
  - `profile-binding`
  - `profile-version`
  - `profile-prompt`
  - `profile-model-policy`
  - `profile-admin`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

Profile 模块聚合智能体配置态，不直接暴露 AgentScope 等具体运行时对象。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
