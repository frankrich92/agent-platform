# V3 Agent 中台文档评审 v1

> 评审模型：GLM-5.1
> 评审日期：2026-05-19
> 评审范围：`docs/v3-agent中台/` 全部文档（含全局基线、小工程目录、模板）
> 评审目的：作为后续 AI Native 编程的基础文档，评审其一致性、完整性、可执行性和 AI Native 适配性

---

## 评审总结

文档体系整体设计成熟，小工程切片 + 契约冻结 + AI 任务单 + L0-L5 验收的框架完备且自洽。以下评审意见按严重程度排序，分为"阻断性问题"、"重要问题"、"建议改进"和"亮点确认"四个层级。

| 级别 | 数量 | 说明 |
| --- | --- | --- |
| 阻断性 | 3 | 不解决将导致 AI agent 执行时产生歧义或错误 |
| 重要 | 8 | 影响文档可执行性或后续工程效率 |
| 建议 | 6 | 可提升文档质量和可维护性 |
| 亮点 | 5 | 确认设计优秀、值得保持的做法 |

---

## 一、阻断性问题

### B-01 编号跳跃：缺少 03 编号文档

文档清单中编号从 `02-Agent中台技术架构方案.md` 直接跳到 `04-API-SSE-运行时契约-全局基线.md`，缺少 `03-*`。README.md 的文档清单和阅读路径中也未说明原因。

- **影响**：AI agent 读取文档清单时可能认为遗漏了文档，或在任务单中引用"03 文档"时产生歧义。
- **建议**：在 README.md 文档清单中标注"03 编号已废弃/预留"的原因，或重新编号消除跳跃。如果原 03 编号已被合并到 02，应显式声明。

### B-02 `agent-domain` 与 `agent-api` 边界在契约层面存在模糊地带

`02-Agent中台技术架构方案.md` 第 5 节定义了 `agent-domain` 包含领域对象、值对象、枚举、状态机；`agent-api` 包含 HTTP DTO、SSE Envelope、错误码。

但在 `04-API-SSE-运行时契约-全局基线.md` 中，`RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent` 等 Java 类型的归属未明确声明——它们是 `agent-api` 的 DTO 还是 `runtime-spi` 的接口契约？

- `04` 第 1 节"契约归属"只列了 `agent-api`、`runtime-spi`、`repo-spi` 三个归属方向。
- `RuntimeRequest` 在第 8 节以 JSON 形式给出，未声明它属于 `runtime-spi` 还是 `agent-api`。
- `RuntimeEvent` 在第 9 节同样如此。

- **影响**：当 `domain-agent` 和 `runtime-agent` 并行开发时，对 `RuntimeRequest`/`RuntimeEvent` 的 Java 类放在哪个模块会产生争议。AI agent 的 owned modules/owned files 边界模糊。
- **建议**：在 `04` 或 `02` 中明确声明 `RuntimeRequest`、`RuntimeEvent`、`RuntimeCapabilityPlan`、`RuntimeResult` 的 Java 类归属模块（推断应为 `runtime-spi`），并在 `02` 第 5 节模块职责中同步补充。

### B-03 Session 并发状态缺少数据层约束

`04` 的 SSE 约束要求 `sequence` 单调递增；`05` 定义了 `SESSION_BUSY` 错误码和 `agent_session` 表。但所有文档中均未定义：

1. 同一 `sessionId` 是否允许多个 `run` 并发？如果不允许，数据库层如何保证（行锁？乐观锁？状态检查？）
2. `SESSION_BUSY` 的判定时机——是在 `POST /api/chat/send` 入口判定，还是在 runtime 提交时判定？
3. `agent_session` 表是否需要 `current_run_id` 或 `status` 字段来承载并发锁语义？

- **影响**：这是 Chat 最核心的并发安全点。如果两个请求同时发送到同一 session，当前契约没有足够的约束保证只有一个 run 进入执行。AI agent 实现时可能选择不同策略，导致行为不一致。
- **建议**：在 `04` 的 Chat Send 或 `05` 的 Session 数据约束中补充并发语义，至少声明：
  - 同一 session 是否只允许一个 active run。
  - 并发判定的入口位置。
  - 数据层保证手段（建议至少声明 `agent_session` 需要支持原子性状态检查或行级锁）。

