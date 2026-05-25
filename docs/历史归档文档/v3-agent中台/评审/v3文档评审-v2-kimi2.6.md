# V3 Agent 中台文档评审报告 v2（kimi2.6）

> 评审范围：`docs/v3-agent中台/` 下全部正式文档、模板及首个小工程（chat-minimal）交付文档。
> 评审目的：在 v1 评审基础上，验证问题修复状态、发现新偏差，确认文档是否达到 AI Native 编程基线。
> 评审时间：2026-05-19

---

## 1. 评审总体结论

| 维度 | v1 评分 | v2 评分 | 变化说明 |
|------|---------|---------|----------|
| 文档体系结构 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 无变化，三层架构依然清晰 |
| 内容完整性 | ⭐⭐⭐⭐☆ | ⭐⭐⭐⭐☆ | chat-minimal 契约切片补全了 SSE 语义和 thinking 策略 |
| 跨文档一致性 | ⭐⭐⭐⭐☆ | ⭐⭐⭐☆☆ | 发现模块地图遗漏、版本号矛盾、RTK 任务追踪滞后等新偏差 |
| 可执行性 | ⭐⭐⭐⭐☆ | ⭐⭐⭐⭐☆ | 技术选型版本号仍未确认，是最大落地风险 |
| 架构合理性 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 架构不变量和设计模式依然稳健 |

**总体判断**：文档体系在 v1 基础上**部分改进**，但**关键阻塞问题仍未解决**。`chat-minimal` 小工程文档（特别是 `02-工程契约切片.md`）在 SSE 语义、Capability Gate 最小行为、thinking 持久化等方面已有明显补全，可支撑 POC-1 启动。但**全局基线文档中的技术选型版本号矛盾、模块地图遗漏、RTK 追踪滞后**等问题，如不在 POC-1 启动前修复，将导致 AI agent 在工程骨架创建阶段产生歧义。

**建议**：优先修复本文档列出的 **P0 级问题**，P1 级问题可在 POC-1 期间并行补全。**不建议在 Spring Boot / Maven 版本号确认前创建工程骨架**。

---

## 2. v1 问题修复状态追踪

| v1 问题编号 | v1 描述 | v1 等级 | 修复状态 | 说明 |
|-------------|---------|---------|----------|------|
| 5.1 | `biz-execution` 在 02 模块地图中标记为 `[Core]`，但在 chat-minimal `03-工程技术设计.md` 中未出现 | P0 | **已修复** | chat-minimal `03-工程技术设计.md` 已补充 `agent-biz/biz-execution` |
| 5.2 | `platform-input` 在 02 中标记为 `[Ext]`，但第 8.5 节详细描述了完整链路 | P1 | **未修复，升级为 P0** | 02 模块地图中**完全未列出** `platform-input`，但 8.5 节链路中两次引用该模块，矛盾更严重 |
| 5.3 | `WorkflowRun` 状态缺少 `waiting`，但 `Task` 状态有 `waiting` | P0 | **未修复** | 04-数据模型... 第 6 节 WorkflowRun 状态仍为 created/running/completed/failed/cancelled |
| 5.4 | `agent_definition.runtime_type` 枚举未在数据模型中明确 | P1 | **未修复** | 05-数据模型... `agent_definition` 关键字段列表中只写了 `runtime_type`，未列枚举值 |
| 5.5 | `agent_message` 是否包含 `thinking_content` 未明确 | P1 | **已修复** | chat-minimal `02-工程契约切片.md` 第 8 节已明确："thinking 不混写进最终 assistant 正文；默认只作为 stream checkpoint / Run 观测数据保留；是否长期落入 `agent_message` 独立字段后续再冻结" |
| 5.6 | chat-minimal 的 Capability Gate "最小"含义未定义 | P1 | **已修复** | chat-minimal `02-工程契约切片.md` 第 7 节已定义最小 gate 行为 |
| 5.7 | 文档编号缺少 `03-xxx` | P1 | **未修复** | README.md 文档清单仍从 02 直接跳到 04，无说明 |
| 5.8 | RTK 中小工程状态与任务状态不完全对应 | P1 | **未修复** | RTK.md 中 chat-minimal 小工程状态仍为 "In Progress"，但其 POC-0 任务状态为 "Review" |
| 5.9 | 前端技术栈（构建工具/UI 库/SSE 客户端）未在全局规范中定义 | P1 | **未修复** | 07-开发规范... 前端部分仍为 Vue 3 + TypeScript + Pinia，无构建工具、UI 库、SSE 客户端、路由、API 客户端说明 |
| 5.10 | SSE endpoint 的初始握手和重连语义在 chat-minimal 契约切片中未明确 | P1 | **已修复** | chat-minimal `02-工程契约切片.md` 第 8 节已补充 Session 并发、SSE 连接与恢复、重新生成与编辑重发语义 |
| v1-7.1-1 | Spring Boot 版本确认 | P0 | **未修复** | 07-开发规范... 仍写 "Spring Boot 4.0.6"；RTK.md OQ-V3-007 标注为 "Done"，但版本号仍存疑 |
| v1-7.1-2 | `biz-execution` 模块归属 | P0 | **已修复** | 见上 5.1 |
| v1-7.1-3 | `WorkflowRun` 是否需要 `waiting` 状态 | P0 | **未修复** | 见上 5.3 |
| v1-7.2-4 | 前端技术栈补充 | P1 | **未修复** | 见上 5.9 |
| v1-7.2-5 | SSE 语义补全 | P1 | **已修复** | 见上 5.10 |
| v1-7.2-6 | Capability Gate "最小"定义 | P1 | **已修复** | 见上 5.6 |
| v1-7.2-7 | Thinking 内容持久化策略 | P1 | **已修复** | 见上 5.5 |
| v1-7.2-8 | POC-2 任务包提前设计 | P1 | **部分修复** | chat-minimal `05-工程路线图与任务.md` 已补充 POC-2 子 Agent 分工表和完成条件，但 RTK.md 中未登记这些任务 |

