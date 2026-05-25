# TASK-V3-021 contract notes

## 涉及契约

- 错误码：`CAPABILITY_NOT_APPROVED`、`CAPABILITY_NOT_AUTHORIZED`、`RUNTIME_UNAVAILABLE`。
- 状态模型：`AuditStatus.APPROVED/PENDING`、`CapabilityStatus.ENABLED`、`RiskLevel.LOW`、`RuntimeType.AGENTSCOPE`。

## 参考依据

| 来源 | 版本 / commit | 路径或 API | 采纳点 | 不采纳点 | 原因 |
| --- | --- | --- | --- | --- | --- |
| AgentScope Runtime Java | 本地 fork `7b9032b` | `engine-core/.../schemas/AgentRequest.java`、`Event.java` | 采纳执行前 request plan / status gate 的分层思路 | 不直接使用 runtime 原始 status 字符串 | 平台需要先做 Agent / Capability 治理，再交给 runtime |
| V3 数据模型基线 | 当前文档 | `04-数据模型与安全审计-全局基线.md` 中 `agent_capability`、`agent_capability_binding`、`agent_audit_log` | 采纳 capability status、audit status、risk level | POC-2 不实现完整 DB 表和 Admin 配置 | chat-minimal 只需要发送前 gate |

## 当前取舍

- `CapabilityGate` 只做策略判断；Agent 和 Capability 查询由 `biz-chat` 通过 `ChatRepository` 完成。
- 高风险沙箱真实执行不属于 chat-minimal。
