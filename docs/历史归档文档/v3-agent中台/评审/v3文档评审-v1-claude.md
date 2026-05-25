# V3 Agent 中台文档评审 v1

评审人：Claude Opus 4.7
评审日期：2026-05-19
评审范围：`docs/v3-agent中台/` 全部文档（含工程迭代、任务包、ADR、模板）

---

## 一、总体评价

文档体系整体质量较高，具备以下优势：

1. **分层清晰**：全局候选基线 → 小工程切片 → 任务包三层结构，权威归属明确
2. **AI Native 方法论成熟**：owned files、契约冻结、验收等级、testRunId 隔离等规则形成闭环
3. **可追溯性强**：RTK 追踪表将需求、任务、契约、测试、知识依据串联
4. **防御性设计到位**：明确禁止事项、阶段门禁、并行开发就绪条件
5. **小工程切片思路正确**：避免了一次性冻结完整产品的评审困境

作为 AI Native 编程的基础文档，当前版本可以支撑 POC-1 启动，但存在以下需要关注的问题。

---

## 二、结构性问题

### 2.1 文档编号缺失 03

全局文档编号从 01、02 跳到 04，缺少 `03`。根据 TASK-V3-003 的 handoff 记录，原 `03-后端模块与核心链路.md` 已合并入 `02-Agent中台技术架构方案.md` 并下线。建议：

- 在 README.md 的文件列表中加一行注释说明 03 已合并入 02，避免后续贡献者困惑
- 或者将后续文档重新编号（代价较大，不推荐）

### 2.2 全局基线与小工程文档的重复

`06-测试策略与验收等级-全局基线.md` 和 `工程迭代/01-chat-minimal/04-工程测试策略与真实E2E.md` 存在大量内容重叠（L1-L5 定义、Playwright 要求、testRunId 规则）。虽然小工程文档声明了"全局规则见 06"，但实际正文仍然重复了大部分内容。

建议：小工程测试策略只写差异和具体验收命令，全局规则通过引用而非复制。

### 2.3 templates 目录未被充分引用

`templates/` 下有 4 个模板文件，但在 README.md 的阅读路径中未出现。新贡献者可能不知道模板存在。建议在 README.md 中增加模板索引入口。

---

## 三、内容完整性问题

### 3.1 前端技术栈未冻结

`OQ-V3-005` 标注为 Open：V3 UI 技术栈和组件库是否沿用旧实现或重新选型。但 `08-开发规范与本地环境.md` 已经写了 Vue 3 + TypeScript + Pinia。两处信息不一致——如果已经决定了 Vue 3，应关闭 OQ-V3-005；如果尚未决定，08 文档不应写死。

**建议**：确认前端技术栈决策，更新 OQ-V3-005 状态或在 08 中标注为候选。

### 3.2 Spring Boot 4 风险未评估

文档多处写明 Spring Boot 4，但截至 2026 年 5 月 Spring Boot 4 仍处于早期阶段。未见对以下风险的评估：

- Spring Boot 4 对 Jakarta EE 10+ 的依赖
- MyBatis-Plus 对 Spring Boot 4 的兼容性
- 第三方库生态成熟度

**建议**：在 ADR 或技术设计中补充 Spring Boot 4 选型理由和降级方案。

### 3.3 AgentScope Java 版本未锁定

文档引用 `source/fork_source/agentscope-java` 作为参考，但未明确：

- 当前 fork 基于哪个 commit/tag
- 是否跟踪上游更新
- 接口变更时的兼容策略

**建议**：在 RTK 知识依据或 ADR 中记录 fork 版本基线。

### 3.4 认证方案空白

`chat-minimal` 范围包含"最小用户身份和 Agent 授权校验"，但未说明：

- 用户认证方式（JWT / Session / OAuth）
- Token 刷新策略
- SSE 长连接的认证保持

**建议**：POC-2 启动前补充认证方案，至少在工程技术设计中声明选型。

### 3.5 部署架构未涉及

文档聚焦开发架构，但缺少：

- 单体 vs 微服务部署形态
- 数据库选型确认（PostgreSQL 在 E2E 中出现，但未在架构文档正式声明）
- 缓存、消息队列等中间件需求

**建议**：在 `02-Agent中台技术架构方案.md` 补充部署架构候选基线，或在 chat-minimal 技术设计中声明 POC 部署形态。

