# V3 Agent 中台文档评审 v2

评审人：Claude Opus 4.7（1M context）
评审日期：2026-05-19
评审范围：`docs/v3-agent中台/` 全量文档（README、RTK、01-08 全局基线、templates、`工程迭代/01-chat-minimal/` 全部交付物，含 ADR、ai-tasks/poc-0、poc-1、cross-cutting、poc-2 占位）
评审目的：在 v1 评审（claude / glm5.1 / kimi2.6 / dpsk-v4 共四份）的基础上，复核当前文档体系作为后续 AI Native 编程基础是否就绪，并给出阶段门禁建议。

---

## 一、总体结论

文档体系相较 v1 有较大改进，绝大部分 v1 阻断性意见已落地。当前状态为：

- POC-0（文档/契约/数据模型/小工程切片冻结）**整体已就绪**，可结案进入 POC-1 真实代码开发，前提是修复本评审 §四列出的 4 项 POC-1 启动前必须收敛的问题。
- POC-1（Maven 多模块骨架、agent-domain / runtime-spi / repo-spi / agent-api 契约、architecture test）任务包已完整创建，目标验收等级、owned files、验证命令清晰，可在阻断项收敛后启动。
- POC-2 任务包尚未创建（仅 README 占位），属于刻意延后；但本评审建议在 POC-1 进行到 50% 时启动 POC-2 任务包预设计，避免阻塞。
- 后端工程实体（`source/v3-agent-platform/`）目前只有 `AGENTS.md`，前端工程同。这是符合预期的；POC-1 的 TASK-V3-010 才创建多模块骨架。文档已正确表达"工程不存在时不要伪造验证结果"。

文档体系的核心优势保持稳定：

- 三层架构（全局候选基线 → 小工程 contract slice → 任务包过程文档）权威归属明确。
- 架构不变量（不自研 ReAct 循环、不绕过 AgentScope 触发工具、不走关键词匹配、sandbox 按动作风险判定、双持久化边界）多处一致。
- AI Native 交付规约的 owned modules / owned files、契约冻结、L0-L5 验收、testRunId 隔离形成完整闭环。
- 03 / 04 全局契约文档把 RuntimeRequest / RuntimeCapabilityPlan / RuntimeEvent / RuntimeResult 的 Java 类型归属明确划归 `runtime-spi`，`agent-api` 仅承载 HTTP/SSE DTO。这一边界 v1 多份评审都质疑，v2 已落地。

但仍存在若干需要在对应阶段门禁前收敛的问题，以及一组 POC-2 启动前不可缺失的工程化补项。

---

## 二、相对 v1 的修复进度复核

### 2.1 已修复（无需再处理）

