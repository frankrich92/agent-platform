# V3 Agent 中台文档评审 v2

> 评审模型：GLM-5.1
> 评审日期：2026-05-19
> 评审范围：`docs/v3-agent中台/` 全局基线文档（01-08）、RTK、templates、工程迭代目录
> 评审基线：基于 v1 四方评审意见（Claude、DeepSeek-V4、GLM-5.1、Kimi2.6），在文档迭代后重新审核
> 评审目的：作为后续 AI Native 编程的基础文档，评审其一致性、完整性、可执行性和 AI Native 适配性

---

## 评审总结

文档体系在 v1 评审后已做了重要改进：编号跳跃已修复（03 编号用于 API-SSE-运行时契约）、`visibility_scope` 已补充枚举、`agent-definition.runtime_type` 与 `RuntimeCapabilityPlan.runtimeType` 关系已明确、`RuntimeRequest`/`RuntimeEvent` 归属已声明为 `runtime-spi`。

本次 v2 评审重点关注：(1) v1 遗留问题是否已闭环；(2) 文档间新增的一致性问题；(3) 对 AI Native 编程的可执行性。

| 级别 | 数量 | 说明 |
| --- | --- | --- |
| 阻断性 | 2 | 不解决将导致 AI agent 执行时产生歧义或错误 |
| 重要 | 7 | 影响文档可执行性或后续工程效率 |
| 建议 | 8 | 可提升文档质量和可维护性 |
| 亮点 | 4 | v1 后新增或确认的优秀设计 |

---

## 一、v1 遗留问题闭环追踪

| v1 编号 | 问题 | v2 状态 | 说明 |
| --- | --- | --- | --- |
| B-01 | 编号跳跃缺 03 | **已修复** | 03 编号已分配给 API-SSE-运行时契约-全局基线 |
| B-02 | Runtime 契约类归属模糊 | **已修复** | 03 第 1 节明确声明 RuntimeRequest/RuntimeEvent/RuntimeCapabilityPlan/RuntimeResult 属于 runtime-spi |
| B-03 | Session 并发状态约束缺失 | **未修复** | 仍缺少同一 session 是否允许并发 run 的声明、SESSION_BUSY 判定时机和数据层保证手段 |
| I-01 | checkpoint 表结构缺失 | **未修复** | `agent_message_checkpoint` 的最低字段和用途仍未定义 |
| I-02 | 前端技术栈未冻结 | **部分修复** | 08 中已写明 Vue 3 + TypeScript + Pinia，但 RTK OQ-V3-005 仍为 Review 状态而非 Done |
| I-03 | visibility_scope 枚举缺失 | **已修复** | 04 第 4 节已补充 private/granted_users/role_scope/public_enabled 枚举 |
| I-04 | runtime_type 语义重叠 | **已修复** | 03 和 04 多处明确声明 agent_definition.runtime_type 为默认偏好，RuntimeCapabilityPlan.runtimeType 为最终决策 |
| I-05 | 错误码与 HTTP 状态码映射缺失 | **未修复** | 仍无业务错误码到 HTTP 状态码的映射表 |
| I-06 | biz-execution 归属不明确 | **未修复** | 属于 chat-minimal 工程目录问题，全局基线层面无此问题 |
| I-07 | Provider/Model 初始化方式未声明 | **未修复** | 同上，属于小工程层面 |
| I-08 | agent_version 用途未说明 | **未修复** | 同上，属于小工程层面 |

**小结**：v1 的 3 个阻断性问题中 B-01、B-02 已修复，B-03 未修复。8 个重要问题中 3 个已修复，5 个未修复（其中 3 个属于小工程层面）。B-03 仍是阻断性问题，必须在 POC-2 前解决。

---

## 二、阻断性问题

### B2-01 Session 并发语义仍未定义

v1 B-03 遗留。这是 Chat 最核心的并发安全点。当前文档中：

- 03 第 5 节 `POST /api/chat/send` 未声明同一 `sessionId` 是否允许并发 run
- 04 `agent_session` 表定义中无 `current_run_id` 或 `status` 字段来承载并发锁语义
- 错误码 `SESSION_BUSY` 存在但无判定时机定义
- 02 第 8.3 节 Stream 持久化链路假设 sequence 单调递增，但未说明如何保证同一 run 内的 sequence 原子性

