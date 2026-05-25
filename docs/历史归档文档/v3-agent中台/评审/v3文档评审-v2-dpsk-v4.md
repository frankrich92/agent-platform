# V3 Agent 中台文档评审报告 v2

> 评审模型：dpsk-v4
> 评审日期：2026-05-19
> 评审范围：`docs/v3-agent中台/` 全局基线文档（01-08）、RTK、templates/、工程迭代/01-chat-minimal/（含 ai-tasks/）
> 评审基线：基于 v1 四方评审（Claude、DeepSeek-V4、GLM-5.1、Kimi2.6）及 v2 已有评审（GLM-5.1、Kimi2.6）后的文档迭代版本
> 评审目的：评估文档体系作为 AI Native 编程基础的完备性、一致性、可执行性和风险

---

## 1. 评审总结

### 1.1 总体结论

**总体评分：A-（优秀，距离"可直接用于 AI 多 Agent 并行开发"仅剩 3-5 个可修项）**

文档体系在 v1 评审后进行了**系统性改进**，大部分 v1 遗留问题已解决。当前版本的核心优势：
- **三层架构成熟**：全局基线 → Epic → 小工程 → 任务包的四级结构清晰、权威归属明确
- **契约冻结机制有效**：chat-minimal 的 `02-工程契约切片.md` 现在已包含 SSE 语义、Session 并发、thinking 处理、错误码-HTTP 映射
- **AI Native 规则闭环**：owned files、契约冻结、并行开发门禁、验证命令报告形成完整链路
- **防御性设计到位**：禁止事项、阶段门禁、并行就绪条件清晰具体

当前版本的主要风险点集中在一个领域：**技术选型的版本号矛盾未解决**（Spring Boot 4 风险、Maven 版本不一致、Node 版本不一致），以及部分 AI Native 可执行性细节尚未完善（OpenAPI 生成、TypeScript 类型生成、data-testid 命名规范）。

### 1.2 作为 AI Native 编程基础的适用性

| 评估维度 | 状态 | 说明 |
|---------|------|------|
| 人类开发者可独立理解并开发 | ✅ 是 | 文档完整度足以支撑人工开发 |
| AI agent 可独立理解并开发（POC-1） | ✅ 是 | 契约切片、模块边界、验证命令清晰 |
| AI agent 可独立理解并开发（POC-2） | ⚠️ 有条件 | 需补 Spring Boot 版本确认 + 前端技术栈细节 |
| 多 AI agent 并行开发无冲突 | ✅ 是 | owned files 互斥规则、共享文件仲裁机制已就绪 |

---

## 2. v1 遗留问题闭环追踪

本表追踪 v1 四方评审中所有 P0/P1 级问题的修复状态。

| v1 编号 | 问题描述 | v1 等级 | v2 状态 | 验证依据 |
|---------|---------|---------|---------|---------|
| README 截断 | README.md 第 128 行截断不完整 | P0 | ✅ 已修复 | 文件完整结束于第 137 行，`维护规则` 小节完整 |
| RTK 截断 | RTK.md 任务追踪表末尾截断 | P1 | ✅ 已修复 | 文件完整结束于第 169 行 |
| 编号缺失 03 | 文档编号从 02 跳到 04 | P1 | ✅ 已修复 | 03 编号分配给 `03-API-SSE-运行时契约-全局基线.md` |
| visibility_scope 枚举缺失 | 04 中 visibility_scope 未定义具体枚举值 | P1 | ✅ 已修复 | 04 第 4 节已定义 private/granted_users/role_scope/public_enabled |
| runtime_type 语义重叠 | agent_definition.runtime_type 与 RuntimeCapabilityPlan.runtimeType 关系不清 | P1 | ✅ 已修复 | 04 第 4 节和 02 第 8.1 节多处声明默认偏好 vs 最终决策 |
| biz-execution 归属不明 | 02 模块地图标记 `[Core]` 但 chat-minimal 技术设计未激活 | P0 | ✅ 已修复 | chat-minimal `03-工程技术设计.md` 已激活 `biz-execution` |
| Capability Gate "最小"含义 | chat-minimal 最小 gate 行为未定义 | P1 | ✅ 已修复 | chat-minimal `02-工程契约切片.md` 第 7 节已详细定义 |
| thinking 持久化 | agent_message 是否包含 thinking 未明确 | P1 | ✅ 已修复 | contract slice 第 8 节明确：thinking 只作为 checkpoint/Run 观测数据保留 |
| SSE 语义 | Session 并发、SSE 恢复、重新生成/编辑重发语义缺失 | P1 | ✅ 已修复 | contract slice 第 8 节完整补充 |
| 错误码-HTTP 映射 | 业务错误码到 HTTP 状态码映射缺失 | P1 | ✅ 已修复 | contract slice 第 6 节定义了完整映射表 |
| 前端技术栈 | 构建工具/UI 库/SSE 客户端未在全局规范中定义 | P1 | ⚠️ 部分修复 | 07 已写 Vue 3 + TypeScript + Pinia，但构建工具、UI 组件库、SSE 客户端、路由方案仍未声明 |
| Spring Boot 4 风险 | Spring Boot 4.0.6 风险未评估 | P0 | ❌ 未修复 | 07 仍写 "Spring Boot 4.0.6"，RTK OQ-V3-007 已标注 "Done"，但无 ADR 评估兼容性风险 |
| Maven 版本不一致 | macOS: 4.0.0-rc-5；WSL2: 3.9.12 | P1 | ❌ 未修复 | 两套环境配置仍引用不同 Maven 版本 |
| Node 版本不一致 | macOS: v24.15.0；WSL2: v24.13.0 | P1 | ❌ 未修复 | 两套环境配置引用不同 Node 版本 |
| WorkflowRun 缺 waiting 状态 | Task 有 waiting 而 WorkflowRun 没有 | P0 | ❌ 未修复 | 04 第 6 节 WorkflowRun 状态仍为 created/running/completed/failed/cancelled |