---

## 二、重要问题

### I-01 `agent_message_checkpoint` 表归属和用途不清

`05` 第 3 节建议表清单中有 `agent_message_checkpoint`，但全文未解释该表与 `agent_run`、`agent_span`、`agent_message` 的关系。R2DBC 负责"checkpoint / final flush"（第 8 节），但 checkpoint 表的具体字段、与 `agent_run.sequence` 的关系、是否与 SSE `eventId` 一一对应，均未定义。

- **建议**：在 `05` 或 `04` 中补充 `agent_message_checkpoint` 的最低字段和用途说明，或在 `02` 的 Stream 持久化链路中补充 checkpoint 表结构。

### I-02 前端技术栈选择未定，影响 POC-2 启动

`08` 提到"Vue 3 + TypeScript + Pinia"；`RTK` OQ-V3-005 记录了"V3 UI 技术栈和组件库是否先沿用旧实现经验或重新选型"为 Open 状态。但 `09` 的小工程拆分中 `chat-minimal` POC-2 已包含 `TASK-V3-024 Chat 前端最小闭环`。

- **影响**：前端技术栈（特别是组件库选择）直接影响 `TASK-V3-024` 的实现边界和验收标准。在 POC-2 创建任务包前，此项必须关闭。
- **建议**：在 POC-2 启动前关闭 OQ-V3-005。如果沿用旧实现经验，应在 `08` 中声明旧前端项目路径和迁移策略。

### I-03 `agent_definition.visibility_scope` 枚举值未定义

`05` 第 4 节列出了 `visibility_scope` 字段，但未定义其可选值（如 `public`、`internal`、`restricted` 或其他）。`01` 和 `02` 多次提到"可见范围以 `visibility_scope`、`agent_user_grant` 和角色权限为准"，但可见范围的具体语义和取值始终缺位。

- **建议**：在 `05` 中补充 `visibility_scope` 的枚举值和语义，至少包含 POC 阶段需要的最小集合。

### I-04 `agent_definition.runtime_type` 与 `RuntimeCapabilityPlan.runtimeType` 语义重叠且不一致

`05` 中 `agent_definition` 有 `runtime_type` 字段；`04` 中 `RuntimeCapabilityPlan` 有 `runtimeType` 字段。但文档多次强调"是否进入 sandbox 由本次执行动作风险决定，不由 Agent 类型决定"。

这意味着 `agent_definition.runtime_type` 不应直接决定 `RuntimeCapabilityPlan.runtimeType`。但当前文档没有明确说明二者关系——是 `agent_definition.runtime_type` 作为默认值，然后由 Capability gate 覆盖？还是 `agent_definition.runtime_type` 只用于展示，完全由 Capability gate 决定？

- **建议**：在 `02` 或 `04` 中明确二者关系，建议：`agent_definition.runtime_type` 作为管理端配置的默认偏好，`RuntimeCapabilityPlan.runtimeType` 由 `platform-capability` 根据实际执行动作风险最终决定，允许覆盖。

### I-05 错误码与 HTTP 状态码映射缺失

`04` 第 12 节定义了业务错误码，第 3 节定义了通用响应格式，但未定义每个业务错误码对应的 HTTP 状态码。例如：

- `AGENT_NOT_FOUND` → 404？
- `AGENT_NOT_AUTHORIZED` → 403？
- `SESSION_BUSY` → 409？
- `RATE_LIMITED` → 429？
- `RUNTIME_UNAVAILABLE` → 503？

- **影响**：前后端并行开发时，HTTP 状态码是接口契约的一部分。AI agent 实现时可能选择不一致的状态码，导致前端无法正确处理。
- **建议**：在 `04` 中补充至少 `chat-minimal` 涉及的错误码与 HTTP 状态码的映射表。