**修复率统计**：v1 共提出 18 项问题（3 P0 + 15 P1），**已修复 6 项，未修复 12 项**。未修复问题中，3 项 P0 仍在阻塞，9 项 P1 待补。

---

## 3. 逐文档详细评审（v2 增量视角）

### 3.1 README.md（文档索引）

| 项目 | 状态 | 说明 |
|------|------|------|
| 阅读路径 | ✅ | 无变化，依然合理 |
| 维护规则 | ✅ | 第 4 节维护规则完整，但末尾缺少"谁负责把局部契约合并回全局基线"的说明（v1 已提出） |
| 编号断层 | ⚠️ **P1** | **仍未修复**。README.md 文档清单（第 7-18 行）从 `01` 跳到 `02` 再跳到 `04`，`03` 号位置为空。根据 claude v1 评审，原 `03-后端模块与核心链路.md` 已合并入 `02`，但 README 中未加注释说明 |
| 模板索引缺失 | ⚠️ **P1** | `templates/` 目录在文件列表中出现，但阅读路径中未引导新贡献者查看模板，claude v1 已提出 |

### 3.2 RTK.md（总追踪表）

| 项目 | 状态 | 说明 |
|------|------|------|
| 产品目标追踪 | ✅ | 9 条 REQ 完整 |
| 核心能力追踪 | ✅ | 10 条 CAP 完整 |
| 小工程追踪 | ⚠️ **P1** | chat-minimal 状态为 "In Progress"，但其 POC-0 任务状态为 "Review"，不一致（v1 已提出） |
| 任务追踪 | ⚠️ **P1** | **新增问题**：任务追踪表只登记到 POC-1 的 `TASK-V3-015`，POC-2 的 `TASK-V3-020` ~ `TASK-V3-024` 和横切任务 `TASK-V3-900`/`TASK-V3-901`/`TASK-V3-902`/`TASK-V3-903` 在 RTK 中**完全未出现**。这与 RTK "解决任务是否已经拆成任务"的定位矛盾 |
| 知识依据追踪 | ✅ | K-V3-001 ~ K-V3-008 完整 |
| 决策与开放问题 | ⚠️ **P0** | OQ-V3-007 标注为 "Done"，内容为 "已固定为 JDK 21、Spring Boot 4.0.6、Maven 4.x RC"，但**Spring Boot 4.0.6 的版本号存疑**（见下文）。如果版本号确实是错误的，OQ-V3-007 不应标记为 Done |

### 3.3 01-产品蓝图与体验目标.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 产品定位 / 用户分层 / 标签体系 | ✅ | 无变化，依然清晰 |
| 暂不追求清单 | ✅ | 第 8 节 9 项清单有效锚定范围 |
| 产品验收口径 | ✅ | 第 9 节 12 条口径完整 |