### 2.1 未修复问题分析

**P0 级遗留（1 项）**：

- **Spring Boot 4 风险未评估**：这是当前最大的技术风险。07 多文档声明 Spring Boot 4.0.6，RTK OQ-V3-007 已标注 "Done"，但实际上没有 ADR 评估其兼容性风险（MyBatis-Plus 兼容性、Jakarta EE 10+ 依赖、第三方库生态）。这对 POC-1 的 Maven 骨架创建是**直接阻塞**——AI agent 创建工程时如遇到依赖冲突，将无法判断是版本问题还是配置问题。

**P1 级遗留（3 项）**：

- **Maven/Node 版本不一致**：两个环境的工具链版本不同，会在跨环境开发时产生隐式差异。
- **前端技术栈细节缺失**：07 只写了 Vue 3 + TypeScript + Pinia，但构建工具（Vite/Webpack）、UI 库、SSE 客户端（EventSource/fetch）、路由方案（Vue Router）、API 客户端均未声明。AI agent 做前端初始化时会自行选型，导致不一致。
- **WorkflowRun 缺 waiting 状态**：Task 状态有 waiting（用于等待外部条件），但 WorkflowRun 没有。虽不影响当前 chat-minimal，但后续 workflow-sequential 小工程需要此状态。

---

## 3. 新发现问题（v2 首次发现）

### 3.1 全局文档级别问题

#### D2-01 [P1] 07 开发规范缺少前端构建工具链声明

**位置**：`07-开发规范与本地环境.md` 第 15-21 行

**问题**：前端规范只声明了 Vue 3 + TypeScript + Pinia，未声明：
- 构建工具（Vite vs Webpack）
- UI 组件库（Element Plus / Ant Design Vue / Naive UI / 自研）
- SSE 客户端方案（EventSource 原生 vs 第三方库）
- 路由方案（Vue Router 版本）
- API 客户端（fetch / axios）
- CSS 方案（Tailwind / UnoCSS / SCSS / CSS Modules）
- 状态管理（Pinia 已声明，但未声明持久化方案）

**影响**：AI agent 在 POC-2 创建前端工程时无选型依据，不同 agent 可能做出不同决策。

**建议**：在 07 或 chat-minimal 技术设计中补充前端工具链声明。

#### D2-02 [P1] 缺少 data-testid 命名规范

**位置**：`05-测试策略与验收等级-全局基线.md` 第 94 行

**问题**：文档声明"页面使用稳定 `data-testid`"，但未定义命名规范。例如：
- 格式：`kebab-case` vs `camelCase` vs `snake_case`
- 层级：`chat-input` vs `chat.input` vs `chat.input.send-button`
- 动态 ID：`message-{id}` vs `message.{id}`

**影响**：POC-2 的前端 agent 和 E2E agent 如使用不同的 data-testid 约定，Playwright 测试会全部失败。

**建议**：在 05 或 chat-minimal 测试策略中补充命名示例：
```text
- 格式: kebab-case, 层级用 - 分隔
- 示例: chat-input, chat-message-{messageId}, agent-selector-dropdown
- 页面级前缀: chat-, sessions-, agents-, executions-
```

