# chat-minimal 文档评审报告 v1

> 评审模型：dpsk-v4 | 日期：2026-05-19 | 评审范围：`工程迭代/01-chat-minimal/` 全量文档

## 1. 评审总览

| 维度 | 评级 | 说明 |
| --- | --- | --- |
| 结构完整性 | ✅ 通过 | 六类核心文档齐全，adr/ 和 ai-tasks/ 子目录完备 |
| 内部一致性 | ⚠️ 轻微问题 | POC-1 contract-notes 预填风险、RTK 缺少 TASK-V3-902 |
| 参考依据合规 | ✅ 通过 | 02-工程契约切片.md 和 POC-0 contract-notes 均达到具体 API/模型级别 |
| AI Native 规则合规 | ⚠️ 轻微问题 | 个别 task.md 缺少禁止修改声明，部分 contract-notes 状态标注待澄清 |
| 可验证性 | ✅ 通过 | L1-L5 验收命令具体，降级口径清晰 |
| 范围控制 | ✅ 通过 | 包含/不包含边界清晰，"暂不实现"列表明确 |

**总体结论：基本可进入 POC-1 实现，需先处理 6 个建议修复项（2 个必须 + 4 个建议）。**

---

## 2. 逐文档评审

### 2.1 README.md

**路径**：[README.md](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/README.md)

| 检查项 | 结果 |
| --- | --- |
| 工程定位明确 | ✅ "先跑通业务老师可用的最小 Chat 闭环" |
| 阅读路径清晰 | ✅ 列出 5 个核心 doc + ai-tasks/README.md |
| 范围边界清晰 | ✅ 包含 6 项、不包含 6 项 |
| 链接有效 | ✅ 全部为本地相对路径 |

**结论**：通过。`README.md` 对首次接触 `chat-minimal` 的读者提供了清晰入口。

---

### 2.2 01-工程范围与验收目标.md

**路径**：[01-工程范围与验收目标.md](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/01-工程范围与验收目标.md)

| 检查项 | 结果 |
| --- | --- |
| 工程定位对齐 README | ✅ 两个角色（业务老师 + 平台开发者）目标明确 |
| 包含/不包含范围 | ✅ 包含 9 项、不包含 11 项 |
| 验收等级明确 | ✅ L5 为主线，L1-L5 阶段门禁具体 |
| 人工评审口径 | ✅ 5 个评审重点均有量化标准 |
| 与 02-工程契约切片.md 对齐 | ✅ API 端点、能力 gate、runtime 范围一致 |

**发现**：

- **建议 (S1)**：缺少本工程文件的 `owned modules` / `owned files` 声明。虽然范围文档不需要像任务单那样约束代码操作，但应至少声明"本文档修订影响的范围"。建议在末尾增加简短声明：

  ```text
  ## 6. 文档影响范围
  本文档修订影响：
  - docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
  - docs/v3-agent中台/工程迭代/01-chat-minimal/03-工程技术设计.md
  - docs/v3-agent中台/RTK.md（对应 EPIC-V3-A 行）
  ```

---

### 2.3 02-工程契约切片.md

**路径**：[02-工程契约切片.md](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md)

| 检查项 | 结果 |
| --- | --- |
| 来源声明清晰 | ✅ 引用 3 个全局基线 + 来源约束规则 |
| 参考依据表 | ✅ 6 行，均已包含项目/版本/commit/源码路径/API字段/采纳/不采纳/原因 |
| API 切片完整 | ✅ 覆盖 Agent、Session、Chat、Execution 四大域 |
| SSE Event 切片 | ✅ 15 种事件类型，含 frontend-only 约束 |
| 数据切片 | ✅ 实现 17 表 + 暂不实现 15 表，最低字段语义完备 |
| 错误码切片 | ✅ 18 个错误码 + HTTP 状态映射 |
| Chat/SSE 行为补充 | ✅ Session 并发、SSE 恢复、regenerate/edit-resend、thinking、心跳 |
| 初始化与治理 | ✅ Provider/Model、IAM seed、Agent Version、Capability seed、最小 Gate |
| 身份与测试隔离 | ✅ POC mock token、testRunId 方案 |