**无新增问题**。v1 提出的 "thinking 内容是否落库" 已在 chat-minimal 契约切片中解决。

### 3.4 02-Agent中台技术架构方案.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 架构不变量 | ✅ | 11 条不变量依然稳健 |
| 目标模块地图 | ⚠️ **P0** | **新增严重问题**：模块地图（第 74-114 行）的 `agent-platform` 子模块中**未列出 `platform-input`**，但第 8.5 节 "图片型输入 OCR 预处理链路" 中两次引用了 `platform-input`（"`platform-input` 解析文件结构"、"`platform-input` 调用 OCR / vision preprocessor adapter"）。这是一个模块地图与核心链路之间的**结构性矛盾** |
| `biz-execution` 归属 | ✅ 已修复 | v1 提出的 `biz-execution` 在 chat-minimal 技术设计中已补充 |
| 后端模块职责 | ⚠️ **P1** | `runtime-spi` 的职责列表（第 203-209 行）中只写了 "定义 Runtime 端口"，未明确列出 `RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent`、`RuntimeResult` 等具体类型。glm5.1 v1 评审的 B-02 已指出此问题 |
| 依赖方向 | ✅ | 单向依赖图无变化 |
| 核心链路 | ⚠️ **P1** | 第 8.5 节 OCR 预处理链路详细描述了 `platform-input`，但模块地图中无此模块，容易误导开发者认为该模块已进入当前范围 |
| 设计模式 | ✅ | 7 种模式适用场景说明依然务实 |
| 前端架构基线 | ⚠️ **P1** | 第 10 节建议了目录结构，但未说明构建工具、UI 库、SSE 客户端（v1 已提出） |

### 3.5 03-API-SSE-运行时契约-全局基线.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 参考项目与采纳边界 | ✅ | 10 个参考项目表格依然是亮点 |
| 通用响应 / API 分组 / Chat Send / SSE Envelope | ✅ | 无变化 |
| SSE Event 类型 | ✅ | Chat/Task/Workflow 三类事件覆盖完整 |
| RuntimeRequest / RuntimeCapabilityPlan | ✅ | 字段完整，约束明确 |
| RuntimeEvent | ⚠️ **P1** | AgentScope Java 到 RuntimeEvent 的映射表格（第 590-613 行）和 AgentScope Runtime Java 映射（第 601-614 行）中，`Event.status=rejected` 映射为 `run.failed` 并建议错误码 `PERMISSION_DENIED`、`CAPABILITY_NOT_AUTHORIZED` 或 `VALIDATION_FAILED`。但错误码全局候选基线中**没有 `PERMISSION_DENIED`**，只有 `CAPABILITY_NOT_AUTHORIZED`。`PERMISSION_DENIED` 和 `CAPABILITY_NOT_AUTHORIZED` 的区分在 chat-minimal 契约切片中已有，但全局基线中未出现 `PERMISSION_DENIED` |
| Task / ExternalAgent Schema | ✅ | 预留契约完整 |
| 错误码 | ✅ | 27 个错误码完整，兼容规则合理 |
| 契约冻结规则 | ✅ | 13 条规则覆盖变更控制 |

### 3.6 04-数据模型与安全审计-全局基线.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 数据域 / 核心关系 / 建议表清单 | ✅ | 无变化 |
| Agent 数据约束 | ⚠️ **P1** | `agent_definition` 关键字段列表（第 154-169 行）中 `runtime_type` 只写了字段名，未列枚举值。03-API 契约中写了枚举值 `agentscope`、`sandbox`、`hermes`、`disabled`，但 05-数据模型未同步 |
| 输入附件与 OCR | ✅ | 字段设计考虑了追溯性和安全性 |
| Task / Run / Span 状态 | ⚠️ **P0** | **仍未修复**：WorkflowRun 状态（第 362-370 行）为 created/running/completed/failed/cancelled，缺少 `waiting`。Task 状态（第 351-360 行）有 `waiting`。如果后续工作流需要等待状态（例如等待人工确认或外部回调），当前枚举缺失会导致状态机设计返工 |
| MyBatis-Plus / R2DBC 边界 | ✅ | 职责分离明确 |
| 安全原则 / 权限模型 | ✅ | 无变化 |
| Capability Gate | ✅ | 输入/输出/风险等级/默认策略完整 |
| 审计模型 | ✅ | 审计对象、action 枚举、字段完整 |
| Flyway 迁移策略 | ✅ | 3 条规则清晰 |
| Session 并发数据层约束 | ⚠️ **P1** | **新增问题**：`agent_session` 表结构未说明如何防止同一 session 的并发 run。04 和 05 文档都未定义：数据库层如何保证（行锁？乐观锁？状态检查？）`SESSION_BUSY` 的判定时机是在 `POST /api/chat/send` 入口还是 runtime 提交时？ |