- **影响**：两个请求同时发送到同一 session 时，AI agent 实现可能选择不同策略（乐观锁、悲观锁、状态检查），导致行为不一致或数据损坏。
- **建议**：在 03 第 5 节补充以下语义：
  - 声明同一 session 同一时刻只允许一个 active run
  - `SESSION_BUSY` 在 `POST /api/chat/send` 入口判定，不等到 runtime 提交
  - `agent_session` 表至少需要支持原子性状态检查或行级锁

### B2-02 SSE 断线恢复交互协议不完整

v1 S-01 遗留并提升为阻断性。03 第 6 节定义了 `Last-Event-ID` 和 `sequence` 单调递增，但以下交互细节缺失：

1. 恢复请求是重新 `GET /api/chat/{runId}/stream` 并带 `Last-Event-ID` header？
2. 服务端返回范围：从 `Last-Event-ID` 之后所有已持久化事件？还是只返回 checkpoint 间隔内事件？
3. 如果 run 已 completed/failed/cancelled，恢复请求返回什么？
4. 如果客户端断线超过 run 生命周期，如何恢复？

- **影响**：SSE 断线恢复是 chat-minimal L5 验收项。没有明确协议，前后端 AI agent 并行开发时会实现不一致。
- **建议**：在 03 第 6 节补充断线恢复的完整交互协议，至少包含以上 4 种场景。

---

## 三、重要问题

### I2-01 错误码与 HTTP 状态码映射缺失

v1 I-05 遗留。03 第 12 节定义了 28 个业务错误码，但未映射到 HTTP 状态码。前后端并行开发时 HTTP 状态码是接口契约的一部分。

建议最低映射：

```text
AGENT_NOT_FOUND / SESSION_NOT_FOUND / RUN_NOT_FOUND / TASK_NOT_FOUND / WORKFLOW_NOT_FOUND -> 404
AUTH_REQUIRED -> 401
PERMISSION_DENIED / AGENT_NOT_AUTHORIZED / CAPABILITY_NOT_AUTHORIZED -> 403
SESSION_BUSY -> 409
RATE_LIMITED -> 429
RUNTIME_UNAVAILABLE / EXTERNAL_AGENT_UNAVAILABLE / OCR_UNAVAILABLE -> 503
VALIDATION_FAILED / ATTACHMENT_UNSUPPORTED / ATTACHMENT_TOO_LARGE -> 400
INTERNAL_ERROR -> 500
其他 -> 按语义补充
```

### I2-02 `agent_message_checkpoint` 表结构仍未定义

v1 I-01 遗留。04 第 3 节列出了 `agent_message_checkpoint`，但全文未给出该表的最低字段、与 `agent_run.sequence` 的关系、与 SSE `eventId` 的映射。

R2DBC 负责"checkpoint / final flush"（04 第 8 节），但 checkpoint 表与 `repo-r2dbc-stream` 的端口定义之间缺少桥梁。当 `repo-agent` 和 `platform-stream` 并行开发时，双方对 checkpoint 表结构的理解可能不一致。

- **建议**：在 04 第 3 节补充 `agent_message_checkpoint` 的最低字段（至少包含 `checkpoint_id`、`run_id`、`sequence`、`event_type`、`event_id`、`payload_json`、`created_at`）和与 SSE `eventId` 的映射关系。

### I2-03 前端技术栈决策应关闭 OQ-V3-005

08 第 1 节已明确写明"Vue 3 + TypeScript + Pinia"，07 第 1 节也有相同声明。但 RTK OQ-V3-005 仍为 Review 状态而非 Done。

- **影响**：AI agent 读取 RTK 时可能认为前端技术栈尚未决定，在任务单中标注为"待确认"。
- **建议**：将 OQ-V3-005 状态更新为 Done，决策内容记录为"V3 前端基线：Vue 3 + TypeScript + Pinia；组件库和旧实现复用策略在 POC-2 前确认"。

### I2-04 `regenerate` 与 `edit-resend` 语义差异未定义