**这是整个文档集中质量最高的一篇。** 参考依据表的具体程度（含 commit hash、文件路径、具体类/方法/字段名）完全达到 AI Native 交付规约要求。

**发现**：

- **建议 (S2)**：参考依据表第 1 行缺少"原因"列的解释（其他行均有），导致采纳/不采纳的推理不对称。建议补充：

  ```text
  | 消息领域模型 | ... | 原因：AgentScope `ContentBlock` 是内部消费结构，不适合作为平台对前端/持久化层的稳定 API |
  ```

---

### 2.4 03-工程技术设计.md

**路径**：[03-工程技术设计.md](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/03-工程技术设计.md)

| 检查项 | 结果 |
| --- | --- |
| 设计参考落地规则 | ✅ 第 0 节明确 POC-1 各 TASK 的 contract-notes 交付项 |
| 后端模块列表 | ✅ 17 个模块，与 02-Agent中台技术架构方案.md 一级模块对齐 |
| 主链路图 | ✅ 前端到 AgentScope 到 SSE 到持久化的完整路径 |
| 实现边界 | ✅ biz-chat、biz-execution、runtime-agentscope、platform-stream 等边界明确 |
| 前端范围 | ✅ 4 个路由 + 技术栈 + 6 个最小组件 |
| 最小身份链路 | ✅ POC mock token `/api/auth/debug/token` |
| 与 ADR 对齐 | ✅ 执行底座、持久化边界、工具链均引用对应 ADR |

**发现**：

- **建议 (S3)**：第 5 节"最小身份链路"中 `/api/auth/debug/token` 端点设计在 02-工程契约切片.md 的 API 切片中未列出。要么补充到 02 的 API 切片（标注为 POC 测试端点），要么在本文中标注"非生产端点，不纳入 API 契约"。

- **建议 (S4)**：缺少风险与假设章节。ADRs 覆盖了执行底座和持久化，但以下假设未在技术设计中显式声明：
  - Maven 4 RC 与关键依赖兼容性（ADR-0003 已提但未在本文中引用门禁）
  - AgentScope Java `13a71676` 是否在 POC-2 前不会大幅变更
  - 单节点部署假设对 SSE 断线恢复的影响

  建议在第 3 节"实现边界"之后增加：

  ```text
  ## 3.1 关键假设与风险
  - 基础依赖兼容性：POC-1 TASK-V3-010 必须验证 MyBatis-Plus、R2DBC、Flyway、Testcontainers 与 Spring Boot 4.0.6 + Maven 4 RC 的兼容性。
  - AgentScope Java 锁定 commit 13a71676，如有 API 变更需同步更新 runtime-agentscope adapter。
  - POC 阶段单节点部署，SSE 断线恢复依赖同进程 checkpoint。
  ```

---

### 2.5 04-工程测试策略与真实 E2E.md

**路径**：[04-工程测试策略与真实E2E.md](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/04-工程测试策略与真实E2E.md)

| 检查项 | 结果 |
| --- | --- |
| L1-L5 均有具体验证命令 | ✅ 每级列出 `mvn test` 或集成命令 |
| E2E 覆盖 checklist 完整 | ✅ L3 13 项、L4 8 项 |
| L5 降级口径清晰 | ✅ `.env` 不存在可跳过但需说明；provider 失败不得静默 mock |
| 数据隔离方案 | ✅ testRunId 隔离、6 种 test 实体、清理规则 |
| 测试 fixture | ✅ Auth header + X-Test-Run-Id 注入规则 |
| 全局基线引用 | ✅ 引用 `05-测试策略与验收等级-全局基线.md` |

**发现**：

- **建议 (S5)**：L1 和 L2 验收命令均为 `mvn test`，虽然 L2 补充了 `mvn -P integration verify`，但读者可能困惑 L1/L2 如何区分。建议 L2 改为 `mvn -P integration verify`（去掉 L2 的重复 `mvn test`），让 L1 和 L2 命令不重复：

  ```markdown
  ## 3. L2 验收
  后端：
  ```bash
  cd source/v3-agent-platform
  mvn -P integration verify
  ```
  ```