### 3.7 05-测试策略与验收等级-全局基线.md

| 项目 | 状态 | 说明 |
|------|------|------|
| L0-L5 定义 | ✅ | 无变化 |
| 单元测试 / 集成测试 / API E2E / Playwright / e2e-real | ✅ | 覆盖场景完整 |
| 测试数据初始化与清理 | ✅ | `testRunId` 隔离规则明确 |
| CI 质量门禁 | ✅ | quick/integration/ui-e2e/e2e-real 分层合理 |

**无新增问题**。但 claude v1 已指出：chat-minimal `04-工程测试策略与真实E2E.md` 与本文档存在大量内容重复，违反了 README.md "约束只保留在一个权威位置"的规则。

### 3.8 06-AI-Native交付规约.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 基本流程 / 任务单模板 | ✅ | 无变化，13 个字段完整 |
| owned modules / owned files | ✅ | 边界规则清晰 |
| 子 Agent 并行开发 | ✅ | 并行就绪条件、推荐分工、冲突处理完整 |
| 子 Agent 输出格式 | ✅ | 6 项输出要求完整 |
| AI 任务过程文档目录 | ✅ | 目录结构和契约合并规则清晰 |
| 验收等级 / 真实 E2E | ✅ | 无变化 |
| Review Agent 检查清单 | ✅ | 9 项检查完整 |
| 完成定义 | ✅ | 7 条完成条件完整 |

**无新增问题**。

### 3.9 07-开发规范与本地环境.md

| 项目 | 状态 | 说明 |
|------|------|------|
| Java 规范 / 前端规范参考 | ✅ | 参考来源合理 |
| 模块规范 | ✅ | 8 条模块约束正确 |
| 持久化 / 安全 / Agent 能力规范 | ✅ | 无变化 |
| 本地工具链 | ⚠️ **P0** | **仍未修复**：macOS 和 WSL2 工具链都写 "Spring Boot 4.0.6" 和 "Maven 4.0.0-rc-5"。截至 2026 年 5 月，Spring Boot 最新稳定版为 3.3.x/3.4.x 系列，不存在 4.0.6 版本；Maven 4 处于 RC 阶段，稳定性待验证。如果这是有意为之（例如内部代号或特定分支），应在文档中说明具体来源和稳定性评估；如果是笔误，应立即修正，否则 AI agent 创建工程骨架时会引用不存在的依赖版本 |
| 推荐命令 | ✅ | Maven/Node 命令清晰 |
| 提交规范 | ✅ | Conventional Commit + PR 要求完整 |

### 3.10 08-小工程切片与验收门禁.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 小工程原则 / 并行开发就绪门禁 | ✅ | 无变化 |
| 推荐并行角色 | ✅ | 8 个角色 + 职责表实用 |
| 小工程拆分 | ✅ | 7 个小工程目标/不做/验收明确 |
| 已启动索引 | ✅ | chat-minimal 已启动 |
| 文档维护边界 | ✅ | 6 条边界规则 + 冲突解决机制完整 |

**无新增问题**。v1 提出的 "冻结判定标准" 未定义的问题依然存在，但属于 P1。

---

## 4. chat-minimal 小工程专项评审（v2 增量）

### 4.1 01-工程范围与验收目标.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 用户目标 / 范围 / 验收等级 | ✅ | 无变化 |
| Capability Gate "最小"含义 | ✅ 已修复 | v1 提出的问题已在 `02-工程契约切片.md` 第 7 节解决 |

### 4.2 02-工程契约切片.md