| v1 编号 | v1 问题 | v2 落地位置 | 复核结论 |
| --- | --- | --- | --- |
| claude 2.1 / glm B-01 / kimi 2.1 / dpsk P0 | 文档编号缺 03 | 全局基线已重排为 01-08，原 04-09 → 03-08，03 = API/SSE/Runtime | 已修复 |
| claude 4.1 | Maven 版本不一致（mac 4.x RC vs WSL2 3.9） | 07 §8 macOS 与 WSL2 都为 `apache-maven-4.0.0-rc-5` | 已修复 |
| glm B-02 / dpsk 4.3 | RuntimeRequest/RuntimeEvent 归属模块模糊 | 03 §1、§8 明确归属 `runtime-spi`；02 §5 同步 `agent-api` 不承载 runtime-spi Java 类型 | 已修复 |
| glm B-03 / kimi 4.2 | Session 并发约束缺失 | 02-工程契约切片 §8 "Session 并发"明确同 sessionId 单 running run、SESSION_BUSY 入口判定、cancel 只取消指定 runId | 已修复（chat-minimal 切片） |
| glm I-01 | `agent_message_checkpoint` 字段未定义 | 02-工程契约切片 §5 给出 `checkpoint_id / run_id / message_id / sequence / event_type / payload_json / accumulated_content / created_at` | 已修复 |
| glm I-03 | `visibility_scope` 枚举缺失 | 04 §4 给出 `private / granted_users / role_scope / public_enabled` 与语义 | 已修复 |
| glm I-04 / kimi 5.4 | `agent_definition.runtime_type` 与 `RuntimeCapabilityPlan.runtimeType` 关系模糊 | 04 §4 与 03 §8 均说明前者是默认偏好，后者是 platform-capability 最终决策 | 已修复 |
| glm I-05 | 错误码与 HTTP 状态码映射缺失 | 02-工程契约切片 §6 给出 chat-minimal 错误码 → HTTP 状态码 | 部分修复（仅 chat-minimal 切片，03 §12 全局表仍未显式映射） |
| glm I-06 / kimi P0-2 / dpsk 5.1 | `biz-execution` 在 chat-minimal 缺位 | chat-minimal 03 §1 后端模块列表已包含 `biz-execution`；§3 实现边界明确 `biz-execution` 负责执行详情读取 | 已修复 |
| glm I-07 | Provider/Model 初始化方式未声明 | 02-工程契约切片 §7 "Provider / Model"：通过 Flyway seed / 测试 fixture，密钥只保存 secret ref | 已修复 |
| glm I-08 | `agent_version` 在 chat-minimal 用途未说明 | 02-工程契约切片 §7 "Agent Version"：至少创建一条初始版本，run/message 必须可追溯 `agent_version_id` | 已修复 |
| glm S-01 / kimi 4.5 / dpsk 3.5 | SSE 断线恢复语义不完整 | 02-工程契约切片 §8 "SSE 连接与恢复"：建立后发送可恢复的首个运行态事件、Last-Event-ID 按 `runId+sequence` 恢复、`STREAM_RESUME_FAILED` 兜底 | 已修复（heartbeat 间隔仍未声明） |
| glm S-02 | regenerate / edit-resend 语义差异 | 02-工程契约切片 §8 "重新生成与编辑重发"明确两者均创建新 run、保留原 message、复用同一权限/Capability/Run/Span/SSE/审计链路 | 已修复 |
| glm S-03 | `agent_run` / `agent_span` 字段缺失 | 02-工程契约切片 §5 给出最低字段（含 `trace_id`、`error_code`、`parent_span_id`） | 已修复 |
| kimi I-04 / I-08 / dpsk P0 | `RTK.md` 截断 | 现 RTK 完整收尾至 §9 状态更新规则 | 已修复 |
| kimi P0 / claude 3.1 | OQ-V3-005 前端技术栈未冻结 | RTK §8 OQ-V3-005 已写明 Vue 3 + TypeScript + Pinia 为基线，组件库和旧实现复用策略 POC-2 前确认 | 已部分修复（组件库仍未定） |
| kimi 3.6 | `WorkflowRun` 状态缺 `waiting` | 04 §6 仍为 `created/running/completed/failed/cancelled` | 未处理（见 §三 OQ-V3-008） |
| kimi 4.6 / dpsk 3.7 | thinking 内容是否落库 | 02-工程契约切片 §8 "thinking 内容"：POC 阶段只作为 stream checkpoint / Run 观测，是否落 `agent_message` 独立字段后续再冻结 | 已显式延后 |
| kimi 4.4 | chat-minimal Capability Gate "最小"含义 | 02-工程契约切片 §7 "最小 Capability Gate"：只允许 enabled+审核通过+绑定+用户授权+riskLevel=low 进入 runtime-agentscope | 已修复 |
| dpsk F3 | 错误码目录缺失 | 03 §12 列出 27 个全局候选错误码 + 兼容规则；chat-minimal 02 §6 列出 18 个切片错误码 + HTTP 映射 | 已修复 |
| claude 5.3 / kimi 3.10 / dpsk F13 | 任务单模板与 06 中模板不一致 | 06 §3 模板与 templates/AI任务单-模板.md 现保持口径一致（字段数与顺序对齐） | 已修复 |

### 2.2 仍未处理或部分处理（合并到本评审 §三、§四、§五跟踪）