#### D2-03 [P1] 缺少 OpenAPI / TypeScript 类型生成策略

**位置**：`03-API-SSE-运行时契约-全局基线.md` 整体

**问题**：API 契约以 Markdown 描述性文字和 JSON 示例形式存在，但未声明：
- 是否生成 OpenAPI 规范
- 是否从 OpenAPI 生成 TypeScript 类型
- 前端类型与后端 DTO 的一致性保证机制

**影响**：AI agent 实现 API 时可能偏离契约，前端 agent 可能手写类型导致与后端不一致。

**建议**：在 03 或 chat-minimal 契约切片中声明类型生成策略（至少声明 POC 阶段手工对齐）。

#### D2-04 [P2] 全局文档缺少 `data-testid` / `testRunId` 的代码级约束

**位置**：跨文档

**问题**：`testRunId` 隔离规则在 05 测试策略中定义明确，但未声明：
- `testRunId` 在 Java entity 中的字段名（`testRunId` vs `test_run_id` vs `metadata.testRunId`）
- `testRunId` 在数据库列名中的格式
- `data-testid` 在 Vue 模板中的绑定方式

**建议**：在 07 或 04 中补充字段命名约定。

#### D2-05 [P2] templates 目录与 README.md 引用关系弱

**位置**：`README.md` 第 19-23 行 vs 阅读路径

**问题**：README.md 的文件清单列出了 templates/ 目录，但在 4 组阅读路径中均未引用。AI agent 可能不知道模板的存在。

**建议**：在 `AI Native 开发` 阅读路径中补充 `templates/` 引用。

### 3.2 跨文档一致性问题

#### D2-06 [P0] Spring Boot 4 版本已在多处固化但无 ADR 支撑

**位置**：
- `07-开发规范与本地环境.md` 第 8 行："Spring Boot 4.0.6"
- `02-Agent中台技术架构方案.md` 第 220 行："Spring Boot 启动入口"（隐式）
- `RTK.md` OQ-V3-007："已固定为 JDK 21、Spring Boot 4.0.6、Maven 4.x RC"
- `AGENTS.md` 开发规范区："Spring Boot 4.0.6"

**问题**：Spring Boot 4.0.6 已被多处固化为事实标准，OQ-V3-007 状态为 "Done"，但没有 ADR 评估：
1. Spring Boot 4 对 Jakarta EE 10+ 的强制依赖
2. MyBatis-Plus 对 Spring Boot 4 的兼容性（截至 2026-05，MyBatis-Plus 官方是否已适配？）
3. R2DBC 对 Spring Boot 4 的兼容性
4. Flyway 对 Spring Boot 4 的兼容性
5. 降级方案（如遇兼容性问题，退到 Spring Boot 3.x 的路径）

**影响**：POC-1 创建 Maven 骨架时，AI agent 可能遇到依赖冲突无法解决。

**建议**：
1. 创建 ADR-0003 记录 Spring Boot 4 选型理由和降级方案
2. 或在 POC-1 任务中明确"先以 Spring Boot 3.4.x 创建骨架，升级到 4.0.x 作为后续优化项"

#### D2-07 [P1] Maven 版本在双环境间不一致

**位置**：
- `07-开发规范与本地环境.md` macOS 参考：`maven/4.0.0-rc-5`
- `07-开发规范与本地环境.md` WSL2 参考：`apache-maven-3.9.12`
- `AGENTS.md`：`maven/4.0.0-rc-5`

**问题**：Maven 4 RC 和 Maven 3.9.x 在 POM 格式、插件兼容性上有差异。两个环境使用不同版本意味着代码在两个环境下可能行为不同。

**建议**：统一为 Maven 4.0.0-rc-5，或明确声明"WSL2 环境待升级到 4.0.0-rc-5，临时使用 3.9.12"。

#### D2-08 [P1] Node.js 版本在双环境间不一致

**位置**：
- `07-开发规范与本地环境.md` macOS 参考：`node/v24.15.0`
- `07-开发规范与本地环境.md` WSL2 参考：`node/v24.13.0`

**问题**：0.2 的版本差异不大，但严格一致性要求下仍应统一。

**建议**：统一为 v24.15.0，或声明 `>= 24.13.0` 的最低版本要求。

#### D2-09 [P2] RTK 中 EPIC 状态与阶段任务状态不完全对应

**位置**：
- `RTK.md` 第 62 行：chat-minimal 小工程状态 "In Progress"
- `RTK.md` 第 99 行：POC-0 契约冻结状态 "Review"
- `08-小工程切片与验收门禁.md` 第 88 行：chat-minimal 状态 "已启动"