---

## 四、一致性问题

### 4.1 Maven 版本不一致

- CLAUDE.md：`maven/4.0.0-rc-5`
- `08-开发规范与本地环境.md` macOS 参考：未明确写版本（引用 CLAUDE.md）
- `08-开发规范与本地环境.md` WSL2 参考：`apache-maven-3.9.12`

Maven 4 RC 和 Maven 3.9 在 POM 格式和插件兼容性上有差异。

**建议**：统一为一个版本，或明确说明两个环境的差异和兼容策略。

### 4.2 Node.js 版本不一致

- CLAUDE.md：`node/v24.15.0`
- WSL2 参考：`node/v24.13.0`

**建议**：统一版本号，或声明最低版本要求。

### 4.3 状态标注不一致

RTK 中 EPIC-V3-A 状态为 `In Progress`，但 POC-0 所有任务状态为 `Review`，POC-1 为 `Planned`。按状态规则，小工程应为 `Review` 而非 `In Progress`。

**建议**：统一状态语义——如果 POC-0 在 Review 中，EPIC 状态应为 `Review` 或保持 `In Progress` 但加注当前阶段。

---

## 五、AI Native 执行规约评审

### 5.1 优势

- owned files 机制有效防止跨模块污染
- contract-notes.md 避免多 Agent 并行修改主契约
- 验证命令强制要求实际执行，杜绝"已测试"空话
- testRunId 隔离规则严格且可操作
- 阶段门禁（POC-0 未通过不进入代码开发）保证了文档先行

### 5.2 潜在风险

| 风险 | 说明 | 建议 |
| --- | --- | --- |
| 任务单过重 | 每个任务单模板字段多达 15+，POC-0 文档任务也要求完整填写 | 区分文档任务和代码任务的模板复杂度 |
| contract-notes 合并时机不明确 | 规则说"任务结束时由主 Agent 或 Review Agent 判断"，但缺少具体触发条件 | 补充合并检查清单或自动化提示 |
| 并行子 Agent 数量上限未定义 | POC-2 建议 7 个并行角色，实际执行时上下文管理成本高 | 建议首次并行不超过 3-4 个子 Agent，验证流程后再扩展 |
| L5 验收依赖 .env | 真实 LLM 测试需要 API key，但文档未说明 key 获取流程和成本控制 | 补充 LLM 测试预算和 mock 降级策略 |

### 5.3 模板评审

4 个模板（AI任务单、ADR、测试报告、交接记录）结构合理，但：

- AI 任务单模板与 `07-AI-Native交付规约.md` 中的模板有细微差异（字段顺序、措辞）
- 建议以 `templates/AI任务单-模板.md` 为唯一权威，07 文档只引用不内联

---

## 六、契约文档评审

### 6.1 API/SSE 契约（04）

优点：
- 参考项目明确（OpenAI、AgentScope、Hermes）
- SSE envelope 设计合理，支持 Last-Event-ID 恢复
- 错误码分类清晰

问题：
- 全局候选错误码有 30+ 个，但 chat-minimal 切片只冻结 18 个，两处列表需要保持同步标注
- `RuntimeRequest` 和 `RuntimeEvent` 的 Java 类型映射未明确（String vs enum vs sealed interface）
- SSE heartbeat 间隔未定义

### 6.2 数据模型契约（05）

优点：
- 数据域划分清晰
- MyBatis/R2DBC 边界明确
- 安全原则（默认拒绝、密钥引用）到位
- OCR/附件候选模型设计前瞻

问题：
- 建议表清单有 40+ 张表，但 chat-minimal 只需 ~20 张，全局文档中未标注哪些是候选、哪些是必须
- `agent_message_checkpoint` 与 R2DBC checkpoint 的关系未明确——是同一张表还是不同存储
- 缺少索引策略和分区策略的候选方案
- 审计日志的保留期和归档策略未提及

### 6.3 小工程契约切片（02-工程契约切片）

优点：
- 从全局基线精确抽取，范围可控
- API 路径设计 RESTful 且语义清晰
- 明确标注了"暂不实现"的数据表

问题：
- `GET /api/chat/{runId}/stream` 语义不明确——是建立新 SSE 连接还是查询历史 stream？建议补充说明
- 缺少分页参数约定（`GET /api/sessions`、`GET /api/agents` 的分页方式）
- 缺少版本策略（URL path versioning vs header）