| v1 编号 | v1 问题 | 当前状态 |
| --- | --- | --- |
| claude 3.2 / kimi P0-1 / dpsk 6.1 | Spring Boot 4.0.6 选型 ADR 缺失 | 仍未补 ADR；07 与 AGENTS.md 已固定版本号但无兼容性验证记录 |
| claude 3.3 | AgentScope Java fork commit 未锁定 | 仍未锁定；contract-notes.md 多处写"本地 fork 当前版本"但无 commit hash |
| claude 3.4 | 认证方案空白 | 仍未声明 |
| claude 3.5 / dpsk F5 | 部署架构与本地环境搭建指南缺失 | 仍未声明（PostgreSQL 安装、`.env` 模板、端口、跨域、本地启动顺序均未写） |
| dpsk P0-2 / dpsk F4 | API/SSE/数据模型形式化 Schema（OpenAPI / JSON Schema / DDL 类型） | 仍为 Markdown 描述；可在 POC-1 落地时由 agent-api / repo-spi 任务通过 Java 类型 + 单元测试承担形式化责任 |
| dpsk 5.6 / dpsk P1 | 测试 fixture / 测试用户登录与 token 注入方式 | 仍未声明 |
| dpsk 6.7 / dpsk P1 | 跨小工程契约依赖管理机制 | 仍未声明（仅约束公共契约改回写全局基线，无版本号或事件通知机制） |
| dpsk 5.4 | "方案竞争"步骤未展开 | 仍未展开 |
| dpsk 5.6 | 测试报告 / 交接记录模板过薄 | 模板仍较薄；有效字段已具备最低可用性，但缺环境信息、覆盖率等 |
| dpsk 6.4 / claude 5.4 | LLM 测试预算 / API key 获取与降级策略 | 仍未声明 |
| claude 4.2 | Node 版本不一致（v24.15.0 vs v24.13.0） | 仍存在小差异；属可接受范围但建议统一最低版本要求 |
| claude 4.3 / 状态语义 | EPIC-V3-A 在 RTK 状态为 In Progress，但下属 POC-0 任务为 Review，POC-1 为 Planned | 仍存在；建议 POC-0 全部 Done 后 EPIC 才转 In Progress，否则保持 Review |

### 2.3 三份 v1 评审的差异说明

- v1-claude 与 v1-glm5.1 在 contract slice 与全局基线的关系上判断一致；glm 提出的 8 项 P0/P1 中已落地 6 项。
- v1-kimi2.6 给出的"P0 必修 = Spring Boot 版本 + biz-execution + WorkflowRun.waiting"中，前两项已修复，第三项被 RTK 隐式延后。
- v1-dpsk-v4 强调形式化（Schema/DDL）与并行协调机制，前者目前仍是 Markdown 描述形态，后者已在 06 §5、§7 加入"共享文件冲突处理"与"contract owner 唯一改写"规则，但仍缺少跨小工程依赖追踪。

---

## 三、需要在 POC-1 启动前收敛的问题（阻断）

POC-1 包含 6 个任务（TASK-V3-010 至 TASK-V3-015），目标是 Maven 多模块骨架 + agent-domain / runtime-spi / repo-spi / agent-api Java 契约 + architecture test。下列问题不解决会直接影响 POC-1 任务执行的正确性。

### 3.1 [P0] Spring Boot 4.0.6 + Maven 4.0.0-rc-5 + MyBatis-Plus 兼容性 ADR 缺失

CLAUDE.md、AGENTS.md、07 文档与 09 路线图均已固定 JDK 21 / Spring Boot 4.0.6 / Maven 4.x RC。但：

- TASK-V3-010 Maven 多模块骨架将直接落地这套版本，一旦发现 MyBatis-Plus / R2DBC / Spring Boot 4 starter 不兼容，整个 POC-1 会返工。
- 当前不存在评估这套组合的 ADR，也没有对 MyBatis-Plus（截止 2026-05 主线 3.5.x，对 Spring Boot 4 尚未官方稳定支持）的冲突预案。
- 02 ADR 目录（chat-minimal/adr）只有 ADR-0001（AgentScope）和 ADR-0002（持久化边界），没有 ADR-0003 工具链选型。

**建议**：在 TASK-V3-010 启动前新增 ADR-0003，记录：当前 starter 兼容性矩阵、MyBatis-Plus / R2DBC 与 Spring Boot 4 的可用版本组合、Maven 4 RC 与多模块插件兼容性、降级到 Spring Boot 3.4.x / Maven 3.9.x 的回退条件。

### 3.2 [P0] AgentScope Java fork 版本锁定缺失

03 §2.2 与多份 contract-notes 均写"本地 `source/fork_source` 当前版本"，但：

- 没有 commit hash / tag。
- TASK-V3-012 `runtime-spi` 设计将依赖 `StreamableAgent.stream(List<Msg>, StreamOptions)`、`EventType.REASONING/TOOL_RESULT/...`、`Event.isLast/getSource/getMessageId`。如果上游接口在后续 fork 升级时变化，平台 RuntimeEvent 映射会出现回归。
- TASK-V3-020 后续真实接入 AgentScope ReActAgent 时，没有版本锚点会导致"按文档参考某接口但实际接口已变"。

**建议**：在 RTK §7 K-V3-002 / K-V3-003 增加 commit hash 或 git short-rev；后续 fork 升级走"先评估接口差异再升级"流程。

