# TASK-V3-002 数据模型与安全审计冻结

## 状态

Review

## 背景

V3 需要先明确租户、用户、Agent、Capability、Run、Span、SSE checkpoint、审计等数据边界，避免后续模块各自建表和重复建模。

## 目标

- 复核数据域、表清单、状态枚举、安全审计模型。
- 明确 MyBatis-Plus 与 R2DBC 的使用边界。
- 明确权限、Capability Gate、审计事件的最小闭环。
- 输出需要 ADR 的重大数据或安全决策。

## 非目标

- 不写 Flyway SQL。
- 不创建数据库。
- 不实现 repo 代码。

## 必须阅读

```text
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
```

## owned modules

```text
docs
```

## owned files

```text
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/contract-notes.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/test-report.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/handoff.md
```

## 禁止修改

```text
source/
```

## 输入契约

现有数据、安全、审计草案。

## 输出契约

冻结后的数据模型、安全审计边界和待 ADR 决策清单。

## 目标验收等级

L0

## 必须执行的验证命令

```bash
git diff --check
rg -n "TODO|FIXME|TBD" docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结 || true
```

## 真实 E2E 要求

无。

## 风险

数据模型不清会直接影响 repo-spi、repo-mybatis、repo-r2dbc-stream 和后续 E2E 数据隔离。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
