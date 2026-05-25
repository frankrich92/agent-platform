# TASK-V3-901 e2e-real 套件

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

`chat-minimal` 已具备真实 LLM、PostgreSQL、MyBatis-Plus 普通 CRUD 和 R2DBC checkpoint 链路。仅靠页面观感或单次 curl 不能稳定证明一次对话在数据库中的持久化记录符合预期，需要一个可重复执行的真实 E2E 脚本。

## 目标

- 在 `source/test/v3` 下提供可独立运行的真实链路 E2E 脚本。
- 通过 HTTP API 发起一次真实对话并消费 SSE 至终态。
- 校验 `agent_session`、`agent_message`、`agent_run`、`agent_span`、`agent_message_checkpoint` 的关键记录和关联关系。
- 默认使用 `testRunId` 隔离并清理测试数据，支持 `--keep-data` 供人工复核数据库记录。

## 非目标

- 不替代 Playwright UI E2E。
- 不覆盖多节点恢复、服务进程重启后继续同一 run、数据库断线恢复。
- 不把 `.env`、数据库密码、LLM key 或真实用户数据写入报告。

## 必须阅读

```text
全局规则（精读）：
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/08-小工程切片与验收门禁.md

本小工程（精读）：
docs/v3-agent中台/工程迭代/01-chat-minimal/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
docs/v3-agent中台/工程迭代/01-chat-minimal/04-工程测试策略与真实E2E.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/README.md

本任务（精读）：
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-901-e2e-real套件/task.md
```

## owned modules

```text
source/test/v3
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-901-e2e-real套件
```

## owned files

```text
source/test/v3/chat_minimal_db_e2e.py
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-901-e2e-real套件/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-901-e2e-real套件/test-report.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-901-e2e-real套件/handoff.md
```

## 禁止修改

```text
source/v3-agent-platform/**/*
source/v3-agent-platform-ui/**/*
docs/v3-agent中台/工程迭代/其他小工程/**/*
```

## 输入契约

`chat-minimal` 已冻结 HTTP API、SSE envelope、PostgreSQL 表结构和 `.env` 中的本地安全配置。

## 输出契约

可重复运行的 Python E2E 脚本；成功时输出 JSON 摘要，失败时返回非 0 并输出失败原因。

## 参考依据要求

本任务只实现测试脚本，不新增或修改 API、SSE、DTO、数据模型、表结构或状态机设计；不需要 `contract-notes.md`。

## 并行开发计划

是否启用多子 Agent 并行：否。

## 目标验收等级

L5。

## 必须执行的验证命令

```bash
python3 source/test/v3/chat_minimal_db_e2e.py --base-url http://127.0.0.1:18080 --timeout-seconds 120
```

## 真实 E2E 要求

- 后端必须连接真实 PostgreSQL。
- 具备 `.env` 且 `HTAM_AGENT_FALLBACK_ONLY=false` 时，应走真实 LLM / AgentScope 链路。
- 测试数据必须使用 `testRunId` 隔离；默认清理，人工复核时可使用 `--keep-data`。

## 风险

- 依赖本地 `.env`、数据库和外部 LLM 可用性；外部 provider 波动时应标记为 L5 failed / degraded，而不是用 mock 结果替代。
- 当前脚本覆盖单轮成功对话，不覆盖失败、取消和重连场景。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