v1 S-02 遗留并提升为重要。03 第 4 节列出了 `POST /api/chat/{messageId}/regenerate` 和 `POST /api/chat/{messageId}/edit-resend`，但行为差异未定义：

- `regenerate`：是否复用原 message 的 agentId 和 sessionId？是否生成新 runId？原 message 状态如何变化？
- `edit-resend`：请求体是否包含编辑后文本？原 message 是标记为 cancelled 还是保留？用户侧看到的效果与 regenerate 有何不同？

- **影响**：这是 Chat 工作台的核心交互，AI agent 实现时语义理解不一致将导致前端行为差异。
- **建议**：在 03 第 5 节或新增 5.2/5.3 子节，定义两个接口的请求/响应结构和语义差异。

### I2-05 `agent_run` 和 `agent_span` 最低字段缺失

v1 S-03 遗留并提升为重要。04 第 6 节定义了 Run/Span 状态枚举，03 第 9 节 `RuntimeEvent` 有 `spanId`，但 `agent_run` 和 `agent_span` 表没有最低字段定义。`agent_audit_log` 有明确字段，但 Run/Span 没有。

- **影响**：`repo-agent` 和 `platform-observability` 并行开发时，对 `agent_run`/`agent_span` 的表结构理解可能不一致。`GET /api/executions/{runId}` 的返回结构也缺少数据层支撑。
- **建议**：在 04 第 3 节补充 `agent_run` 和 `agent_span` 的最低字段定义。

### I2-06 Maven 版本在 WSL2 环境中不一致

08 第 8 节 macOS 参考使用 `maven/4.0.0-rc-5`，WSL2 参考使用 `apache-maven-4.0.0-rc-5`。目录名一致，但需确认两个环境的 Maven 版本确实相同。

更关键的是：Spring Boot 4.0.6 + Maven 4.x RC 组合是非主流选择。v1 Claude 评审指出 Spring Boot 4 的生态风险（MyBatis-Plus 兼容性、第三方库成熟度），但尚未被 ADR 或技术设计文档回应。

- **建议**：补充 Spring Boot 4 选型 ADR 或在 RTK 开放问题中登记此项风险。

### I2-07 认证方案仍为空白

v1 Claude 评审指出 `chat-minimal` 范围包含"最小用户身份和 Agent 授权校验"，但全局基线和 chat-minimal 均未说明认证方式（JWT / Session / OAuth）、Token 刷新策略和 SSE 长连接认证保持。

04 第 10 节权限模型定义了 `UserContext`、`AgentGrant` 等，但未说明 `UserContext` 如何从 HTTP 请求中获取。

- **影响**：POC-2 前后端并行开发时，认证方式直接影响 API 调用方式和 SSE 连接建立。缺少认证方案将导致前端无法正确实现登录和请求鉴权。
- **建议**：在 03 或 chat-minimal 技术设计中补充最小认证方案声明。

---

## 四、建议改进

### S2-01 建议在 03 中补充 SSE heartbeat 间隔定义

03 第 7 节定义了 `heartbeat` 事件类型，但未指定发送间隔。前端需要知道多久没收到事件视为连接断开。建议声明默认间隔（如 15 秒或 30 秒），并声明可配置。

### S2-02 建议在 03 中补充 API 分页约定

03 第 3 节定义了分页响应结构（`items`/`page`/`pageSize`/`total`），但未声明：

- `page` 从 0 还是 1 开始
- `pageSize` 默认值和最大值
- 是否支持游标分页（对于消息列表等高频场景）

建议补充最小分页约定。

### S2-03 建议在 04 中补充审计日志保留策略

04 第 12 节定义了审计 action 和字段，但未涉及保留期和归档策略。对于生产环境，审计日志会快速增长。建议至少声明 POC 阶段不做保留期限制，后续作为 hardening 阶段补充。

### S2-04 建议统一 07 任务单模板与 templates/AI任务单-模板.md

07 第 3 节包含一个内联任务单模板，`templates/AI任务单-模板.md` 也有一个独立模板。两者结构基本一致但有细微差异（07 多了"并行开发计划"表格，templates 多了"状态"字段）。建议以 templates 为唯一权威，07 只引用不内联，避免 AI agent 选用不同模板。

### S2-05 建议在 08 中补充 AgentScope fork 版本锁定