### 3.3 [P0] EPIC-V3-A 状态语义不一致

RTK §3.1 EPIC-V3-A 状态 = `In Progress`，但 §4 任务表中：

- POC-0 全部为 `Review`（未到 `Done`）。
- POC-1 全部为 `Planned`。
- POC-2 全部为 `Pending`。

按 RTK §9 状态更新规则，"任务开始执行后任务包和本文都改为 In Progress"。当前任务尚未真正进入执行阶段（POC-0 是文档冻结，POC-1 未启动），EPIC 状态应为 `Review` 或 `Planned`。

**建议**：调整为 `Review`；待 POC-0 全部 `Done` 且 TASK-V3-010 实际启动后再切 `In Progress`。否则后续 AI agent 阅读 RTK 时会误判进度。

### 3.4 [P1] 03 全局错误码 → HTTP 状态码映射仅落在 chat-minimal 切片

chat-minimal 02 §6 已给出 18 个切片错误码的 HTTP 状态码（401/403/404/409/400/503/500）。但 03 §12 全局错误码表只列了 27 个错误码 + 兼容规则，没有 HTTP 状态码列。

后续 admin-config / task-center / workflow-sequential 等小工程会再次为各自切片决定 HTTP 状态码，可能出现 `RATE_LIMITED` 一处 429、另一处 503 之类的分叉。

**建议**：在 03 §12 增加一列默认 HTTP 状态码，并明确"小工程可在不破坏语义的前提下调整"。这能在 TASK-V3-015 `agent-api` 契约任务里直接把 HTTP 状态码做成 enum 常量。

---

## 四、需要在 POC-2 启动前收敛的问题（阻断）

POC-2 是 Chat 主链路 + 真实 AgentScope + 前端最小闭环，目标 L5。下列问题在 POC-2 任务包创建前必须有结论。

### 4.1 [P0] 认证、会话与 SSE 长连接的最小身份方案

chat-minimal 01 §3 范围包含"最小用户身份和 Agent 授权校验"，但全局与小工程文档均未声明：

- 用户登录方式（JWT / 服务端 session / Mock token）。
- SSE 长连接如何携带身份（query 参数？Authorization header？cookie？）。
- testRunId 注入方式（header / cookie / metadata）。
- TASK-V3-022 biz-chat 入口、TASK-V3-024 前端 SSE client 都依赖该决策。

**建议**：在 chat-minimal 03 工程技术设计补一节"最小身份方案"，明确 POC 阶段使用 mock token / 内置测试用户 + Authorization header；同步把 SSE endpoint 的鉴权方式写入 02 §8。

### 4.2 [P0] 前端选型补齐：构建工具 / UI 组件库 / SSE 客户端 / 路由

OQ-V3-005 已收敛核心栈（Vue 3 + TypeScript + Pinia），但 TASK-V3-024 直接需要：

- 构建工具（Vite / Rspack / 其他）。
- UI 组件库（Element Plus / Ant Design Vue / 自研）。
- SSE 客户端（原生 EventSource / fetch + ReadableStream / 第三方）—与 `Last-Event-ID` 恢复语义直接相关。
- 路由（Vue Router 4 + 权限路由模式）。
- API 客户端（axios / fetch wrapper）。

**建议**：在 chat-minimal 03 工程技术设计 §4 前端范围下增加技术栈细化；或新增 ADR-0003-前端栈与 SSE 客户端选型。

### 4.3 [P0] PostgreSQL 与本地环境搭建指南

L2 集成测试与 L3 API/SSE E2E 都需要 PostgreSQL。文档当前规则：

- 06 §4 建议 Testcontainers PostgreSQL。
- 02 §11 提到 Capability gate 与 environment health。
- 但没有：本地数据库搭建步骤、`.env` 模板、Flyway 默认 schema、CI 中 Testcontainers 镜像版本约束。

dpsk 评审 P0-5 已强调；当前仍未补。

**建议**：在 07 §8 之后新增一节"本地依赖搭建"，列出 PostgreSQL 版本、最小数据库与用户、`.env.example`、`docker-compose.yml`（如使用）、CI 中 Testcontainers 镜像 tag。

### 4.4 [P0] 测试 fixture 与 testRunId 注入方式

05 §1 明确 E2E 数据必须带 testRunId，但：