### I-06 `biz-execution` 在 chat-minimal 契约切片中缺位

`02` 第 5 节定义了 `biz-execution` 负责"Run / Span 查询、执行详情"。`01` 第 5 节 Chat 工作台要求"执行详情面板"。`02-工程契约切片.md` API 切片中包含 `GET /api/executions/{runId}`。但 `03-工程技术设计.md` 的激活模块列表中未包含 `biz-execution`。

- **影响**：`GET /api/executions/{runId}` 的实现模块不明确，`biz-chat` 是否兼管执行查询也未说明。
- **建议**：在 `03-工程技术设计.md` 中明确执行详情查询的归属模块。如果在 `chat-minimal` 中由 `biz-chat` 兼管，应显式声明；如果启用 `biz-execution`，应加入激活模块列表。

### I-07 `agent-provider` 和 `agent_model` 表在 chat-minimal 数据切片中出现但未被讨论

`02-工程契约切片.md` 第 5 节数据切片包含 `agent_provider` 和 `agent_model` 表。但 `03-工程技术设计.md` 中 `platform-provider` 的职责只写了"解析模型供应商、模型、密钥引用和 endpoint 引用"。

`chat-minimal` 的 Provider/Model 数据从哪里初始化？是 Flyway 种子数据还是 Admin 手工配置？`chat-minimal` 明确不做 Admin 完整后台，但 Provider/Model 是 Chat 运行的前置依赖。

- **建议**：在 `03-工程技术设计.md` 或 `04-工程测试策略与真实E2E.md` 中声明 Provider/Model 数据的初始化方式（建议 Flyway 种子数据 + `.env` 密钥引用）。

### I-08 `agent_version` 表在 chat-minimal 数据切片中出现但用途未说明

`02-工程契约切片.md` 包含 `agent_version` 表。但 `chat-minimal` 不做 Agent 版本管理（属于 `admin-config` 范围）。该表在 `chat-minimal` 中的用途——是只存一条初始版本记录以满足外键约束，还是有实际业务含义？

- **建议**：在 `02-工程契约切片.md` 中声明 `agent_version` 在 `chat-minimal` 中的用途（建议：每个 `agent_definition` 必须有至少一条 `agent_version` 以满足外键约束，初始化时由种子数据创建）。

---

## 三、建议改进

### S-01 建议在 `04` 中补充 SSE 断线恢复的完整交互协议

当前 `04` 第 6 节提到 `Last-Event-ID` 恢复和 `sequence` 单调递增，但未定义：

1. 客户端请求恢复时，是重新 GET `/api/chat/{runId}/stream` 并带上 `Last-Event-ID` header？
2. 服务端返回的范围：是从 `Last-Event-ID` 之后的所有已持久化事件？还是只返回 checkpoint 间隔内的事件？
3. 如果 run 已 completed，恢复请求是返回完整事件回放还是直接返回最终状态？

`06` 和 `chat-minimal/04` 中均将"SSE 断线恢复"列为验收项，但交互协议不够。

### S-02 建议在 `04` 中明确 `POST /api/chat/{messageId}/regenerate` 与 `POST /api/chat/{messageId}/edit-resend` 的语义差异

两个接口的路径参数都是 `messageId`，但行为差异未在契约中明确定义：

- `regenerate`：是否复用原 message 的 `agentId` 和 `sessionId`？是否生成新 `runId`？原 message 状态如何变化？
- `edit-resend`：请求体是否包含编辑后的 `message` 文本？原 message 是标记为 `cancelled` 还是保留不变？

AI agent 实现时对这些语义可能有不同理解。

### S-03 建议补充 `agent_run` 和 `agent_span` 的最低字段定义

`05` 定义了 Run 和 Span 的状态枚举，但未给出与 `04` 契约对应的表字段清单。`04` 中的 `RuntimeEvent` 有 `spanId`，但 `agent_span` 表需要哪些字段来支持 Run/Span 查询、traceId 追踪和执行详情展示？