| 项目 | 状态 | 说明 |
|------|------|------|
| API 切片 | ✅ | 14 个接口覆盖核心需求 |
| SSE Event 切片 | ✅ | 11 个事件 + 约束 |
| Runtime 切片 | ✅ | 6 个 runtime 对象，sandbox/hermes 占位明确 |
| 数据切片 | ✅ | 19 张表冻结，8 张表暂不实现 |
| 错误码切片 | ⚠️ **P1** | `STREAM_RESUME_FAILED` 映射为 400，但 03 全局基线中未对该错误码映射 HTTP 状态。更关键的是，错误码切片中**包含 `INTERNAL_ERROR`**，但 03 全局基线的错误码候选基线中也有 `INTERNAL_ERROR`，这是合理的；不过 `STREAM_RESUME_FAILED` 的 HTTP 状态映射只在 chat-minimal 切片中出现，全局基线未定义，可能导致后续小工程不一致 |
| 最小 Capability Gate | ✅ 已修复 | 第 7 节已明确定义：只校验 Agent enabled + 审核通过 + 用户授权 + 风险等级 low |
| Chat 与 SSE 行为补充 | ✅ 已修复 | 第 8 节已补充 Session 并发、SSE 连接恢复、重新生成/编辑重发、thinking 内容策略 |
| `GET /api/chat/{runId}/stream` 响应格式 | ⚠️ **P1** | v1 提出该问题。当前契约切片中已存在 SSE envelope 结构（来自全局基线），但**未明确连接建立时的初始事件序列**（例如是否立即发送 `run.started`，还是先发送最近 checkpoint 后的下一条事件）。第 8 节写了 "应发送可恢复的首个运行态事件"，但不够精确 |
| capability 数据初始化 | ⚠️ **P1** | v1 已提出。数据切片包含 `agent_capability_binding` 和 `agent_capability`，但测试策略中未说明 capability 数据的初始化方式（Flyway seed？测试 fixture？） |

### 4.3 03-工程技术设计.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 后端模块 | ✅ 已修复 | 已补充 `agent-biz/biz-execution` |
| 主链路 | ✅ | 10 步链路清晰 |
| 实现边界 | ✅ | 5 条边界约束正确 |
| 前端范围 | ✅ | 4 个页面 + 7 个最小组件 |
| 暂缓技术项 | ✅ | 5 项暂缓与范围文档一致 |

### 4.4 04-工程测试策略与真实E2E.md

| 项目 | 状态 | 说明 |
|------|------|------|
| L1-L5 验收 | ✅ | 各级验收命令和覆盖场景完整 |
| L5 LLM 最低要求 | ⚠️ **P1** | v1 已提出：未明确 LLM provider 的最低配置要求（例如是否需要支持流式输出、是否需要工具调用能力）。如果 `.env` 配置的是不具备工具调用能力的模型，tool call 相关 SSE 事件将无法验证 |
| 数据隔离 | ✅ | `testRunId` 隔离规则明确 |
| 与全局文档重复 | ⚠️ **P1** | claude v1 已指出：与 05-测试策略与验收等级-全局基线.md 存在大量内容重复（L1-L5 定义、Playwright 要求、testRunId 规则），违反了 README.md "约束只保留在一个权威位置"的规则 |

### 4.5 05-工程路线图与任务.md

| 项目 | 状态 | 说明 |
|------|------|------|
| 阶段切片 | ✅ | POC-0/POC-1/POC-2/横切 4 个阶段 |
| POC-0 / POC-1 任务 | ✅ | 任务已创建，状态清晰 |
| POC-2 任务 | ✅ 已修复 | 已补充子 Agent 分工表和完成条件，比 v1 时详细很多 |
| 横切任务 | ✅ | TASK-V3-900 ~ TASK-V3-903 已列出 |
| RTK 同步 | ⚠️ **P1** | **新增问题**：POC-2 和横切任务在本文档中已列出，但在 RTK.md 的任务追踪表中**完全未登记** |

---

## 5. 跨文档一致性新问题汇总（v2 新增）