- testRunId 写入位置：`agent_session.test_run_id` / `metadata.testRunId` 两条路径并存，未统一推荐。
- Playwright 测试用户的种子方式（API / SQL fixture / mock auth）未声明。
- TASK-V3-900 与 TASK-V3-901 是横切任务，目前只有任务编号、无任务包目录。

**建议**：在 chat-minimal 04 测试策略增加"测试 fixture 方案"小节，统一 testRunId 写入字段名（建议显式 column 而非 metadata）；在 cross-cutting 创建 TASK-V3-900 / TASK-V3-901 任务包目录与最小 task.md 模板。

### 4.5 [P0] LLM provider 最低能力声明与 .env 缺失时的降级路径

L5 验收依赖 `.env` 中真实 LLM。但：

- 没有声明 provider 最低能力（必须支持 streaming？必须支持 function calling？）。
- 当 LLM 不支持工具调用时，`tool.call.*` SSE 事件路径无法验证；当前 chat-minimal 不强制 tool 调用（最小 Capability Gate 只允许 low 风险），但仍需要明确 L5 通过的最低门槛。
- `.env` 不存在时只有"跳过并说明原因"。LLM 可用但调用失败（rate limit / 配额）时是否允许降级到 L4？

**建议**：在 chat-minimal 04 §6 L5 验收增加 LLM 最低能力清单，并定义 `.env` 存在但调用失败时的降级条件。

### 4.6 [P1] heartbeat 间隔与 SSE 超时

03 §6、§7 与 chat-minimal 02 §3、§8 均提到 heartbeat 用于保活，但未声明：

- 默认间隔（例如 15s / 25s / 30s）。
- 客户端在多久未收到 heartbeat 后判定断线。
- 服务端在 sandbox / runtime 长耗时时是否提升 heartbeat 频率。

**建议**：在 03 §6 增加默认 heartbeat 间隔（推荐 15s），客户端断线判定阈值（推荐 30s）。这是前端 SSE client 与 stream-repo-agent 共同依赖的契约值。

### 4.7 [P1] sys_user / sys_role / sys_permission 等 IAM 表的初始化方式

chat-minimal 02 §5 数据切片包含 IAM 5 张表，但 chat-minimal 不实现 admin 配置，没有像 §7 处理 provider/model 那样声明 IAM 数据的初始化方式。

**建议**：在 02 §7 "初始化与最小治理"增加"IAM seed"段，明确 POC 阶段 sys_user / sys_role / sys_user_role 的最小种子数据由 Flyway seed 或测试 fixture 创建。

### 4.8 [P1] WorkflowRun 状态枚举是否需要 waiting

kimi P0 评审已提出：Task 状态有 waiting，WorkflowRun 没有。当前 chat-minimal 不做 workflow，但 EPIC-V3-D workflow-sequential 启动时会立刻遇到这个差异。

**建议**：在 RTK §8 新增 `OQ-V3-008`：WorkflowRun 是否需要 waiting 状态、人工审批节点是否进入。当前先标记 Open，等 EPIC-V3-D 启动前再决定。

---

## 五、新发现的问题（v1 未明确指出）

### 5.1 [P1] `agent_audit_log` 在 chat-minimal 数据切片中只有表名，无最低字段

chat-minimal 02 §5 列出 `agent_audit_log` 但 §5 最低字段语义只覆盖 agent_definition / agent_version / agent_session / agent_message / agent_message_checkpoint / agent_run / agent_span / agent_tool_call。审计字段在 04 §12 有定义，但没回写到 chat-minimal 切片。

POC-1 TASK-V3-013 repo-spi 设计审计仓储端口时会再次决定字段，可能与 04 §12 出现命名差异。

**建议**：在 chat-minimal 02 §5 增加 `agent_audit_log` 最低字段说明，或显式引用 "字段语义参见 04 §12"。

### 5.2 [P1] `agent_capability` / `agent_capability_binding` 在 chat-minimal 用途模糊

chat-minimal 不做 admin-capability 后台，但数据切片包含两表，最小 Capability Gate（02 §7）依赖"Capability 是 enabled、审核通过、绑定到当前 Agent"。这意味着：

- POC-1 必须有种子 Capability 与 binding 记录（同 provider / model 的 Flyway seed），但 02 §7 没明确声明。
- riskLevel=low 的"普通 Chat capability"具体指哪个 capability code？（默认对话能力？空 capability？）

**建议**：在 02 §7 "最小 Capability Gate"段下追加"种子 Capability"段，明确 chat-minimal 至少有一条 enabled + 审核通过 + low 风险的 capability，bind 到默认 Agent。