---

## 七、工程迭代文档评审

### 7.1 01-chat-minimal 整体

5 个工程文档 + ADR + 任务包结构完整，是文档体系中最成熟的部分。POC-0 到 POC-2 的阶段划分合理，门禁条件清晰。

### 7.2 ADR 质量

两个 ADR 格式规范、决策理由充分、替代方案分析到位。建议后续 ADR 保持同等质量。

### 7.3 任务包质量

POC-0 任务包（000-003）：
- 目标明确、非目标清晰
- owned files 列表详尽
- 验证命令可执行
- 状态均为 Review，说明已完成初稿

POC-1 任务包（010-015）：
- 结构完整，依赖关系合理
- TASK-V3-010 作为骨架任务，其他任务依赖它
- 验证命令包含编译和 architecture test

POC-2 任务包：
- 尚未创建，只有 README 占位
- 建议在 POC-1 完成后再创建，避免过早冻结

---

## 八、缺失文档建议

| 建议补充 | 优先级 | 理由 |
| --- | --- | --- |
| 认证方案设计 | 高 | chat-minimal 需要用户身份，但无认证方案 |
| 部署架构候选 | 中 | 影响 Docker/CI 配置和 E2E 环境搭建 |
| Spring Boot 4 选型 ADR | 中 | 非主流版本，需记录决策理由 |
| AgentScope fork 版本基线 | 中 | 接口引用需要版本锚点 |
| 前端组件库选型 | 低 | POC-2 前确认即可 |
| 性能基线和 SLA 目标 | 低 | 后续 hardening 阶段补充 |

---

## 九、对 AI Agent 执行的可用性评估

作为 AI Native 编程的输入文档，评估如下：

| 维度 | 评分 | 说明 |
| --- | --- | --- |
| 任务边界清晰度 | 9/10 | owned files + 禁止修改 + 验证命令，AI Agent 不易越界 |
| 契约可实现性 | 8/10 | DTO/SSE/SPI 定义充分，但部分字段类型需要在代码中确认 |
| 阅读路径可导航 | 8/10 | README 阅读顺序明确，但文档间交叉引用较多，Agent 需要多次跳转 |
| 验收可判定性 | 9/10 | L0-L5 等级 + 具体命令，完成标准客观 |
| 并行隔离性 | 9/10 | owned files 互斥 + contract-notes 隔离，冲突风险低 |
| 上下文负担 | 6/10 | 全量文档超过 5 万字，单个 Agent 需要阅读的"必须阅读"列表较长 |

**总体结论**：文档体系可以支撑 AI Agent 执行 POC-1 任务。建议在 POC-1 实际执行中验证"必须阅读"列表是否过长，必要时进一步精简。

---

## 十、评审结论

### 通过条件

- [x] 文档结构分层合理，权威归属明确
- [x] AI Native 规约可操作，防护机制完整
- [x] chat-minimal 范围足够小，可独立验收
- [x] 契约切片能支撑 POC-1 后端骨架开发
- [x] 阶段门禁和状态追踪机制健全

### 需要在 POC-1 启动前解决

1. 确认前端技术栈决策，关闭 OQ-V3-005 或修正 08 文档
2. 统一 Maven/Node 版本号（CLAUDE.md vs WSL2 环境）
3. 补充认证方案最小声明（至少在工程技术设计中占位）

### 建议在 POC-2 启动前解决

4. Spring Boot 4 选型 ADR
5. AgentScope fork 版本锁定
6. 部署架构候选方案
7. API 分页和版本策略
8. 并行子 Agent 数量上限建议

### 非阻塞性改进建议

9. README 补充 templates 索引入口
10. 说明 03 编号缺失原因
11. 小工程测试策略减少与全局基线的重复
12. AI 任务单模板统一为 templates/ 下唯一权威

---

## 附录：评审覆盖文件清单

```text
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/01-产品蓝图与体验目标.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/04-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/05-数据模型与安全审计-全局基线.md
docs/v3-agent中台/06-测试策略与验收等级-全局基线.md
docs/v3-agent中台/07-AI-Native交付规约.md
docs/v3-agent中台/08-开发规范与本地环境.md
docs/v3-agent中台/09-小工程切片与验收门禁.md
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
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/task.md
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
```
