# TASK-V3-023 contract notes

## 涉及契约

- SSE envelope：`eventId`、`eventType`、`runId`、`sessionId`、`messageId`、`sequence`、`createdAt`、`payload`。
- checkpoint：`runId + sequence` 单调顺序、`eventId` 恢复、`accumulatedContent` final flush。

## 参考依据

| 来源 | 版本 / commit | 路径或 API | 采纳点 | 不采纳点 | 原因 |
| --- | --- | --- | --- | --- | --- |
| 本地 V2 SSE envelope | 当前工作区 | `source/htam-agent-platform/.../StreamEnvelope.java`、`source/htam-agent-platform-ui/src/api.ts` | 采纳 envelope 字段、event id、前端 `fetch` + `ReadableStream` 分发方式 | 不沿用旧下划线事件名 | V3 统一为点分事件名 |
| AgentScope Java session mysql | 本地 fork `13a71676` | `agentscope-extensions-session-mysql/.../MysqlSession.java` | 采纳增量状态、幂等 key、参数化写入思路 | 不直接采用 `agentscope_sessions` blob 表结构 | V3 需要 Run/Span/SSE checkpoint 专用模型 |
| PostgreSQL / R2DBC | PostgreSQL 18.x、`r2dbc-postgresql` | `agent_message_checkpoint(run_id, sequence, event_id, payload_json, accumulated_content)`、`DatabaseClient.sql(...).rowsUpdated()` | 采纳 `run_id + sequence` 幂等写入、`event_id` 恢复和 JSONB payload 存储 | 不在 POC-2 中引入队列或跨 JDBC / R2DBC 分布式事务 | `chat-minimal` 优先验证真实 checkpoint 可恢复和 final flush 可见 |

## 当前取舍

- POC-2 已使用真实 PostgreSQL / MyBatis-Plus Mapper / R2DBC checkpoint 验证 `Last-Event-ID` replay。
- 普通 CRUD 与历史读取已由 `repo-mybatis` 接入 MyBatis-Plus；checkpoint 写入继续走 R2DBC，二者不做跨技术事务，依靠 `runId + sequence`、终态和 final flush 收敛。