当前只有 `agent_audit_log` 有明确的字段定义，`agent_run` 和 `agent_span` 缺失。

### S-04 建议在 `07` 中补充"子 Agent 发现任务需要修改未授权文件"的处理流程

`07` 第 4 节要求"如果必须修改未授权文件，先停下来说明原因"，但未定义说明后的处理流程：

- 是暂停任务等待主 Agent 授权？
- 是在 `contract-notes.md` 中记录并继续其他工作？
- 主 Agent 如何决定是否授权修改？

对于 AI agent 自动化执行，这个流程需要更明确的指导。

### S-05 建议统一文档中的"runtime type"术语

文档中存在多种表述：

- `runtimeType`：`04` 中的 `RuntimeCapabilityPlan` 字段和 JSON 枚举
- `runtime_type`：`05` 中 `agent_definition` 的字段名
- `runtime-spi` / `runtime-agentscope` / `runtime-sandbox` / `runtime-hermes`：模块名
- `runtime`：上下文中泛指执行运行时

建议在 `02` 或 `08` 中增加术语表，区分模块名、字段枚举值和上下文泛称。

### S-06 建议在 `06` 或 `08` 中补充前端 E2E 测试的 Playwright 配置基线

`06` 和 `chat-minimal/04` 都要求 Playwright UI E2E，但未定义：

- 基础配置：baseURL、超时、重试策略。
- 认证方式：测试用户如何登录？种子数据？token 注入？
- `testRunId` 在 Playwright 测试中的传递方式（URL 参数？header？cookie？）

---

## 四、亮点确认

### H-01 小工程切片 + 契约冻结的设计

将全局候选基线与小工程 contract slice 分离，是本体系最核心的设计决策。它解决了"完整产品文档太长无法一次评审"和"全局基线阻塞当前实现"两个经典问题。`09` 和 `04` 中的"未进入当前小工程的接口/事件/错误码保留为后续候选契约"这一规则，清晰地划定了评审边界。

### H-02 AI 任务单的 owned modules / owned files 机制

`07` 中的 owned files 机制是 AI Native 并行开发的关键约束。它比"代码审查"更前置，直接在任务定义阶段就划定了修改边界，避免了 AI agent 顺手修改公共契约的问题。

### H-03 参考项目采纳/不采纳对照表

`04` 第 2 节的参考项目对照表和第 2.2 节的代码级接口引用要求，是防止 AI agent"参考了某开源产品但实际没有看过源码"的有效手段。这比通常的"参考了 XX 产品"这类模糊描述强很多。

### H-04 L0-L5 验收等级

将验收等级从 L0（文档检查）到 L5（真实链路）分层，使不同阶段的任务有不同验收标准，避免了"所有任务都必须达到 L5"的不切实际要求，也防止了"所有任务都用 L1 冒充完成"的问题。

### H-05 "禁止平台侧通过关键词绕过 AgentScope 触发工具"

这是一个反复出现、多处强调的架构不变量。在 `01`、`02`、`04`、`05`、`07` 中均有对应表述。这种"关键约束多处声明"的做法，对于 AI agent 理解系统边界非常有价值。

---

## 五、各文档评审详情

### README.md

- **状态**：结构清晰，阅读路径和按任务类型阅读设计合理。
- **问题**：文档清单中缺少 03 编号说明（见 B-01）。
- **建议**：在第 1 节文档清单下方增加注释说明编号跳跃原因。

### RTK.md

- **状态**：追踪表设计完整，需求→任务→契约→测试→知识→决策的链路清晰。
- **问题**：OQ-V3-005（前端技术栈）应提升为 POC-2 前置条件（见 I-02）。
- **建议**：核心能力追踪表中 `CAP-V3-002` 的 Chat SSE 关联了 `repo-r2dbc-stream`，但 `CAP-V3-005` SSE 持久化也关联同一模块。建议在 CAP 追踪中标注 SSE 持久化与 Chat 工作台的依赖关系。

### 01-产品蓝图与体验目标.md