**问题**：三个文档对 chat-minimal 的状态描述不同。"In Progress"、"Review"、"已启动" 语义不等价。AI agent 可能据此做出不同判断。

**建议**：统一小工程状态描述或在 RTK 中增加状态说明。

### 3.3 单个文档内部问题

#### D2-10 [P2] 02 模块地图中 `platform-input` 标记为 `[Ext]`，但 8.5 节链路描述详尽

**位置**：`02-Agent中台技术架构方案.md` 第 99 行 vs 第 377-398 行

**问题**：`platform-input` 在模块地图中标记为 `[Ext]`（后续能力候选），这是正确的。但 8.5 节"图片型输入 OCR 预处理链路"花了 22 行详细描述完整链路（包括字段、约束、错误处理），阅读时容易产生"该模块已就绪"的错觉。

**说明**：这不是 v2-GLM-5.1 评审中声称的"完全未列出"问题——`platform-input` 确实在模块地图第 99 行列出了。但详细链路的篇幅远超过一个 `[Ext]` 模块应有的体量。

**建议**：在 8.5 节开头增加一句："以下为后续 `input-ocr` 小工程的候选架构，当前所有小工程不实现该链路。"

#### D2-11 [P2] 05 测试策略中的推荐命令无法在当前阶段执行

**位置**：`05-测试策略与验收等级-全局基线.md` 第 140-163 行

**问题**：文档推荐了 `mvn test`、`npm run build` 等命令，但 `source/v3-agent-platform` 和 `source/v3-agent-platform-ui` 目录尚未创建。AI agent 按文档执行时可能误认为需要这些目录已存在。

**建议**：在 POC-1 前端章节增加注释："本命令在对应源码目录创建后方可执行"。

#### D2-12 [P3] 06 中 `contract-notes.md` 合并主契约的时机不明确

**位置**：`06-AI-Native交付规约.md` 第 113-114 行

**问题**：规则说"公共契约只能由 contract owner、主 Agent 或 Review Agent 汇总回写"，但未指定：
- 合并触发时机（POC 阶段结束后？所有子 Agent 完成后？）
- 合并前是否需要 Review Agent 确认
- 合并冲突的处理方式

**建议**：补充合并流程："子 Agent 完成后 → Review Agent 确认 → 主 Agent 合并 → 更新 RTK 契约状态"。

### 3.4 小工程文档内部问题

#### D2-13 [P2] chat-minimal contract slice 中 `agent_message_checkpoint` 字段仅定义最低字段

**位置**：`工程迭代/01-chat-minimal/02-工程契约切片.md` 第 151 行

**问题**：contract slice 声明了 `agent_message_checkpoint` 的最低字段（checkpoint_id, run_id, message_id, sequence, event_type, payload_json, accumulated_content, created_at），但没有说明：
- `payload_json` 的结构约束（是否与 SSE envelope payload 同构？）
- `accumulated_content` 的累积策略（全量 vs 增量？）
- checkpoint 的保留策略（全保留 vs 按 run 清理？）

**影响**：POC-2 实现 checkpoint 时，AI agent 可能对 payload 结构做出不同假设。

**建议**：补充 payload_json 的结构说明，至少声明"与 SSE envelope payload 结构一致"。

#### D2-14 [P3] chat-minimal 技术设计中缺少具体的 Java 包路径

**位置**：`工程迭代/01-chat-minimal/03-工程技术设计.md`

**问题**：后模块列表只有模块名（如 `agent-domain`），未给出完整 Java 包路径（如 `com.htam.agent.domain`）。虽 07 声明包名使用 `com.htam...`，但 Maven artifactId 和 package 的映射关系未定义。

**建议**：在 POC-1 的 Maven 骨架任务中声明 artifactId 与 package 的映射约定。

---

## 4. 逐文档详细评审

### 4.1 `README.md` — 文档索引

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 结构清晰度 | ★★★★ | ★★★★★ | 修复了截断问题，维护规则完整 |
| 阅读路径 | ★★★★ | ★★★★ | 四组阅读路径清晰，但 templates/ 未出现在任何阅读路径中 |
| 维护规则 | ★★★ | ★★★★★ | 截断已修复，约束与文档一一对应 |

**v2 新增问题**：无阻断性问题。模板引用缺失为 P2 改善项。

