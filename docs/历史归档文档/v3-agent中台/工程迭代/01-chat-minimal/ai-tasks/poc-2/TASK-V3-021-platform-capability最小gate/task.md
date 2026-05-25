# TASK-V3-021 platform-capability 最小 gate

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

Chat 发送前必须经过 Agent / Capability 审核和风险等级判断，不能由前端或业务服务绕过。

## 目标

- 实现最小 `CapabilityGate`。
- 拒绝未授权、未审核、未启用或非低风险 Chat capability。
- 保持 `platform-capability` 不直接依赖仓储。

## 非目标

- 不实现 Admin capability 配置页面。
- 不实现高风险 sandbox 真实执行。

## owned modules

```text
source/v3-agent-platform/agent-platform/platform-capability
source/v3-agent-platform/agent-biz/biz-chat
```

## 输入契约

`AgentDefinition`、`AgentCapability`、`RuntimeType`、`AuditStatus`、`CapabilityStatus`、`RiskLevel`。

## 输出契约

通过时返回可执行 Agent；拒绝时抛出 `CapabilityDeniedException` 并由 boot 层映射到 V3 错误响应。

## 目标验收等级

L3-L5。

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn test
```

## 真实 E2E 要求

API/SSE E2E 必须覆盖未审核 capability -> 403。
