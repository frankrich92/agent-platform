# governance-risk

## 模块作用

风险治理模块，按分层风险策略对动作进行 allow/ask/deny 决策，并串联审批与审计。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 6 个 Java 源文件。
- 主要包：
  - `com.htam.agent.governance.risk`
- 阅读入口：
  - `GovernanceActionRequest`
  - `GovernanceDecision`
  - `GovernanceDecisionStatus`
  - `GovernanceGuard`
  - `LayeredRiskPolicy`
  - `LayeredRiskPolicyResolver`
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

治理模块应输出决策、审批、审计和鉴权结果，避免直接耦合具体运行时实现。

## 阅读建议

先看 `com.htam.agent.governance.risk` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