### 4.2 `RTK.md` — 需求/任务/知识追踪表

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 追踪完整性 | ★★★★ | ★★★★★ | 截断已修复，REQ → CAP → TASK 三层联动完整 |
| 状态一致性 | ★★★ | ★★★ | EPIC 状态与阶段任务状态仍不完全对应 |
| AI 可读性 | ★★★★ | ★★★★ | 表格结构清晰，但 OQ 项的 "Review" 状态含义模糊 |

**v2 新增问题**：
- D2-09：EPIC/阶段/文档三者状态不完全对应
- OQ-V3-007 标注 "Done" 但 Spring Boot 4 风险无 ADR 支撑

### 4.3 `01-产品蓝图与体验目标.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 用户画像 | ★★★★★ | ★★★★★ | 五类用户清晰 |
| Agent 标签体系 | ★★★★★ | ★★★★★ | 七维度标签 + 治理边界 |
| 产品边界 | ★★★★ | ★★★★ | "不做" 声明到位 |

**v2 新增问题**：无。本文在 v1→v2 期间基本稳定。

### 4.4 `02-Agent中台技术架构方案.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 架构不变量 | ★★★★★ | ★★★★★ | 17 条不变量覆盖核心约束 |
| 模块地图 | ★★★★ | ★★★★ | platform-input 已列出，完整 |
| 核心链路 | ★★★★ | ★★★★ | 5 条核心链路，8.5 节篇幅偏大 |
| 依赖方向 | ★★★★★ | ★★★★★ | 依赖图和约束清晰 |

**v2 新增问题**：
- D2-06：Spring Boot 4 无 ADR 支撑
- D2-10：8.5 节 OCR 链路描述过于详细，与 `[Ext]` 标记不匹配

### 4.5 `03-API-SSE-运行时契约-全局基线.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 参考来源 | ★★★★★ | ★★★★★ | 10 个参考项目 + 代码级引用完整 |
| API 覆盖度 | ★★★★ | ★★★★ | 用户侧、管理侧、Task、Workflow API 分组完整 |
| SSE Envelope | ★★★★★ | ★★★★★ | 统一 envelope 结构 + 线协议示例 |
| 形式化程度 | ★★★ | ★★★ | 仍为描述性文字，无 OpenAPI Schema |

**v2 新增问题**：
- D2-03：缺少 OpenAPI / TypeScript 类型生成策略

### 4.6 `04-数据模型与安全审计-全局基线.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 数据域覆盖 | ★★★★★ | ★★★★★ | 11 个数据域 + 建议表清单 |
| 字段约束 | ★★★★ | ★★★★★ | 新增 visibility_scope 枚举、runtime_type 语义 |
| 安全模型 | ★★★★★ | ★★★★★ | Capability Gate + 权限模型 + 审计模型完整 |
| WorkflowRun 状态 | ★★★ | ★★★ | 仍缺 waiting 状态 |

**v2 新增问题**：无。visibility_scope 和 runtime_type 的修复提升了本文质量。

### 4.7 `05-测试策略与验收等级-全局基线.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 验收等级 | ★★★★★ | ★★★★★ | L0-L5 定义清晰 |
| E2E 要求 | ★★★★ | ★★★★ | Playwright 和 e2e-real 要求明确 |
| data-testid | ★★★ | ★★★ | 声明了 "稳定 data-testid" 但无命名规范 |

**v2 新增问题**：
- D2-02：缺少 data-testid 命名规范
- D2-11：推荐命令在当前阶段无法执行

### 4.8 `06-AI-Native交付规约.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 任务单模板 | ★★★★★ | ★★★★★ | 模板字段完整 |
| owned files 规则 | ★★★★★ | ★★★★★ | 修改边界清晰 |
| 并行开发规则 | ★★★★ | ★★★★ | 就绪条件和共享文件仲裁明确 |
| 合并流程 | ★★★ | ★★★ | contract-notes 合并时机模糊 |

**v2 新增问题**：
- D2-12：contract-notes.md 合并主契约的时机和流程不明确

### 4.9 `07-开发规范与本地环境.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 后端规范 | ★★★★ | ★★★★ | Java、Spring Boot、持久化规范清晰 |
| 前端规范 | ★★ | ★★ | 仅声明 Vue 3 + TypeScript + Pinia，缺构建工具链 |
| 工具链 | ★★★ | ★★★ | 双环境配置存在版本不一致 |

**v2 新增问题**：
- D2-01：前端构建工具链缺失
- D2-07：Maven 版本不一致
- D2-08：Node 版本不一致

### 4.10 `08-小工程切片与验收门禁.md`