### 5.3 [P1] `tool` 角色消息在 chat-minimal SSE event 中的展示语义未定

03 §7 `message.created` payload 包含 `role: user|assistant|tool`，chat-minimal 02 §3 SSE 切片同时保留 `tool.call.*` 事件。tool 既以独立 message 出现，又以 tool_call 过程块出现，前端如何区分两者展示？

**建议**：在 02 §8 增加一段"tool 消息与 tool_call 事件展示边界"，明确：
- `tool` role message 仅作为执行详情面板的工具调用结果记录。
- 普通 Chat 消息流（用户视角）只通过 `tool.call.*` 事件展示工具调用。
- 前端不渲染 `role=tool` 的 message 在主聊天泡。

### 5.4 [P1] AgentScope `HINT` 事件与 Memory / RAG 的脱敏边界

03 §9 RuntimeEvent 映射表把 `HINT` 默认归到 Run/Span/Audit、不向普通用户展示。但：

- 当 `chat-minimal` 后续启用 memory（即使是 Agent 默认 memory），HINT 事件会包含敏感片段。
- 没有声明 HINT 在执行详情面板（管理员视角）下的展示策略；contract-notes-001 §4 写"`tool.call.*` 在管理员执行详情中可见的原始参数范围，需要后续权限和审计任务继续细化"，但 HINT 同样需要细化。

**建议**：把 HINT 与 tool.call 原始参数合并为同一个 OQ：`OQ-V3-009 HINT/原始 tool 参数 在管理员执行详情的展示边界`，由 platform-security / platform-observability 后续任务细化。

### 5.5 [P1] architecture test 的具体规则集未在 02 / 14 任务中明示

TASK-V3-014 architecture test 任务说"验证 agent-domain 不依赖基础设施、agent-api 不依赖 runtime-spi"，但：

- 02 §6 文字描述了 11 条架构不变量，没有列举 architecture test 的具体规则名。
- ArchUnit 与 Modulith 等可选实现路径没有点名。

dpsk P0-5 已隐含提示。建议在 TASK-V3-014 task.md 输出契约中追加一个"规则名清单"区，让规则与不变量一一对应（domain-no-spring / api-no-runtime-spi / biz-no-mapper / platform-no-bizadmin / runtime-no-repo / boot-no-business 等）。

### 5.6 [P2] RTK 任务追踪与 REQ 引用断裂

REQ-V3-002（业务老师按标签选择 Agent）映射到 TASK-V3-020 / 024 / 040，但：

- TASK-V3-020 是 runtime-agentscope（与"按标签选择 Agent"业务关系弱）。
- TASK-V3-022 biz-chat 才是承载 Chat 主链路、TASK-V3-024 前端是承载选 Agent 体验、TASK-V3-040 是任务中心（属 EPIC-V3-C）。

**建议**：把 REQ-V3-002 关联任务调整为 TASK-V3-020 / 022 / 024（chat-minimal 范围内）+ TASK-V3-040（task-center），并在 RTK §3.1 EPIC 表里增加 REQ → 小工程的映射列。

### 5.7 [P2] 03 §2 "参考项目对照表" 未把当前 fork 的 commit / 文档 URL 标注到行级

03 §2 表格列了 10 个参考项目，但只有"参考对象 / 参考内容 / V3 采纳 / 不采纳"四列。结合 §2.2 要求"代码级接口引用"，建议给参考表增加"版本 / 源码或文档定位"两列，方便后续 AI agent 直接定位实际类与方法。

### 5.8 [P2] `metadata` JSON 字段缺约定

`agent_definition`、`agent_session`、`agent_message`、`agent_run`、`agent_message_attachment`、`agent_input_derived_context`、`agent_audit_log` 等多张表均有 `metadata`，但只有"非敏感"约束，没有：

- 顶层 key 命名规范（snake_case / camelCase）。
- 保留 key 列表（如 `testRunId`、`uiHints`、`featureFlags`）。
- 大小限制。

**建议**：在 04 §3 数据约束之后新增"metadata 通用约定"小节。

### 5.9 [P2] 文档"必须阅读"清单的精读 / 了解分级

dpsk 评审 §3.8 已提出。当前 AI 任务单"必须阅读"普遍 12-14 个文件，POC-1 任务执行时上下文压力较大。

**建议**：在 06 §3 "AI 任务单模板"中加入分级建议：精读（任务直接消费）/ 了解（背景，可仅读相关章节）。

