# agent-governance

## 模块作用

治理聚合层，承载认证、IAM、策略、敏感词、审批、审计、评估、风险和可观测性。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `governance-auth`
  - `governance-policy`
  - `governance-sensitive`
  - `governance-approval`
  - `governance-eval`
  - `governance-observability`
  - `governance-risk`
  - `governance-iam`
  - `governance-audit`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