| 维度 | v1 评分 | v2 评分 | 说明 |
|------|---------|---------|------|
| 拆分原则 | ★★★★★ | ★★★★★ | 业务闭环拆分原则清晰 |
| Epic 列表 | ★★★★★ | ★★★★★ | 7 个 Epic 目标/不做/验收清晰 |
| 并行门禁 | ★★★★★ | ★★★★★ | 就绪条件完整 |
| 文档边界 | ★★★★★ | ★★★★★ | 本文只做轻量索引，详细内容归属小工程 |

**v2 新增问题**：无。本文在 v1→v2 期间基本稳定。

### 4.11 工程迭代/01-chat-minimal/ 小工程文档

| 文档 | 评分 | 说明 |
|------|------|------|
| README.md | ★★★★ | 范围和暂缓范围清晰 |
| 01-工程范围与验收目标.md | ★★★★★ | 用户目标、范围、验收等级、人工评审口径完整 |
| 02-工程契约切片.md | ★★★★★ | 在 v1 基础上大幅补全：SSE 语义、Session 并发、thinking、错误码映射 |
| 03-工程技术设计.md | ★★★★ | 模块、主链路、前后端范围、暂缓项完整 |
| 04-工程测试策略与真实E2E.md | ★★★★ | L1-L5 验收命令和覆盖范围具体 |
| 05-工程路线图与任务.md | ★★★★ | POC-0/1/2 阶段切片和任务清单清晰 |

**chat-minimal contract slice v1→v2 关键改进**：
- ✅ 第 8 节：Session 并发语义（单 session 单 active run，SESSION_BUSY 判定时机）
- ✅ 第 8 节：SSE 连接与恢复（Last-Event-ID、sequence 单调递增、STREAM_RESUME_FAILED）
- ✅ 第 8 节：重新生成与编辑重发语义（保留关联关系，走同一套 Capability Gate）
- ✅ 第 8 节：thinking 内容处理（流式展示、不混入正文、默认只做 checkpoint 保留）
- ✅ 第 6 节：错误码到 HTTP 状态码的完整映射表
- ✅ 第 7 节：最小 Capability Gate 行为定义
- ✅ 第 5 节：agent_message_checkpoint 最低字段定义

**v2 新增问题**：
- D2-13：checkpoint payload_json 结构约束未定义
- D2-14：Java 包路径映射缺失

### 4.12 templates/ 模板

| 模板 | 评分 | 说明 |
|------|------|------|
| AI任务单-模板.md | ★★★★★ | 字段完整，并行开发计划表设计合理 |
| ADR-模板.md | ★★★★ | 取舍分析维度全面，缺"降级方案"字段 |
| 测试报告-模板.md | ★★★ | 结构完整但偏薄，缺少"通过的测试用例数/失败数"统计字段 |
| 交接记录-模板.md | ★★★★ | 包含公共契约处理和未授权文件变更 |

---

## 5. AI Native 编程可执行性专项评估

### 5.1 指令清晰度

| 场景 | 评估 | 说明 |
|------|------|------|
| AI agent 创建 Maven 多模块骨架 | ⚠️ 可执行但有风险 | 模块列表完整，但 Spring Boot 4 兼容性风险未评估 |
| AI agent 实现 agent-domain DTO | ✅ 可执行 | 数据模型字段定义足够清晰 |
| AI agent 实现 agent-api Controller | ✅ 可执行 | API 列表 + 错误码映射完整 |
| AI agent 实现 SSE stream | ✅ 可执行 | SSE envelope + checkpoint 策略清晰 |
| AI agent 实现前端 Chat 页面 | ⚠️ 可执行但有风险 | 契约清晰但前端工具链未声明 |
| AI agent 编写 Playwright E2E | ⚠️ 可执行但有风险 | 测试场景明确但 data-testid 规范缺失 |
| 多 AI agent 并行开发 | ✅ 可执行 | owned files 互斥 + 共享文件仲裁机制完整 |

### 5.2 契约可验证性

| 契约类型 | 可验证性 | 说明 |
|---------|---------|------|
| API 请求/响应结构 | ⚠️ 部分可验证 | Chat Send Request 有完整 JSON Schema，其他 API 只有路径无 Schema |
| SSE Event Type | ✅ 可验证 | 15 种 Event Type 明确枚举 + envelope 结构 |
| 错误码 | ✅ 可验证 | 17 个错误码 + HTTP 状态码映射 |
| 数据表字段 | ✅ 可验证 | chat-minimal contract slice 定义了 20 张表的最低字段 |
| 状态机 | ✅ 可验证 | Run/Message/Task/WorkflowRun 状态枚举完整 |