- **状态**：产品定位清晰，目标用户分层合理，"暂不追求"列表划定了合理边界。
- **问题**：`visibility_scope` 枚举值缺失（见 I-03）。
- **建议**：第 9 节产品验收口径中"管理员能登记外部复杂 Agent"和"管理员能把多个已有 Agent 编排成顺序工作流"不属于 `chat-minimal` 范围，建议标注"后续小工程"。

### 02-Agent中台技术架构方案.md

- **状态**：架构不变量、模块地图、依赖方向、核心链路设计完整且自洽。小工程架构使用方式（第 3 节）设计合理。
- **问题**：
  - `agent-domain` 与 `agent-api` 边界在 Runtime 层面的归属模糊（见 B-02）。
  - `agent_definition.runtime_type` 与 `RuntimeCapabilityPlan.runtimeType` 关系不清（见 I-04）。
  - 缺少 `biz-execution` 在 `chat-minimal` 中的激活声明（见 I-06）。
- **建议**：第 5 节 `agent-api` 职责中补充"HTTP DTO、SSE DTO、错误结构、分页结构，不含 Runtime SPI 契约类"的声明；或在 `runtime-spi` 职责中补充"RuntimeRequest、RuntimeEvent、RuntimeCapabilityPlan、RuntimeResult"的归属声明。

### 04-API-SSE-运行时契约-全局基线.md

- **状态**：契约设计详尽，参考项目对照表是亮点。SSE Envelope 设计和错误码体系完整。
- **问题**：
  - `RuntimeRequest`/`RuntimeEvent` 归属模块未声明（见 B-02）。
  - Session 并发约束缺失（见 B-03）。
  - 错误码与 HTTP 状态码映射缺失（见 I-05）。
  - SSE 断线恢复协议不完整（见 S-01）。
  - `regenerate`/`edit-resend` 语义差异未定义（见 S-02）。
- **建议**：
  - 在第 1 节契约归属中明确声明 `RuntimeRequest`/`RuntimeEvent`/`RuntimeCapabilityPlan`/`RuntimeResult` 的 Java 类归属为 `runtime-spi`。
  - 在第 5 节 Chat Send Request 中补充并发语义说明。
  - 在第 12 节错误码表中补充 HTTP 状态码映射列。

### 05-数据模型与安全审计-全局基线.md

- **状态**：数据域、安全原则、Capability Gate、审计模型设计完整。MyBatis/R2DBC 双持久化边界清晰。Flyway 迁移策略合理。
- **问题**：
  - `agent_message_checkpoint` 表用途不清（见 I-01）。
  - `visibility_scope` 枚举值缺失（见 I-03）。
  - `agent_run`/`agent_span` 最低字段缺失（见 S-03）。
  - `agent_provider`/`agent_model` 初始化方式未声明（见 I-07）。
  - `agent_version` 在 `chat-minimal` 中的用途未说明（见 I-08）。
- **建议**：
  - 补充 `agent_message_checkpoint` 的最低字段定义。
  - 补充 `agent_run` 和 `agent_span` 的最低字段定义。
  - 补充 `visibility_scope` 的枚举值。

### 06-测试策略与验收等级-全局基线.md

- **状态**：L0-L5 分层设计清晰，测试数据隔离（testRunId）要求合理，CI 质量门禁定义完整。
- **问题**：Playwright 配置基线缺失（见 S-06）。
- **建议**：补充 Playwright 基础配置建议和 `testRunId` 传递方式。

### 07-AI-Native交付规约.md

- **状态**：AI 任务单模板设计完整，owned files 机制是亮点。契约合并规则和 Review Agent 检查清单实用。
- **问题**：子 Agent 发现需修改未授权文件的处理流程缺失（见 S-04）。
- **建议**：在第 4 节补充处理流程。

### 08-开发规范与本地环境.md

- **状态**：基础规范清晰，本地工具链配置完整。
- **问题**：前端技术栈"Vue 3"与 OQ-V3-005 的 Open 状态矛盾（见 I-02）。
- **建议**：在 OQ-V3-005 关闭前，将"Vue 3"标注为"当前候选"，或直接关闭 OQ-V3-005。

