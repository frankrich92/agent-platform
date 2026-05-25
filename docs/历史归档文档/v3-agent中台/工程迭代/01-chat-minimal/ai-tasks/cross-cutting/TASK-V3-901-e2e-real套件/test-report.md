# TASK-V3-901 测试报告

## 状态

Review。

## 已执行命令

```bash
python3 source/test/v3/chat_minimal_db_e2e.py --base-url http://127.0.0.1:18080 --timeout-seconds 120
```

结果：通过。脚本发起一次 `chat-minimal` 对话，消费 SSE 至 `run.completed`，并校验 PostgreSQL 中 Session、Message、Run、Span、checkpoint 记录的关联关系；默认清理测试数据，输出 `cleanedUp: true`。

```bash
python3 source/test/v3/chat_minimal_db_e2e.py --base-url http://127.0.0.1:18080 --timeout-seconds 120 --keep-data
```

结果：通过。保留测试数据用于人工查看 `agent_session`、`agent_message`、`agent_run`、`agent_span`、`agent_message_checkpoint`。

## 环境信息

- OS / shell：macOS / zsh。
- JDK / Maven / Node：JDK 21、Maven 4 RC、Node 24。
- 数据库：本地 PostgreSQL，连接信息来自 `.env`。
- 是否使用 `.env`：是；报告不记录密钥、token、数据库密码或 LLM key。

## 覆盖范围

- `GET /api/health` 健康检查。
- `POST /api/sessions` 新建会话。
- `POST /api/chat/send` 发起对话。
- `GET /api/chat/{runId}/stream` 消费 SSE 至终态。
- 校验 `agent_session.last_run_id`、`test_run_id`、用户和 Agent 归属。
- 校验 `agent_message` 中 USER / ASSISTANT 两条消息、内容、状态、run 关联和 `test_run_id`。
- 校验 `agent_run` 状态、消息关联、完成时间和 `test_run_id`。
- 校验 `agent_span` 至少存在已结束 span。
- 校验 `agent_message_checkpoint` 序号连续、包含生命周期事件和 `run.completed`。

## 用例统计

- 通过：2 次手工执行。
- 失败：0。
- 跳过：0。
- 重试后通过：0。

## 未覆盖范围

- 未覆盖服务进程重启后继续同一 run。
- 未覆盖多节点恢复。
- 未覆盖数据库断线、LLM provider 超时、取消和失败分支。
- 未覆盖 Playwright 页面操作；页面验收由 `TASK-V3-024` 承担。

## 测试数据与隔离

- `testRunId`：脚本每次生成 `e2e-db-<timestamp>-<random>`。
- 数据清理方式：默认按 `sessionId` / `runId` 删除 checkpoint、span、run、message、session。
- 未清理风险：仅在显式传入 `--keep-data` 时保留，供人工数据库复核。

## 失败与重试

- 失败命令：无。
- 失败原因：无。
- 是否重试：否。
- 剩余风险：真实 LLM 和本地数据库可用性仍可能导致后续执行出现环境型失败。

## 结论

`chat-minimal` 的真实 API / SSE / PostgreSQL 持久化链路已有可重复执行的 DB E2E 脚本，进入 Review。
