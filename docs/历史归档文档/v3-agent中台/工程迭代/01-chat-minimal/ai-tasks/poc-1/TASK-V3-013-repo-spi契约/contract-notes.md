# TASK-V3-013 契约补充

> 本文为 POC-1 规划阶段预填。POC-1 执行完成后，必须复核并更新所有“本任务采纳”“本任务不采纳”和待人工确认项。

## 本任务涉及接口

涉及 repo-spi Java 仓储端口。执行后需要补充最终 V3 接口名、方法签名和包路径。

端口必须区分：

```text
普通 CRUD / 历史查询仓储
stream checkpoint / final flush 仓储
审计与执行详情查询仓储
```

## 本任务涉及 SSE event

无直接 SSE event，涉及平台 SSE envelope 的 checkpoint 持久化和 `Last-Event-ID` 恢复。

## 本任务涉及 RuntimeEvent

无直接 RuntimeEvent 类型定义，但仓储端口必须能保存从 RuntimeEvent 映射后的 `eventId`、`sequence`、`eventType`、`payload`、`accumulatedContent`、run/message 终态。

## 本任务涉及错误码

repo-spi 不直接定义 HTTP 错误码，但必须能向上层区分：

```text
SESSION_NOT_FOUND
RUN_NOT_FOUND
STREAM_RESUME_FAILED
STREAM_CHECKPOINT_FAILED
```

## 本任务涉及 DTO / 数据模型 / 表结构 / 状态机

- DTO：定义 repo-spi 查询条件、保存命令和返回模型，不定义 HTTP DTO。
- 数据模型：覆盖 Agent、AgentVersion、Session、Message、MessageCheckpoint、Run、Span、ToolCall、Audit、Provider、Capability。
- 表结构：不写 Flyway SQL，但端口必须与 `02-工程契约切片.md` 的最低字段和索引语义兼容。
- 状态机：涉及 Session running guard、Run 终态、Message final flush、checkpoint 幂等写入。

## 参考开源产品与 API / 模型依据

| 参考项目 | 版本/commit | 源码或文档定位 | 实际 API / 模型 / 字段 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-extensions/agentscope-extensions-session-mysql/src/main/java/io/agentscope/core/session/mysql/MysqlSession.java` | `agentscope_sessions(session_id,state_key,item_index,state_data,created_at,updated_at)`，主键 `(session_id,state_key,item_index)` | 采纳增量 list state、幂等 key、参数化写入和 session state 分片思路 | 不直接采用 `agentscope_sessions` 表名、MySQL DDL、`state_data` blob 作为 V3 业务表 | V3 需要 PostgreSQL、Run/Span/Audit、SSE checkpoint 和用户隔离字段 |
| chat-minimal 工程契约切片 | 当前文档版本 | `docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md` | `agent_message_checkpoint.event_id/sequence/event_type/payload_json/accumulated_content`，`agent_run.status/trace_id/error_code/test_run_id`，`agent_session.last_run_id/test_run_id` | 作为 repo-spi 端口字段权威范围 | 不加入暂缓表 `agent_message_attachment`、`agent_input_derived_context`、Task / Workflow 表 | 保持 `chat-minimal` 不引入 OCR / 附件 / 工作流 |
| V3 数据模型全局基线 | 当前文档版本 | `docs/v3-agent中台/04-数据模型与安全审计-全局基线.md` | 普通 CRUD 使用 MyBatis-Plus，SSE 高频 checkpoint / final flush 使用 R2DBC；审计字段和 testRunId 隔离要求 | 采纳仓储端口分层和测试数据隔离要求 | 不把全局候选表全部纳入 POC-1 | 小工程 contract slice 是当前实现权威 |
| 本地 V2 Flyway | 当前工作区 | `source/htam-agent-platform/agent-web/src/main/resources/db/migration/V1__init_agent_chat_tables.sql` | `agent_session(id,user_code,agent_id,title,status,message_count,last_message_at)`，`agent_message(id,agent_id,session_id,role,status,sequence)`，`agent_run(id,user_code,agent_id,session_id,user_message_id,assistant_message_id,status,trace_id)`，索引 `idx_agent_session_user_status_updated`、`idx_agent_message_session_sequence`、`idx_agent_run_user_agent_session_started` | 采纳会话列表、消息顺序、run 查询和索引方向 | 不沿用 `user_code`、旧表 `agent_message_part` 拆分方式和旧 capability 绑定表命名 | V3 统一使用 `user_id`、`agent_message.content`、checkpoint 表和新授权模型 |

## 是否需要修改主契约

默认不需要。若执行时发现 repo-spi 必须新增表、字段、索引或破坏当前数据切片，先写入本文，再由主 Agent 或 Review Agent 判断是否回写 `02-工程契约切片.md` 或全局数据基线。

## 待人工确认

- `agent_message_checkpoint` 的 `payload_json` 是否需要在 POC-1 冻结 JSON schema，还是由 `TASK-V3-015 agent-api 契约` 冻结 SSE payload 后再回填。
- final flush 是否由 `repo-r2dbc-stream` 端口一次性更新 `agent_message` / `agent_run`，还是拆成 checkpoint 追加与终态更新两个端口。