### 09-小工程切片与验收门禁.md

- **状态**：小工程拆分原则设计合理，并行开发门禁清晰。Epic 拆分覆盖了 V3 核心业务闭环。
- **问题**：无重大问题。
- **建议**：在已启动小工程索引中，考虑增加"启动日期"和"当前阶段"列，便于跨团队了解进度。

### 工程迭代/01-chat-minimal/ 目录

- `01-工程范围与验收目标.md`：范围清晰，验收等级明确。无重大问题。
- `02-工程契约切片.md`：API/SSE/数据/错误码切片与全局基线对齐良好。问题见 I-07、I-08。
- `03-工程技术设计.md`：主链路设计完整。问题见 I-06（`biz-execution` 缺位）。
- `04-工程测试策略与真实E2E.md`：L1-L5 验收场景覆盖合理。无重大问题。
- `05-工程路线图与任务.md`：阶段划分和任务包设计合理，并行分工表实用。

### templates/ 目录

- **ADR 模板**：结构完整，编号规则清晰。无问题。
- **AI 任务单模板**：结构完整，并行开发计划表格实用。无问题。
- **测试报告模板**：结构合理但偏简略。建议增加"环境信息"和"测试数据 testRunId"字段。
- **交接记录模板**：结构合理但偏简略。建议增加"契约合并说明"字段（与 `07` 第 7 节的 handoff 要求对齐）。

---

## 六、阻断性问题修复优先级

| 编号 | 问题 | 建议修复位置 | 建议修复时机 |
| --- | --- | --- | --- |
| B-01 | 编号跳跃缺 03 | `README.md` | POC-0 Review 前 |
| B-02 | Runtime 契约类归属模糊 | `04` 第 1 节 + `02` 第 5 节 | POC-1 创建任务包前 |
| B-03 | Session 并发状态约束缺失 | `04` 第 5 节 + `05` Session 数据约束 | POC-2 创建任务包前 |

---

## 七、重要问题修复优先级

| 编号 | 问题 | 建议修复位置 | 建议修复时机 |
| --- | --- | --- | --- |
| I-01 | checkpoint 表结构缺失 | `05` 第 3 节 | POC-1 repo-spi 任务前 |
| I-02 | 前端技术栈未定 | `08` + OQ-V3-005 | POC-2 启动前 |
| I-03 | visibility_scope 枚举缺失 | `05` 第 4 节 | POC-1 agent-domain 任务前 |
| I-04 | runtime_type 语义重叠 | `02` 或 `04` | POC-1 runtime-spi 任务前 |
| I-05 | 错误码与 HTTP 状态码映射缺失 | `04` 第 12 节 | POC-2 创建任务包前 |
| I-06 | biz-execution 归属不明确 | `chat-minimal/03-工程技术设计.md` | POC-2 创建任务包前 |
| I-07 | Provider/Model 初始化方式未声明 | `chat-minimal/03-工程技术设计.md` | POC-1 Maven 骨架任务前 |
| I-08 | agent_version 在 chat-minimal 用途未说明 | `chat-minimal/02-工程契约切片.md` | POC-1 agent-domain 任务前 |

---

## 八、评审结论

文档体系作为 AI Native 编程基础**总体可用**，但存在 3 个阻断性问题和 8 个重要问题需要在对应阶段门禁前修复。建议修复顺序：

1. POC-0 Review 前：修复 B-01（编号跳跃）。
2. POC-1 启动前：修复 B-02（Runtime 契约类归属）、I-03（visibility_scope）、I-04（runtime_type 语义）、I-07（Provider 初始化）、I-08（agent_version 用途）。
3. POC-2 启动前：修复 B-03（Session 并发）、I-01（checkpoint 表）、I-02（前端技术栈）、I-05（错误码 HTTP 映射）、I-06（biz-execution 归属）。

修复以上问题后，文档体系可以支撑 AI agent 按任务单、owned files 和验收等级执行真实开发任务。
