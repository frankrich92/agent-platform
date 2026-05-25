# TASK-V3-002 契约补充

## 本任务涉及数据域

- User / Role / Permission。
- Agent / AgentVersion / AgentTag / ExternalAgent。
- Session / Message / Memory。
- Provider / Model。
- Capability / MCP / Skill / Tool。
- Run / Span / Audit。
- Task / TaskResult。
- Workflow / WorkflowRun。
- E2E `testRunId` 数据隔离。

## 本任务涉及表与字段

主契约冻结以下表清单作为 POC-1/POC-2 基线：

```text
agent_definition
agent_version
agent_tag
agent_definition_tag
agent_external_config
agent_user_grant
agent_capability_binding
agent_session
agent_message
agent_message_checkpoint
agent_run
agent_span
agent_tool_call
agent_audit_log
agent_task_template
agent_task_run
agent_task_step
agent_task_result
agent_workflow_definition
agent_workflow_version
agent_workflow_node
agent_workflow_edge
agent_workflow_run
agent_workflow_step_run
agent_capability
agent_mcp_config
agent_skill_config
agent_tool_config
agent_provider
agent_model
sys_user
sys_role
sys_permission
sys_user_role
sys_role_permission
```

本任务补充冻结：

- V3 POC 默认单组织 / 单租户部署，后续 `tenantId` 只能追加，不破坏当前 `userId` 和授权关系。
- E2E 数据必须通过独立字段或 `metadata.testRunId` 隔离。
- `metadata` 不允许保存密钥、token、数据库密码或原始敏感响应。

## 本任务涉及状态枚举

```text
Run: queued / running / completed / failed / cancelled / timeout
Message: created / streaming / completed / failed / cancelled
Task: queued / running / waiting / completed / failed / cancelled
WorkflowRun: created / running / completed / failed / cancelled
RiskLevel: low / medium / high / critical
```

## 本任务涉及权限与审计

权限最小闭环：

- Agent enabled。
- Agent 审核通过。
- 用户有可见或使用权限。
- Agent 绑定 capability 可用。
- Capability gate 通过。
- workflow 节点调用 Agent 时重新计算授权和 capability gate。

审计最小闭环：

- Provider 配置、Capability 审核、权限授权、高风险执行、外部 Agent 调用、工具/MCP/Skill 调用必须写审计。
- 审计字段至少包含 `auditId/userId/action/resourceType/resourceId/riskLevel/result/traceId/runId/spanId/createdAt/summary/metadata`。
- 普通用户只能看到脱敏摘要。

## 本任务涉及持久化边界

- MyBatis-Plus：普通 CRUD、后台配置、历史查询、分页查询、常规 Run/Span/Message 读写。
- R2DBC：SSE checkpoint、assistant message 累积内容、run/span/message final flush、流式状态更新、幂等 sequence 写入。
- Flyway：结构变更、新表、新索引、初始化系统参数和能力开关；已应用迁移不修改语义。
- MyBatis 和 R2DBC 不做跨技术事务，依靠 `runId/messageId/spanId/sequence` 和状态机保证最终一致。

## 参考开源产品与 API / 模型依据

| 参考来源 | 版本/状态 | 定位 | 实际对象/字段/规则 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |
| V3 数据模型全局基线 | 当前文档版本 | `docs/v3-agent中台/04-数据模型与安全审计-全局基线.md` | 数据域、表清单、安全审计规则 | 采纳为数据、安全、审计主契约 | 不在本任务写 Flyway SQL | POC-0 只冻结文档契约，不实现数据库 |
| V3 后端链路 | 当前文档版本 | `docs/v3-agent中台/02-Agent中台技术架构方案.md` | `RuntimeCapabilityPlan`、stream 持久化链路、MyBatis/R2DBC 边界 | 采纳 repo-spi、repo-mybatis、repo-r2dbc-stream 的边界 | 不在 biz/admin 直接访问 Mapper 或 R2DBC Client | 保持模块隔离，便于 POC-1/POC-2 并行开发 |
| V3 测试策略 | 当前文档版本 | `docs/v3-agent中台/05-测试策略与验收等级-全局基线.md` | `testRunId` 数据隔离和清理 | 采纳所有 E2E 数据必须带 `testRunId`，清理按 `testRunId` 执行 | 不允许用人工本地脏数据证明 E2E 完成 | 防止测试污染真实数据 |
| V3 开发规范 | 当前文档版本 | `docs/v3-agent中台/07-开发规范与本地环境.md` | 普通 CRUD 使用 MyBatis-Plus，SSE checkpoint/final flush 使用 R2DBC | 采纳双持久化边界和 Flyway 迁移规则 | 不使用手写 Spring JDBC / JdbcClient 作为新架构方案 | 降低持久化风格分裂和阻塞 SSE 风险 |
| AgentScope Java | `13a71676` | `source/fork_source/agentscope-java/agentscope-extensions/agentscope-extensions-session-mysql/src/main/java/io/agentscope/core/session/mysql/MysqlSession.java` | `agentscope_sessions(session_id,state_key,item_index,state_data,created_at,updated_at)`，主键 `(session_id,state_key,item_index)` | 采纳增量状态、幂等 key、参数化写入和 session state 分片思路 | 不直接采用 MySQL 表名、列名或 `state_data` blob 作为 V3 表结构 | V3 需要 PostgreSQL、Run/Span/Audit、SSE checkpoint 和用户隔离字段 |
| 本地 V2 Flyway | 当前工作区 | `source/htam-agent-platform/agent-web/src/main/resources/db/migration/V1__init_agent_chat_tables.sql` | `agent_session(id,user_code,agent_id,title,status,message_count,last_message_at)`，`agent_message(id,agent_id,session_id,role,status,sequence)`，`agent_run(id,user_code,agent_id,session_id,user_message_id,assistant_message_id,status,trace_id)` | 采纳会话列表、消息顺序、run 查询和索引方向 | 不沿用 `user_code`、旧 `agent_message_part` 拆分方式和旧 capability 绑定表命名 | V3 统一使用 `user_id`、`agent_message.content`、checkpoint 表和新授权模型 |
| V2 历史实现评审 | 历史参考 | `docs/v2-通用大模型对话/评审/agentscope源码分析.md` | runtime 请求携带 `userCode/agentId/runId/trace context/安全策略` | 采纳 trace/run/user/capability 上下文进入审计和 Run/Span | 不直接沿用 V2 旧目录和旧服务结构 | V3 使用新模块边界 |

## 是否需要修改主契约

已修改 `docs/v3-agent中台/04-数据模型与安全审计-全局基线.md`：

- 标注 POC-0 数据模型 / 安全 / 审计基线冻结状态。
- 明确 V3 POC 租户边界和未来 `tenantId` 追加规则。
- 明确所有 E2E 数据必须通过 `testRunId` 隔离和清理。
- 补充 metadata、MyBatis-Plus、R2DBC、Capability risk、审计 action 和审计脱敏规则。

## 待人工确认

- 是否需要为 MyBatis-Plus + R2DBC 双持久化边界新增 ADR，对应 `RTK.md` 的 `OQ-V3-002`。
- `tenantId` 是否在 POC-1 表结构中预留字段，还是等多租户进入正式范围后再追加迁移。
- 高风险审计写入失败时默认阻断执行的具体策略，需要后续 `platform-security` / `platform-observability` 任务细化。