文档多处引用 `source/fork_source/agentscope-java` 和 `source/fork_source/agentscope-runtime-java` 作为接口参考，但未锁定 fork 基于哪个 commit/tag。RTK K-V3-002 和 K-V3-003 引用了源码路径但未记录版本基线。接口变更时无法判断是否需要更新映射。

### S2-06 建议在 02 中补充部署架构候选

02 聚焦开发架构，但缺少部署架构声明。PostgreSQL 在 E2E 和 R2DBC 中出现，但未在架构文档正式声明为数据库选型。单体部署 vs 微服务部署形态、缓存/消息队列等中间件需求也未涉及。建议至少在 02 补充 POC 部署形态声明。

### S2-07 建议在 06 中补充 Playwright 配置基线

v1 S-06 遗留。06 和 chat-minimal/04 都要求 Playwright UI E2E，但未定义基础配置（baseURL、超时、重试策略）、认证方式（测试用户如何登录）和 `testRunId` 在 Playwright 测试中的传递方式。

### S2-08 建议在 07 中补充子 Agent 发现未授权文件的处理流程

v1 S-04 遗留。07 第 4 节要求"如果必须修改未授权文件，先停下来说明原因"，但未定义说明后的处理流程。对于 AI agent 自动化执行，建议补充：在 `contract-notes.md` 中记录并继续其他工作，同时通知主 Agent 授权修改范围；主 Agent 决定是否追加 owned files 或另建任务。

---

## 五、亮点确认

### H2-01 `agent_definition.runtime_type` 与 `RuntimeCapabilityPlan.runtimeType` 关系已明确

v1 后的改进。03 和 04 多处明确声明：`agent_definition.runtime_type` 是管理端配置的默认偏好或约束输入，`RuntimeCapabilityPlan.runtimeType` 是 `platform-capability` 基于本次请求、用户授权、能力风险和环境健康做出的最终执行决策。两者关系清晰，AI agent 不会混淆。

### H2-02 `visibility_scope` 枚举已补充

v1 后的改进。04 第 4 节已补充 `private`/`granted_users`/`role_scope`/`public_enabled` 枚举及语义说明。同时声明"可见范围只解决能否看到或选择 Agent，不替代 Capability 授权、runtime 风险判定或审计规则"，边界清晰。

### H2-03 OCR / input-preprocessor 纳入 Capability gate 的设计完整

01、02、03、04 四份文档中，OCR / input-preprocessor 纳入 Capability gate 的约束形成完整闭环：产品蓝图声明 OCR 必须受控 → 架构方案定义 OCR 链路 → 契约定义 `allowedInputPreprocessors` 和 `attachmentPolicy` → 数据模型定义附件和派生上下文的审计字段。这是跨文档一致性设计的优秀范例。

### H2-04 参考项目采纳/不采纳表 + 代码级接口引用要求

v1 确认的亮点，v2 确认仍然有效。03 第 2 节的参考项目对照表和第 2.2 节的代码级接口引用要求，配合 07 的 `contract-notes.md` 模板，形成了"引用必须精确到代码符号"的执行闭环。这对 AI agent 尤其重要——避免"参考了 XX 产品"的模糊描述。

---

## 六、各文档评审详情

### README.md

- **状态**：结构清晰，阅读路径和按任务类型阅读设计合理。03 编号已修复。
- **问题**：无阻断性问题。
- **建议**：在文档清单下方增加 templates 索引入口，便于新贡献者发现模板文件。

### RTK.md

- **状态**：追踪表设计完整，需求→任务→契约→测试→知识→决策的链路清晰。OQ-V3-007 已关闭。
- **问题**：
  - OQ-V3-005 应更新为 Done（见 I2-03）。
  - EPIC-V3-A 状态为 `In Progress`，但 POC-0 全部任务为 `Review`、POC-1 为 `Planned`。按状态规则建议标注当前阶段或调整为 `Review`。
- **建议**：在核心能力追踪表中，标注 `CAP-V3-002`（Chat SSE）与 `CAP-V3-005`（SSE 持久化）的依赖关系。

### 01-产品蓝图与体验目标.md

