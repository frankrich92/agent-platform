# TASK-V3-023 测试报告

## 状态

Review。

## 已执行命令

```bash
cd source/v3-agent-platform
mvn test
```

结果：通过。API/SSE E2E 覆盖 `Last-Event-ID` replay、无效 event id -> 400、assistant final flush、run completed / cancelled。

```bash
HTAM_AGENT_FALLBACK_ONLY=false mvn -pl agent-boot -am spring-boot:run -Dspring-boot.run.arguments=--server.port=18080
curl /api/chat/{runId}/stream
curl /api/chat/{runId}/stream -H "Last-Event-ID: <firstEventId>"
curl /api/chat/{runId}/stream -H "Last-Event-ID: evt_missing"
```

结果：通过。真实 PostgreSQL / R2DBC checkpoint 链路中，首次流 `eventCount=14`、`duplicateEventIds=0`、终态 `run.completed`；断点回放从第二个事件开始返回 `replayCount=13`；无效 `Last-Event-ID` 返回 `400 STREAM_RESUME_FAILED`。

```bash
set -a
source .env
set +a
HTAM_AGENT_FALLBACK_ONLY=true mvn -pl agent-boot -am test
```

结果：通过。真实 PostgreSQL / MyBatis-Plus Mapper / R2DBC checkpoint 路径通过 API/SSE E2E；runtime 使用 deterministic fallback，避免本次 MyBatis 修复依赖外部 LLM 波动。

```bash
python3 source/test/v3/chat_minimal_db_e2e.py --base-url http://127.0.0.1:18080 --timeout-seconds 120
```

结果：通过。真实 API / SSE / PostgreSQL 持久化链路中，脚本校验 `agent_session`、`agent_message`、`agent_run`、`agent_span`、`agent_message_checkpoint` 的关联关系、终态、checkpoint 连续性和 `testRunId` 隔离；默认清理测试数据。

## 未覆盖范围

- 未覆盖数据库断线、服务进程重启继续同一 run、多节点并发恢复。
- `repo-r2dbc-stream` 尚未拆成独立 adapter；当前 checkpoint 写入在 PostgreSQL 仓储实现内通过 R2DBC 完成。

## 结论

SSE checkpoint 语义进入 Review；真实 MyBatis-Plus 普通 CRUD 与 R2DBC checkpoint 已补测通过。