| 序号 | 问题 | 涉及文档 | 严重等级 | 建议修复方式 |
|------|------|----------|----------|-------------|
| V2-1 | `platform-input` 模块在 02 模块地图中**完全不存在**，但 02 第 8.5 节核心链路中两次引用 | `02-...` | **P0** | 在模块地图的 `agent-platform` 下补充 `platform-input [Ext]`，或在 8.5 节首句明确 "`platform-input` 是候选模块，当前未进入模块地图" |
| V2-2 | RTK.md 任务追踪表未登记 POC-2 任务（TASK-V3-020~024）和横切任务（TASK-V3-900~903） | `RTK.md` / `chat-minimal/05-...` | **P1** | 在 RTK.md 第 5 节任务追踪表中补充这些任务，状态设为 Planned 或 Pending |
| V2-3 | `RuntimeEvent` 映射中提到 `PERMISSION_DENIED` 错误码，但全局错误码基线中**不存在**该错误码 | `03-...` | **P1** | 在 03 第 12 节错误码全局候选基线中补充 `PERMISSION_DENIED`，或在映射中将其替换为已有的 `AGENT_NOT_AUTHORIZED` 或 `CAPABILITY_NOT_AUTHORIZED` |
| V2-4 | `agent_session` 表未说明并发 run 的数据层保证机制 | `04-...` / `05-...` | **P1** | 在 04 或 chat-minimal `02-...` 中补充：同一 session 的并发 run 通过 `status` 乐观锁、唯一索引或显式行锁保证；`SESSION_BUSY` 在 `POST /api/chat/send` 入口层判定 |
| V2-5 | `agent_definition.runtime_type` 枚举值在 05 数据模型中未列出 | `05-...` / `03-...` | **P1** | 在 05 第 4 节 `agent_definition` 关键字段中补充：`runtime_type enum(agentscope, sandbox, hermes, disabled)` |
| V2-6 | chat-minimal 错误码切片中 `STREAM_RESUME_FAILED` 的 HTTP 状态映射（400）未在全局基线中定义 | `chat-minimal/02-...` / `03-...` | **P1** | 在 03 第 12 节或 12.1 节中补充常见错误码的 HTTP 状态建议，确保跨工程一致 |
| V2-7 | 05-测试策略... 与 chat-minimal/04-工程测试策略... 内容大量重复 | `05-...` / `chat-minimal/04-...` | **P1** | chat-minimal 测试策略只写差异和具体验收命令，全局规则通过引用而非复制 |

---

## 6. 关键风险与改进建议

### 6.1 P0 级问题（阻塞启动）

**1. Spring Boot / Maven 版本号确认**
- **影响**：决定 Maven 依赖、Spring 配置和工程骨架。如果 "Spring Boot 4.0.6" 是笔误，AI agent 创建 `pom.xml` 时会引用不存在的版本，导致骨架无法编译。
- **当前状态**：07-开发规范... 和 RTK.md 都写 "Spring Boot 4.0.6"，RTK OQ-V3-007 标注为 "Done"。
- **行动**：立即确认版本号。如果是 Spring Boot 3.x（例如 3.4.x），修正所有文档中的版本号，并将 OQ-V3-007 状态改回 Review。如果是确指内部版本/分支，在文档中注明来源和获取方式。

**2. `platform-input` 模块在模块地图中缺失**
- **影响**：02 第 8.5 节详细描述了 `platform-input` 的工作方式，但模块地图中完全没有这个模块。AI agent 在分配 owned modules 时会困惑——`platform-input` 是否真实存在？由谁负责？
- **行动**：在 02 第 4 节模块地图中补充 `platform-input [Ext]`，或在 8.5 节首句明确该模块是候选占位，未进入当前模块地图。

**3. `WorkflowRun` 状态缺少 `waiting`**
- **影响**：如果后续工作流需要等待状态（等待人工确认、外部回调、条件满足），当前枚举缺失会导致状态机设计返工。
- **行动**：确认 V3 工作流是否需要 `waiting` 状态。如果需要，在 04 第 6 节补充；如果不需要，在工作流设计文档中明确说明原因。

### 6.2 P1 级问题（启动后可并行修复）

**4. 前端技术栈冻结**
- **建议**：在 07-开发规范... 或 chat-minimal `03-...` 中明确：构建工具（Vite）、UI 组件库（Element Plus / Ant Design Vue / 其他）、SSE 客户端封装（EventSource / fetch + ReadableStream）、API 客户端（Axios / fetch）、路由方案（Vue Router 4 + 权限路由控制方式）。关闭 RTK.md 中 OQ-V3-005。

**5. RTK.md 任务追踪表补全**
- **建议**：补充 POC-2 任务（TASK-V3-020~024）和横切任务（TASK-V3-900~903），与 chat-minimal `05-...` 保持一致。