- **状态**：产品定位清晰，目标用户分层合理，"暂不追求"列表划定了合理边界。图片型输入 OCR 预留说明完整。
- **问题**：无阻断性问题。
- **建议**：第 9 节产品验收口径中"管理员能登记外部复杂 Agent"和"管理员能把多个已有 Agent 编排成顺序工作流"不属于 chat-minimal 范围，建议标注"后续小工程验收"。

### 02-Agent中台技术架构方案.md

- **状态**：架构不变量、模块地图、依赖方向、核心链路设计完整且自洽。小工程架构使用方式设计合理。`RuntimeCapabilityPlan` 字段列表是亮点。
- **问题**：
  - 缺少部署架构候选（见 S2-06）。
  - 缺少数据库选型正式声明（PostgreSQL 在 E2E 和 R2DBC 中出现，但未在架构文档正式声明）。
- **建议**：在第 8 节或新增第 13 节补充 POC 部署形态和数据库选型声明。

### 03-API-SSE-运行时契约-全局基线.md

- **状态**：契约设计详尽，参考项目对照表是亮点。SSE Envelope 设计和错误码体系完整。`RuntimeRequest`/`RuntimeEvent` 归属已声明。
- **问题**：
  - Session 并发语义缺失（见 B2-01）。
  - SSE 断线恢复协议不完整（见 B2-02）。
  - 错误码与 HTTP 状态码映射缺失（见 I2-01）。
  - `regenerate`/`edit-resend` 语义差异未定义（见 I2-04）。
  - heartbeat 间隔未定义（见 S2-01）。
  - 分页约定不完整（见 S2-02）。
- **建议**：这是 AI agent 编程时参考频率最高的文档，以上缺失将直接影响前后端并行开发的契约一致性。建议优先修复 B2-01、B2-02 和 I2-01。

### 04-数据模型与安全审计-全局基线.md

- **状态**：数据域、安全原则、Capability Gate、审计模型设计完整。MyBatis/R2DBC 双持久化边界清晰。Flyway 迁移策略合理。`visibility_scope` 枚举已补充。
- **问题**：
  - `agent_message_checkpoint` 表结构缺失（见 I2-02）。
  - `agent_run`/`agent_span` 最低字段缺失（见 I2-05）。
  - 审计日志保留策略缺失（见 S2-03）。
- **建议**：补充 `agent_run`、`agent_span`、`agent_message_checkpoint` 的最低字段定义。

### 05-测试策略与验收等级-全局基线.md

- **状态**：L0-L5 分层设计清晰，测试数据隔离要求合理，CI 质量门禁定义完整。
- **问题**：Playwright 配置基线缺失（见 S2-07）。
- **建议**：补充 Playwright 基础配置建议和 `testRunId` 传递方式。

### 06-AI-Native交付规约.md

- **状态**：AI 任务单模板设计完整，owned files 机制是亮点。契约合并规则和 Review Agent 检查清单实用。
- **问题**：
  - 与 templates/AI任务单-模板.md 存在差异（见 S2-04）。
  - 子 Agent 发现未授权文件的处理流程缺失（见 S2-08）。
- **建议**：统一任务单模板权威来源，补充未授权文件处理流程。

### 07-开发规范与本地环境.md

- **状态**：基础规范清晰，本地工具链配置完整。Agent 能力开发规范完整。
- **问题**：前端技术栈"Vue 3"与 OQ-V3-005 的 Review 状态矛盾（见 I2-03）。
- **建议**：确认前端技术栈后关闭 OQ-V3-005。

### 08-小工程切片与验收门禁.md

- **状态**：小工程拆分原则设计合理，并行开发门禁清晰。Epic 拆分覆盖了 V3 核心业务闭环。
- **问题**：无阻断性问题。
- **建议**：在已启动小工程索引中考虑增加"启动日期"和"当前阶段"列。

### templates/ 目录

- **ADR 模板**：结构完整，编号规则清晰。无问题。
- **AI 任务单模板**：与 07 内联模板有细微差异（见 S2-04）。
- **测试报告模板**：偏简略。建议增加"环境信息"字段（Java 版本、OS、数据库版本）。
- **交接记录模板**：偏简略。建议增加"契约合并说明"字段（与 06 第 7 节 handoff 要求对齐）。

---