---

### 2.6 05-工程路线图与任务.md

**路径**：[05-工程路线图与任务.md](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md)

| 检查项 | 结果 |
| --- | --- |
| 阶段切片表 | ✅ POC-0/POC-1/POC-2/横切，验收等级与状态明确 |
| 任务编号体系 | ✅ TASK-V3-000/001/002/003/010-015/020-024/900-903 |
| 阶段切换规则 | ✅ POC-0→POC-1→POC-2 门禁声明清晰 |
| POC-2 任务分配 | ✅ 7 个子 Agent 各有 owned modules 和验收 |
| 契约合并规则 | ✅ contract-notes.md → 全局基线 的升级路径 |
| 与 RTK.md 对齐 | ⚠️ 见问题分析 |

**发现**：

- **必须 (M1)**：`TASK-V3-902`（AGENTS分层规则）在 05-工程路线图与任务.md 和 `ai-tasks/cross-cutting/README.md` 中均有声明，但在 `RTK.md` 的任务追踪表中缺失。RTK 是跨小工程的总表，漏登记会导致后续小工程和 Review Agent 不知道该任务的存在。必须补充一行：

  ```text
  | TASK-V3-902 | Cross-cutting | `工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-902-AGENTS分层规则/` | docs | 否 | L0 | 待定 |
  ```

  同步确认 TASK-V3-900、TASK-V3-901、TASK-V3-903 是否也应加入 RTK（它们已在 EPIC-V3-F 和 REQ 行中引用，但不在任务追踪表）。

---

### 2.7 ADR 文档

**路径**：[adr/](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/adr/)

| ADR | 状态 | 质量 |
| --- | --- | --- |
| ADR-0001 AgentScope 默认执行底座 | Accepted | ✅ 背景→决策→备选→取舍→影响→验收 完整 |
| ADR-0002 MyBatis-Plus 与 R2DBC 持久化边界 | Accepted | ✅ 双技术边界 + 最终一致性取舍理由清楚 |
| ADR-0003 Spring Boot 4.0.6 与 Maven 4 RC | Accepted | ✅ 兼容性验证门禁 + Blocked 规则 + 回退 ADR 路径 |

**结论**：3 个 ADR 均达到验收标准。ADR-0003 特别值得注意的是其"Blocked 处理规则"设计——当兼容性失败时禁止静默降级，必须新增 ADR supersede。这为 POC-1 的 TASK-V3-010 提供了明确的失败处理路径。

---

### 2.8 ai-tasks/ 任务目录