### 5.3 构建与验证命令可执行性

| 命令类别 | 可执行性 | 说明 |
|---------|---------|------|
| mvn test | ❌ 当前不可执行 | 源码目录尚未创建 |
| npm run build | ❌ 当前不可执行 | 前端源码目录尚未创建 |
| Playwright E2E | ❌ 当前不可执行 | 需要前后端服务运行 |
| L0 文档检查 | ✅ 可执行 | 文档链接验证可在当前执行 |

---

## 6. 跨文档引用完整性检查

### 6.1 文件引用交叉验证

| 引用方 | 被引用方 | 状态 |
|--------|---------|------|
| README.md → RTK.md | RTK.md 存在 | ✅ |
| README.md → 01-08 文档 | 全部存在 | ✅ |
| README.md → templates/ | 存在但未加入阅读路径 | ⚠️ |
| README.md → 工程迭代/README.md | 存在 | ✅ |
| RTK.md → 全局基线文档 | 全部存在 | ✅ |
| RTK.md → chat-minimal ai-tasks | 部分任务目录待创建 | ⚠️ POC-2 任务目录未创建 |
| 02 → 全局基线文档 | 全部存在 | ✅ |
| 03 → 调研文档 | 全部存在（均为相对路径引用） | ✅ |
| chat-minimal 契约 → 全局基线 | 引用关系完整 | ✅ |
| AGENTS.md → docs/v3-agent中台/ | 引用一致 | ✅ |

### 6.2 编号一致性

| 编号体系 | 一致性 | 说明 |
|---------|--------|------|
| 文档编号 01-08 | ✅ 连续 | 03 已补全 |
| REQ-V3-xxx | ✅ 完整 | REQ-V3-001 到 REQ-V3-009 |
| CAP-V3-xxx | ✅ 完整 | 部分截断但主体完整 |
| TASK-V3-xxx | ✅ 完整 | POC-0/POC-1 任务已定义，POC-2 任务已规划 |
| EPIC-V3-x | ✅ 完整 | EPIC-V3-A 到 EPIC-V3-G |
| ADR-xxxx | ✅ 一致 | ADR-0001、ADR-0002 已创建 |
| OQ-V3-xxx | ✅ 完整 | OQ-V3-001 到 OQ-V3-007 |

---

## 7. 风险矩阵

| 风险编号 | 风险描述 | 等级 | 影响范围 | 阻塞阶段 |
|---------|---------|------|---------|---------|
| R2-01 | Spring Boot 4.0.6 兼容性未验证 | 🔴 高 | Maven 骨架、所有后端模块 | POC-1 |
| R2-02 | Maven 版本双环境不一致 | 🟡 中 | 跨环境开发、CI | POC-1 |
| R2-03 | 前端构建工具链未声明 | 🟡 中 | 前端初始化 | POC-2 |
| R2-04 | data-testid 命名规范缺失 | 🟡 中 | Playwright E2E | POC-2 |
| R2-05 | OpenAPI/类型生成策略缺失 | 🟢 低 | 前后端类型一致性 | POC-2 |
| R2-06 | WorkflowRun 状态缺 waiting | 🟢 低 | workflow-sequential 小工程 | EPIC-V3-D |
| R2-07 | contract-notes 合并时机模糊 | 🟢 低 | 多 Agent 并行契约合并 | POC-2 |

### 7.1 POC-1 阻塞项

在 POC-1（Maven 多模块骨架）启动前，**必须解决** R2-01：

1. **创建 ADR** 评估 Spring Boot 4.0.6 的兼容性风险，包括：
   - MyBatis-Plus 兼容性验证
   - R2DBC 兼容性验证
   - Flyway 兼容性验证
   - 降级到 Spring Boot 3.4.x 的路径
2. **或调整策略**：POC-1 先用 Spring Boot 3.4.x 创建骨架，升级到 4.0.x 作为后续任务

建议**同步解决** R2-02：统一两个环境的 Maven 版本为 4.0.0-rc-5。

### 7.2 POC-2 阻塞项

在 POC-2（Chat 主链路和前端闭环）启动前，建议解决 R2-03 和 R2-04。

---

## 8. 改进建议（按优先级排序）

### P0 — 必须在 POC-1 前解决

1. **[R2-01] Spring Boot 4 风险评估**
   - 创建 `工程迭代/01-chat-minimal/adr/ADR-0003-Spring-Boot-4选型与降级方案.md`
   - 或创建 POC-1 前置任务：先验证 Spring Boot 4 依赖兼容性
   - 更新 RTK OQ-V3-007 状态为 "Blocked" 直到 ADR 完成