## 七、阻断性问题修复优先级

| 编号 | 问题 | 建议修复位置 | 建议修复时机 |
| --- | --- | --- | --- |
| B2-01 | Session 并发语义缺失 | 03 第 5 节 + 04 agent_session 字段 | POC-2 创建任务包前 |
| B2-02 | SSE 断线恢复协议不完整 | 03 第 6 节 | POC-2 创建任务包前 |

---

## 八、重要问题修复优先级

| 编号 | 问题 | 建议修复位置 | 建议修复时机 |
| --- | --- | --- | --- |
| I2-01 | 错误码与 HTTP 状态码映射缺失 | 03 第 12 节 | POC-2 创建任务包前 |
| I2-02 | checkpoint 表结构缺失 | 04 第 3 节 | POC-1 repo-spi 任务前 |
| I2-03 | OQ-V3-005 应关闭 | RTK | 立即 |
| I2-04 | regenerate/edit-resend 语义差异 | 03 第 5 节 | POC-2 创建任务包前 |
| I2-05 | agent_run/agent_span 字段缺失 | 04 第 3 节 | POC-1 repo-spi 任务前 |
| I2-06 | Spring Boot 4 选型风险未评估 | ADR 或 RTK | POC-1 启动前 |
| I2-07 | 认证方案空白 | 03 或 chat-minimal/03 | POC-2 启动前 |

---

## 九、对 AI Agent 执行的可用性评估

| 维度 | v1 评分 | v2 评分 | 变化说明 |
| --- | --- | --- | --- |
| 任务边界清晰度 | 9/10 | 9/10 | owned files + 禁止修改 + 验证命令仍然有效 |
| 契约可实现性 | 8/10 | 8.5/10 | Runtime 归属已明确，visibility_scope 已补充；但 Session 并发和断线恢复仍缺 |
| 阅读路径可导航 | 8/10 | 8/10 | README 阅读顺序明确，03 编号已修复 |
| 验收可判定性 | 9/10 | 9/10 | L0-L5 等级 + 具体命令，完成标准客观 |
| 并行隔离性 | 9/10 | 9/10 | owned files 互斥 + contract-notes 隔离 |
| 上下文负担 | 6/10 | 6/10 | 全量文档仍然超过 5 万字，单个 Agent 必须阅读列表较长 |

**总体结论**：文档体系可以支撑 AI Agent 执行 POC-1 任务。POC-2 启动前需解决 B2-01（Session 并发）和 B2-02（SSE 断线恢复），这两项直接影响前后端并行开发的契约一致性。

---

## 十、评审结论

### 已通过

- [x] 文档结构分层合理，权威归属明确
- [x] AI Native 规约可操作，防护机制完整
- [x] chat-minimal 范围足够小，可独立验收
- [x] 契约切片能支撑 POC-1 后端骨架开发
- [x] 阶段门禁和状态追踪机制健全
- [x] v1 阻断性问题 B-01、B-02 已修复

### 需要在 POC-2 启动前解决

1. **B2-01** Session 并发语义：声明同一 session 同一时刻只允许一个 active run，定义 SESSION_BUSY 判定时机
2. **B2-02** SSE 断线恢复协议：补充完整交互协议
3. **I2-01** 错误码 HTTP 映射：补充业务错误码到 HTTP 状态码的映射表
4. **I2-04** regenerate/edit-resend 语义：定义两个接口的请求/响应和行为差异
5. **I2-07** 认证方案：补充最小认证方案声明

### 建议在 POC-1 启动前解决

6. **I2-02** checkpoint 表结构：补充 agent_message_checkpoint 最低字段
7. **I2-05** agent_run/agent_span 字段：补充最低字段定义
8. **I2-03** 关闭 OQ-V3-005：确认前端技术栈决策
9. **I2-06** Spring Boot 4 选型风险：补充 ADR 或登记开放问题

### 非阻塞性改进建议

10. 补充 SSE heartbeat 间隔定义
11. 补充 API 分页约定
12. 统一 06 内联模板与 templates 为唯一权威
13. 补充 AgentScope fork 版本锁定
14. 补充部署架构候选
15. 补充 Playwright 配置基线
16. 补充子 Agent 未授权文件处理流程

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
```
