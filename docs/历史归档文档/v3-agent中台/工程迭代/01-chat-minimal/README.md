# EPIC-V3-A chat-minimal

`chat-minimal` 是 V3 的首个小工程，目标是先跑通业务老师可用的最小 Chat 闭环，而不是一次性实现完整 Agent 中台。

优先阅读：

```text
01-工程范围与验收目标.md
02-工程契约切片.md
03-工程技术设计.md
04-工程测试策略与真实E2E.md
05-工程路线图与任务.md
ai-tasks/README.md
```

关键架构决策见：

```text
adr/
```

本工程只覆盖：

- Agent 查询、标签筛选和授权可见。
- Session 创建、查询、重命名、删除。
- Chat 发送、取消、重新生成、编辑重发。
- Chat SSE 流式输出。
- Message / Run / Span / Stream checkpoint 最小落库。
- Chat 页面和基础执行详情。

暂不覆盖：

- Admin 完整后台。
- 任务中心。
- 工作流。
- 外部复杂 Agent。
- 高风险沙箱真实执行。