### 5.10 [P2] 02 §2 架构不变量与 ADR 关联缺失

02 §2 列出 11 条架构不变量，但与 ADR-0001（AgentScope 默认底座）、ADR-0002（持久化边界）没有显式索引。

**建议**：在 02 §2 末尾追加一行 ADR 索引：`ADR-0001 → 第 1、3、4 条；ADR-0002 → 第 9 条`。

### 5.11 [P2] 子 Agent 完成报告与 Code Review 模板独立化

dpsk §3.11 已提出。06 §6 子 Agent 输出格式与 §12 Review Agent 检查清单都嵌在文档正文，没有独立模板。

**建议**：在 templates/ 增加 `子Agent报告-模板.md` 和 `Review-检查清单-模板.md`，与现有四份模板对齐。

---

## 六、AI Native 适用性评估（更新版）

| 维度 | v1 评分 | v2 评分 | 变化说明 |
| --- | --- | --- | --- |
| 任务边界清晰度 | 9 | 9 | 维持高水平，POC-1 的 owned files 互斥可执行 |
| 契约可实现性 | 8 | 9 | RuntimeRequest/RuntimeEvent 归属、SSE 恢复、并发约束、HTTP 映射均已落地，剩 heartbeat 间隔 |
| 阅读路径可导航 | 8 | 8 | 编号已修复，但跨小工程与全局基线交叉引用仍需多次跳转 |
| 验收可判定性 | 9 | 9 | L0-L5 + 验证命令依旧明确 |
| 并行隔离性 | 9 | 9 | 共享文件冲突处理已写入 06 §5、§7；contract owner 唯一性可执行 |
| 上下文负担 | 6 | 6 | 单任务必须阅读清单仍长，未做精读 / 了解分级 |
| 形式化精度 | 6 | 7 | DTO/SSE 字段语义与 HTTP 映射在 chat-minimal 切片已写明；DDL 类型与 OpenAPI 仍延后到 POC-1 通过 Java 类型承载 |
| 工程可启动性 | 6 | 8 | POC-1 任务包齐备，前置阻断为 §三 4 项；POC-2 仍待 §四 8 项 |

**总体结论**：文档体系已可支撑 AI Agent 启动 POC-1 任务（先解决 §三 4 项）；POC-2 启动前必须收敛 §四 列出的 8 项。POC-2 任务包当前未创建是合理延后，但应在 POC-1 进行到 50% 时并行启动 POC-2 任务包预设计（与 v1-claude / kimi 建议一致）。

---

## 七、按阶段汇总的修复优先级

### 7.1 POC-1 启动前必修（4 项，§三）

1. ADR-0003 工具链选型（Spring Boot 4.0.6 + Maven 4 RC + MyBatis-Plus 兼容性）。
2. RTK 锁定 AgentScope Java / Runtime Java fork 的 commit hash。
3. 修正 EPIC-V3-A 状态为 `Review`（而非 In Progress）。
4. 03 §12 错误码表追加 HTTP 状态码列。

### 7.2 POC-2 启动前必修（8 项，§四）

5. chat-minimal 03 增加最小身份/认证方案（含 SSE 鉴权与 testRunId 注入）。
6. chat-minimal 03 §4 / 新 ADR 补全前端栈（构建工具 / UI 库 / SSE 客户端 / 路由）。
7. 07 §8 之后新增"本地依赖搭建"段（PostgreSQL / .env / Testcontainers 镜像）。
8. chat-minimal 04 增加测试 fixture 方案 + cross-cutting 创建 TASK-V3-900 / 901 任务包目录。
9. chat-minimal 04 §6 增加 LLM 最低能力声明 + 失败降级条件。
10. 03 §6 声明默认 heartbeat 间隔与客户端断线阈值。
11. chat-minimal 02 §7 增加"IAM seed"段。
12. RTK 新增 `OQ-V3-008 WorkflowRun.waiting` Open 项。

### 7.3 非阻塞但建议在 POC-2 启动前一并处理（5 项，§五选录）

13. chat-minimal 02 §5 补 `agent_audit_log` 最低字段（或显式引用 04 §12）。
14. chat-minimal 02 §7 增加"种子 Capability"段（默认对话 capability 名称）。
15. chat-minimal 02 §8 明确 tool message 与 tool.call 事件的展示边界。
16. RTK 新增 `OQ-V3-009 HINT/tool 原始参数 管理员展示边界`。
17. TASK-V3-014 task.md 输出契约追加 architecture test 规则名清单。