### P1 — 应在 POC-1 启动前或期间解决

2. **[R2-02] 统一工具链版本**
   - 将 WSL2 环境的 Maven 版本更新为 4.0.0-rc-5（或补充说明临时差异）
   - 将两个环境的 Node.js 版本统一为 v24.15.0（或声明最低版本 `>= 24.13.0`）

3. **[D2-01] 补充前端工具链声明**
   - 在 `07-开发规范与本地环境.md` 或 chat-minimal `03-工程技术设计.md` 中声明：
     - 构建工具：Vite
     - UI 组件库：Element Plus / Ant Design Vue / Naive UI（三选一）
     - SSE 客户端：原生 EventSource + 自定义 reconnect 封装
     - 路由：Vue Router 4.x
     - API 客户端：fetch / axios（二选一）
     - CSS 方案：SCSS / Tailwind / UnoCSS（三选一）

4. **[D2-02] 定义 data-testid 命名规范**
   - 在 `05-测试策略与验收等级-全局基线.md` 中补充命名示例

5. **[D2-07] [D2-08] 统一工具链版本**
   - 见 P1-2

### P2 — 可在 POC-2 期间解决

6. **[D2-03] 声明 OpenAPI/类型生成策略**
7. **[D2-04] 补充 data-testid/testRunId 代码级约束**
8. **[D2-05] 在 README.md 阅读路径中引用 templates/**
9. **[D2-06] 创建 Spring Boot 4 ADR**
10. **[D2-09] 统一 EPIC/阶段/文档状态描述**
11. **[D2-10] 在 02 8.5 节开头增加 "后续候选" 声明**
12. **[D2-13] 补充 checkpoint payload_json 结构约束**
13. **[D2-14] 在 POC-1 任务中补充 Java 包路径约定**

### P3 — 后续优化

14. **[D2-11] 在测试策略中标注当前阶段命令不可执行**
15. **[D2-12] 明确 contract-notes 合并流程**

---

## 9. 与 v1/v2 其他评审的交叉验证

### 9.1 与 v1-dpsk-v4 对比

v1 评审的 16 个主要问题中，**12 个已修复**，**4 个遗留**（Spring Boot 4、Maven/Node 版本、WorkflowRun 状态、前端技术栈细节）。

v1 的核心发现 "文档在约束 AI agent 不越界方面表现出色，但在告诉 AI agent 精确该怎么做方面存在缺口" —— 在 v2 中已有**大幅改善**。chat-minimal 契约切片的补全（Session 并发、SSE 恢复、thinking 处理、错误码映射）使得 AI agent 现在可以有更精确的执行指导。

### 9.2 与 v2-glm5.1 对比

v2-GLM-5.1 评审中指出的 `platform-input` "完全未列出" 问题——经核实，`platform-input` 在 02 模块地图第 99 行已列出（标记为 `[Ext]`）。该评审的此项结论**不准确**。

v2-GLM-5.1 的其他发现（Session 并发语义缺失、SSE 断线恢复不完整）——经核实，chat-minimal contract slice 第 8 节已完整补充，**已修复**。

### 9.3 与 v2-kimi2.6 对比

v2-Kimi2.6 评审的核心发现 "Spring Boot / Maven 版本号矛盾是最大落地风险" —— **完全同意**。这也是本次评审的 P0 项。

v2-Kimi2.6 的 "agent_definition.runtime_type 枚举未明确" —— 经核实，04 第 4 节已明确 `agentscope`、`sandbox`、`hermes`、`disabled` 四种值，**已修复**。

---

## 10. 结论

V3 Agent 中台文档体系经过 v1→v2 迭代，已经达到**较高的 AI Native 编程基线水平**。三层架构（全局基线 → 小工程 → 任务包）+ 契约冻结 + owned files + 验收等级形成了完整的 AI 可执行闭环。

当前版本可以支撑：
- ✅ **L0 文档检查**：文档结构、链接、规则完整性可验证
- ✅ **POC-1 启动**（条件：先解决 Spring Boot 4 版本风险）
- ✅ **POC-2 启动**（条件：POC-1 完成 + 解决前端工具链和 data-testid 问题）

最大的单一风险是 **Spring Boot 4.0.6 的选型未经兼容性验证**。建议在 POC-1 的第一个任务（Maven 骨架创建）中优先验证依赖兼容性，并根据结果决定是否调整版本。

**总评：A-。距 "可直接用于 AI 多 Agent 并行开发" 仅差 Spring Boot 版本确认和前端工具链声明。**