**6. 03 编号断层说明**
- **建议**：在 README.md 文档清单中增加一行注释："`03` 编号已合并入 `02-Agent中台技术架构方案.md` 并下线，编号保留为空以避免文件重命名冲突。

**7. `PERMISSION_DENIED` 错误码缺失**
- **建议**：在 03 第 12 节错误码全局候选基线中补充 `PERMISSION_DENIED`，或修正 RuntimeEvent 映射中的引用。

**8. Session 并发数据层约束**
- **建议**：在 04 或 chat-minimal `02-...` 中补充并发控制机制说明。

**9. 小工程测试策略去重**
- **建议**：chat-minimal `04-...` 只保留本工程特有的验收命令和差异说明，L0-L5 通用定义改为引用 05-... 全局基线。

### 6.3 架构设计亮点（应坚持）

- ✅ **架构不变量**：特别是"不自研 ReAct 循环"和"平台不绕过 AgentScope 触发工具"，是避免技术债务的关键红线。
- ✅ **Capability Gate**：将能力授权、风险分级、运行时路由统一在一个 gate 中，设计优雅且可扩展。
- ✅ **MyBatis-Plus + R2DBC 分离**：普通 CRUD 与流式高频写入的职责分离，避免了性能瓶颈。
- ✅ **小工程切片策略**：按可验收业务闭环拆分，而不是按技术模块拆分，是正确的敏捷/迭代策略。
- ✅ **AI Native 交付规约**：owned files、验证命令、L0-L5 验收、Review Agent 检查清单等机制，有效约束了 AI agent 的"随意性"。
- ✅ **chat-minimal 契约切片补全**：v1 提出的 SSE 语义、Capability Gate 最小行为、thinking 持久化策略已在 chat-minimal `02-...` 中得到有效补充，说明文档维护流程在运转。

---

## 7. 评审结论与下一步行动

### 7.1 结论

当前 V3 Agent 中台文档体系在 v1 基础上**部分改进**，`chat-minimal` 小工程文档的补全表明契约冻结和迭代机制正在有效运转。但**全局基线文档中的 3 项 P0 问题仍未解决**，尤其是 **Spring Boot / Maven 版本号** 和 **`platform-input` 模块地图遗漏**，会在 POC-1 工程骨架创建阶段产生直接阻塞。

**不建议在以下问题解决前创建 `source/v3-agent-platform/pom.xml`：**
1. Spring Boot 版本号确认并修正文档（如果是笔误）
2. `platform-input` 模块在模块地图中的位置明确

### 7.2 建议的下一步行动

| 优先级 | 行动项 | 涉及文档 | 建议截止时间 |
|--------|--------|----------|-------------|
| P0 | 确认 Spring Boot / Maven 版本号并修正文档 | `07-...`、`RTK.md` | 评审后 1 天内 |
| P0 | 在模块地图中补充 `platform-input` 或明确其候选身份 | `02-...` | 评审后 1 天内 |
| P0 | 确认 `WorkflowRun` 是否需要 `waiting` 状态 | `04-...` | 评审后 2 天内 |
| P1 | 补充前端技术栈（构建工具/UI 库/SSE 客户端/API 客户端） | `07-...` 或 chat-minimal `03-...` | POC-1 启动前 |
| P1 | 在 RTK.md 中补全 POC-2 和横切任务追踪 | `RTK.md` | POC-1 启动前 |
| P1 | 修复文档编号断层（补充 03 占位说明） | `README.md` | 任意时间 |
| P1 | 在全局错误码基线中补充 `PERMISSION_DENIED` 或修正映射 | `03-...` | POC-1 启动前 |
| P1 | 定义 Session 并发数据层约束 | `04-...` 或 chat-minimal `02-...` | POC-1 启动前 |
| P1 | 去重 chat-minimal 测试策略与全局测试策略 | `chat-minimal/04-...` | POC-1 启动前 |
| P1 | 在数据模型中补充 `runtime_type` 枚举值 | `05-...` | POC-1 启动前 |

---

> 评审人：kimi2.6
> 评审日期：2026-05-19
> 本评审意见基于 `docs/v3-agent中台/` 下所有正式文档、模板及 `工程迭代/01-chat-minimal/` 下全部交付文档的完整阅读，并与 v1 评审（kimi2.6 / claude / dpsk-v4 / glm5.1）进行了对比追踪。