### 7.4 长期改进（5 项，§五其余）

18. RTK §3.1 EPIC 表增加 REQ → 小工程映射列；REQ-V3-002 关联任务调整。
19. 03 §2 参考项目对照表增加"版本 / 源码或文档定位"两列。
20. 04 §3 增加 `metadata` 通用约定。
21. 06 §3 必须阅读分级（精读 / 了解）。
22. templates/ 新增子 Agent 报告与 Review 检查清单模板；02 §2 关联 ADR 索引。

---

## 八、对四份 v1 评审本身的质量复核

| 评审 | 准确性 | 操作性 | 冗余度 | 当前价值 |
| --- | --- | --- | --- | --- |
| v1-claude | 高，问题分级清晰 | 高，建议位置具体 | 低 | 可作为 POC-1 验收 checklist 的基准 |
| v1-glm5.1 | 高，B/I/S 分级与表格映射好 | 极高，给到具体修复位置 | 低 | 已基本被 v2 文档吸收 |
| v1-kimi2.6 | 高，覆盖技术栈风险 | 高，给到 P0/P1 行动项 | 中（评分维度部分重复） | Spring Boot 风险与 biz-execution 缺位的提示价值最高 |
| v1-dpsk-v4 | 高，强调形式化与并行协调 | 中（部分建议落到工具层面，需进一步翻译） | 中 | 形式化建议（OpenAPI/JSON Schema/DDL）已被部分吸收，仍需推进 |

四份评审整体一致性较高，未出现彼此矛盾的判断。本评审 v2 已在 §二 中复核每条 v1 意见的处理状态，未发现"v1 已修复但 v2 又重新引入"的回归。

---

## 九、评审结论

文档体系作为后续 AI Native 编程的基础**已具备启动 POC-1 的成熟度**，但需要先完成 §七 7.1 列出的 4 项 POC-1 启动前必修项；POC-2 启动前必须再完成 §七 7.2 的 8 项。剩余 §七 7.3 与 7.4 不阻塞但建议在 POC-2 任务包创建前一并处理。

人工评审应聚焦：

1. ADR-0003 工具链选型是否能在 1 周内完成兼容性验证。
2. AgentScope Java fork commit 是否能立即锁定。
3. EPIC-V3-A 状态是否回退到 `Review`。
4. POC-1 各任务的"必须执行的验证命令"是否能在工程骨架创建后真实通过。

文档系统当前的核心约束（不自研 ReAct、不绕过 AgentScope 触发工具、按动作风险判定 sandbox、双持久化边界、testRunId 隔离、L0-L5 验收）保持稳定且互不矛盾，是后续 AI Native 多 Agent 并行开发能否成立的基础。建议在 POC-1 第一个任务（TASK-V3-010 Maven 多模块骨架）完成后即进行一次 v3 复评，验证全局基线在真实代码出现后是否仍然自洽。

---

## 附录：评审覆盖文件清单

```text
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/01-产品蓝图与体验目标.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/07-开发规范与本地环境.md
docs/v3-agent中台/08-小工程切片与验收门禁.md
docs/v3-agent中台/templates/AI任务单-模板.md
docs/v3-agent中台/templates/ADR-模板.md
docs/v3-agent中台/templates/测试报告-模板.md
docs/v3-agent中台/templates/交接记录-模板.md
docs/v3-agent中台/工程迭代/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/01-工程范围与验收目标.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
docs/v3-agent中台/工程迭代/01-chat-minimal/03-工程技术设计.md
docs/v3-agent中台/工程迭代/01-chat-minimal/04-工程测试策略与真实E2E.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/01-chat-minimal/adr/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/adr/ADR-0001-AgentScope普通Chat默认执行底座.md
docs/v3-agent中台/工程迭代/01-chat-minimal/adr/ADR-0002-MyBatis-Plus与R2DBC持久化边界.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-000-文档索引复核/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/contract-notes.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/contract-notes.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-010-Maven多模块骨架/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-011-agent-domain契约/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-012-runtime-spi契约/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-013-repo-spi契约/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-014-architecture-test/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-015-agent-api契约/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-2/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-902-AGENTS分层规则/task.md
docs/v3-agent中台/评审/v3文档评审-v1-claude.md
docs/v3-agent中台/评审/v3文档评审-v1-glm5.1.md
docs/v3-agent中台/评审/v3文档评审-v1-kimi2.6.md
docs/v3-agent中台/评审/v3文档评审-v1-dpsk-v4.md
```
