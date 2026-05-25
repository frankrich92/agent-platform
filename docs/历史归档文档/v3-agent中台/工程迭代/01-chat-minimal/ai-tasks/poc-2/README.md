# POC-2 任务包

POC-2 聚焦 Chat 主链路、AgentScope runtime、SSE checkpoint 语义和前端最小闭环。

阶段门禁：

- 业务老师能选择一个通用 Agent，完成流式对话。
- 普通 Chat 通过 `runtime-agentscope` 适配 `agentscope-java`；无 `.env` 时使用 deterministic runtime 作为本地验收替代。
- Run / Span / SSE / 消息可追踪。
- Chat 前端通过 Playwright UI E2E。
- 真实 LLM / AgentScope / PostgreSQL / R2DBC checkpoint 链路已在本地 `.env` 下跑通；`.env` 不入库、不写入报告。

当前任务状态：

```text
TASK-V3-020-runtime-agentscope最小适配      Review
TASK-V3-021-platform-capability最小gate     Review
TASK-V3-022-biz-chat主流程                  Review
TASK-V3-023-stream-persistence              Review
TASK-V3-024-Chat前端最小闭环                Review
```

已执行验证：

```bash
cd source/v3-agent-platform
mvn test

set -a
source .env
set +a
HTAM_AGENT_FALLBACK_ONLY=false mvn -pl agent-boot -am spring-boot:run -Dspring-boot.run.arguments=--server.port=18080

cd source/v3-agent-platform-ui
npm test
npm run build
HTAM_AGENT_FALLBACK_ONLY=false npm run test:e2e
```

当前剩余风险：

- POC-2 已补 PostgreSQL / Flyway / MyBatis-Plus Mapper / R2DBC checkpoint 真实链路；`repo-r2dbc-stream` 独立 adapter 拆分和多节点恢复留到后续 hardening。
- `.env` 属于本地安全配置，未提交；无 `.env` 时只能执行 deterministic runtime 的 L4/L3 回归。