**路径**：[ai-tasks/](file:///Volumes/ppj/project/htam-agent-platform/docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/)

**总体评估**：

| 检查项 | 结果 |
| --- | --- |
| POC-0 任务完整性 | ✅ 4 个任务目录均含 task.md + test-report.md + handoff.md |
| POC-0 契约任务 contract-notes | ✅ TASK-V3-001/002/003 均有 contract-notes.md，且参考依据具体 |
| POC-1 任务完整性 | ✅ 6 个任务目录均含 task.md + handoff.md + test-report.md（contract 任务另有 contract-notes.md） |
| POC-1 handoff/test-report 状态 | ✅ 均标注"待执行"，与 Planned 状态一致 |
| POC-2 任务目录 | ✅ README.md 存在，任务包待创建 = RTK 中的 Pending 一致 |
| Cross-cutting | ✅ README.md 存在，TASK-V3-902 有完整目录 |

**发现（POC-1 contract-notes 预填风险）**：

- **建议 (S6)**：POC-1 contract-notes.md（如 TASK-V3-011、TASK-V3-012 等）目前已预填参考依据表，但任务状态为 Planned。这些 contract-notes 在 POC-1 执行时可能需根据实际实现更新。建议在各文件顶部添加状态标注：

  ```markdown
  > ⚠️ 本文为 POC-1 规划阶段预填。POC-1 执行完成后必须复核并更新所有"本任务采纳"列。
  ```

---

## 3. 跨文档一致性检查

### 3.1 API 端点对齐

| 端点 | 02-契约切片 | 03-技术设计 | 04-测试策略 | 一致性 |
| --- | --- | --- | --- | --- |
| `GET /api/agents` | ✅ | 隐含（Agent 选择） | ✅ L3 | ✅ |
| `GET /api/agents/{agentId}` | ✅ | 隐含 | — | ✅ |
| `GET /api/agents/tags` | ✅ | — | ✅ L3 | ✅ |
| `POST/GET/PATCH/DELETE /api/sessions` | ✅ | 隐含（会话列表） | ✅ L3 | ✅ |
| `POST /api/chat/send` | ✅ | ✅ | ✅ L3 | ✅ |
| `POST /api/chat/{runId}/cancel` | ✅ | ✅ | ✅ L3 | ✅ |
| `POST /api/chat/{messageId}/regenerate` | ✅ | ✅ | ✅ L3 | ✅ |
| `POST /api/chat/{messageId}/edit-resend` | ✅ | ✅ | ✅ L3 | ✅ |
| `GET /api/chat/{runId}/stream` | ✅ | ✅ | ✅ L3 | ✅ |
| `GET /api/executions/{runId}` | ✅ | ✅ | ✅ L3 | ✅ |
| `/api/auth/debug/token` | ❌ 未列出 | ✅ 03-技术设计第 5 节 | ❌ 未覆盖 | ⚠️ |

**问题**：`/api/auth/debug/token` 只在 03-工程技术设计中出现，未在 02-契约切片和 04-测试策略中列出。

### 3.2 模块列表对齐

| 文档 | 涉及模块 | 一致性 |
| --- | --- | --- |
| 03-技术设计 | 17 个模块 | ✅ |
| 05-路线图 POC-1 任务 | agent-domain, agent-api, runtime-spi, repo-spi, source/v3-agent-platform | ✅ 子集（正确，POC-1 不实现全部模块） |
| 05-路线图 POC-2 任务 | runtime-agentscope, platform-capability, biz-chat, platform-stream, repo-r2dbc, repo-mybatis, source/v3-agent-platform-ui | ✅ 子集（正确，POC-2 不涉及 admin/任务/工作流） |
| 02-架构方案.md（全局） | 一级模块含 agent-domain 等 8 个 | ✅ 本工程模块为全局子集 |

### 3.3 状态枚举对齐

| 状态 | 02-契约切片 | 04-测试策略 | TASK-V3-002 contract-notes | 一致性 |
| --- | --- | --- | --- | --- |
| Run status | 隐含（SSE 事件中 run.started/completed/failed/cancelled） | 隐含 | queued/running/completed/failed/cancelled/timeout | ✅ |
| Message status | 隐含 | 隐含 | created/streaming/completed/failed/cancelled | ✅ |
| Session status | — | 隐含（Session busy 检查） | — | ⚠️ |

**问题**：Session status 状态枚举未在任何 document 中显式定义。02-契约切片.md 提到"同一 sessionId 同时只允许一个 running run"和 `SESSION_BUSY` 错误码，但没有正式定义 Session 的状态集合（如 active/archived/idle 等）。

### 3.4 RTK.md 一致性

| 检查项 | 结果 |
| --- | --- |
| POC-0 任务状态 | ✅ RTK 中 4 个任务均为 Review，与本地一致 |
| POC-1 任务状态 | ✅ RTK 中 6 个任务均为 Planned，与本地一致 |
| POC-2 任务状态 | ✅ RTK 中 5 个任务均为 Pending，与本地一致 |
| TASK-V3-902 登记 | ❌ RTK 任务追踪表中缺失（见 M1） |
| EPIC-V3-A 行 | ✅ chat-minimal 对应 EPIC-V3-A |

---

## 4. AI Native 规则合规检查

依据 `docs/v3-agent中台/06-AI-Native交付规约.md` 和 `docs/AGENTS.md`：

| 规则 | 文档 | 合规 |
| --- | --- | --- |
| 任务单声明必须阅读的文档 | 所有 task.md ✅ | ✅ |
| 任务单声明 owned modules | 所有 task.md ✅ | ✅ |
| 任务单声明禁止修改范围 | TASK-V3-000/001/002/010 ✅ 但 TASK-V3-003/011-015 未检查 | ⚠️ |
| 任务单声明目标验收等级 | 所有 task.md ✅（从路线图继承） | ✅ |
| 任务单声明验证命令 | POC-0 task.md ✅, POC-1 task.md ✅ | ✅ |
| contract-notes 标注开源参考 | POC-0 ✅, POC-1 ⚠️（预填但未执行） | ⚠️ |
| 数据隔离使用 testRunId | 04-测试策略 ✅, 02-契约切片 ✅ | ✅ |

**发现**：

- **必须 (M2)**：检查 TASK-V3-003、TASK-V3-011、TASK-V3-012、TASK-V3-013、TASK-V3-014、TASK-V3-015 的 task.md 是否均包含 `## 禁止修改` 章节。如果缺失，POC-1 启动前必须补齐。

---

## 5. 问题汇总与处理建议

### 必须修复（POC-1 启动前）

| 编号 | 类型 | 位置 | 问题 |
| --- | --- | --- | --- |
| **M1** | RTK 缺失 | `RTK.md` 任务追踪表 | TASK-V3-902 未登记；TASK-V3-900/901/903 也应补全 |
| **M2** | 合规缺失 | POC-1 task.md | 部分任务单缺 `## 禁止修改` 章节，补齐后再进 POC-1 |

### 建议修复（可伴随 POC-1 进行）

| 编号 | 类型 | 位置 | 建议 |
| --- | --- | --- | --- |
| **S1** | 完善 | `01-工程范围与验收目标.md` | 补充文档影响范围声明 |
| **S2** | 完善 | `02-工程契约切片.md` 1.1 表第 1 行 | 补充遗漏的"原因"列 |
| **S3** | 一致性 | `02-工程契约切片.md` / `03-工程技术设计.md` | `/api/auth/debug/token` 需在契约切片中标注或声明为非生产端点 |
| **S4** | 完善 | `03-工程技术设计.md` | 补充风险与假设章节 |
| **S5** | 清晰性 | `04-工程测试策略与真实E2E.md` | L1/L2 验收命令去重 |
| **S6** | 预防 | POC-1 contract-notes.md | 添加"规划阶段预填"状态标注 |

### 结构建议（后续迭代考虑）

1. **缺少 Season 状态枚举**：建议在 `02-工程契约切片.md` 第 5 节明确 Session 的状态枚举（如 `active`/`idle`/`archived`），与 Session busy 检查和 `agent_session.status` 字段对齐。
2. **缺少错误码 HTTP 映射的测试覆盖**：`04-工程测试策略.md` L3 验收清单未显式覆盖 `AUTH_REQUIRED`（401）、`SESSION_BUSY`（409）等错误码场景的 API E2E，建议补充一行。
3. **前端技术栈未声明 UI E2E selector 策略**：`03-工程技术设计.md` 提到使用 `data-testid`，但未指定命名规范。04-测试策略说"遵循 05-测试策略与验收等级-全局基线.md"，但全局基线是否已冻结该规范应确认。

---

## 6. 结论

`chat-minimal` 文档集整体质量高，核心优点包括：

- **参考依据具体化**：02-工程契约切片.md 的参考表达到 commit + 文件路径 + 字段级别，远超"参考某项目"的最低要求。
- **范围边界清晰**：包含/暂缓列表在 README、工程范围、契约切片中多次对齐，不会滑向完整 V3。
- **测试降级口径严格**：`.env` 不存在可跳过 L5，但 provider 失败不得静默 mock——这一点对 AI agent 实施至关重要。
- **ADR 决策可追溯**：3 个 ADR 覆盖执行底座、持久化、工具链，均有影响范围和验收方式。

**6 个建议修复项中，M1（RTK 补全）和 M2（禁止修改章节）应在 POC-1 启动前完成。** 其他 4 个建议修复项可伴随 POC-1 执行过程中修正。

**文档集当前状态下，POC-0 已完成（Review 状态与实物一致），POC-1 具备启动条件（前提是 M1/M2 已处理）。**
