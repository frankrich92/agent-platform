# Cross-cutting 任务包

本目录保存跨阶段任务，例如 Playwright、e2e-real、AGENTS 分层规则和 CI 质量门禁。

阶段门禁：

- 横切任务必须声明影响的 POC 阶段和 owned files。
- 不得在横切任务中顺手修改业务模块。
- 涉及测试数据的任务必须使用 `testRunId` 隔离和清理。

当前任务：

```text
TASK-V3-900-Playwright-E2E套件
TASK-V3-901-e2e-real套件
TASK-V3-902-AGENTS分层规则
TASK-V3-903-CI质量门禁
```

计划任务：

```text
暂无
```
