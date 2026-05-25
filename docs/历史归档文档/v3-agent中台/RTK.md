# V3 Agent 中台 RTK

RTK = Requirements / Tasks / Knowledge。

本文用于追踪 V3 Agent 中台的需求、任务、知识依据、契约、测试和决策状态。它是“总追踪表”，不替代产品、架构、契约、测试、AI 任务单或 ADR 文档。

## 1. 定位

RTK 解决三个问题：

- 需求是否已经拆成任务。
- 任务是否有契约、测试和验收等级。
- 关键知识依据是否能追溯到文档、源码或 ADR。

维护规则：

- 新增产品目标或核心能力时，在本文登记追踪关系。
- 新增 AI 任务包时，在任务追踪表登记。
- 契约冻结、测试覆盖、ADR 状态变化时，更新对应表格。
- 本文只记录索引和状态，不复制主文档正文。
- 任务执行细节写入对应小工程的 `工程迭代/<小工程>/ai-tasks/TASK-*/`。

## 2. 产品目标追踪

| 需求编号 | 产品目标 | 来源文档 | 对应任务 | 目标验收等级 | 状态 |
| --- | --- | --- | --- | --- | --- |
| REQ-V3-001 | 企业内部 Agent 中台，而不是单一 Chat 应用 | `01-产品蓝图与体验目标.md` | `TASK-V3-000` | L0 | Planned |
| REQ-V3-002 | 业务老师可按标签选择被授权 Agent 或工作流完成任务 | `01-产品蓝图与体验目标.md` | `TASK-V3-020`、`TASK-V3-024`、`TASK-V3-040` | L4-L5 | Review：chat-minimal L5 通过，工作流待后续 |
| REQ-V3-003 | 管理员可配置通用 Agent、Provider、Capability、任务模板和用户授权 | `01-产品蓝图与体验目标.md` | `TASK-V3-030`、`TASK-V3-031`、`TASK-V3-034` | L4-L5 | Pending |
| REQ-V3-004 | 普通 Chat 默认使用 `agentscope-java` 的 `ReActAgent` | `02-Agent中台技术架构方案.md`、`docs/调研/README.md` | `TASK-V3-012`、`TASK-V3-020` | L5 | Review：已适配，真实 provider L5 通过 |
| REQ-V3-005 | 高风险执行通过 `agentscope-runtime-java` 沙箱底座 | `02-Agent中台技术架构方案.md`、`docs/调研/README.md` | `TASK-V3-012`、`TASK-V3-021` | L3-L5 | Pending |
| REQ-V3-006 | MCP、Skills、Tool 必须纳入审核、授权、Capability gate、审计和观测 | `01-产品蓝图与体验目标.md`、`04-数据模型与安全审计-全局基线.md` | `TASK-V3-021`、`TASK-V3-031`、`TASK-V3-033` | L4-L5 | Pending |
| REQ-V3-007 | UI E2E 必须通过 Playwright 真实操作页面验证 | `05-测试策略与验收等级-全局基线.md` | `TASK-V3-024`、`TASK-V3-900` | L4 | Review：chat-minimal Playwright 通过 |
| REQ-V3-008 | 所有 E2E 数据必须使用 `testRunId` 隔离和清理 | `05-测试策略与验收等级-全局基线.md` | `TASK-V3-900`、`TASK-V3-901` | L3-L5 | Review：chat-minimal 使用 `testRunId`，真实 DB 清理通过 |
| REQ-V3-009 | 非多模态主模型可通过受控 OCR / input-preprocessor 理解独立图片、扫描页和上传文件内嵌图片 | `01-产品蓝图与体验目标.md`、`02-Agent中台技术架构方案.md`、`03-API-SSE-运行时契约-全局基线.md`、`04-数据模型与安全审计-全局基线.md` | `TASK-V3-050` | L4-L5 | Pending |

## 3. 核心能力追踪

| 能力编号 | 能力名称 | 主要模块 | 关键契约 | 数据/审计 | 测试要求 | 状态 |
| --- | --- | --- | --- | --- | --- | --- |
| CAP-V3-001 | Agent 库与标签 | `biz-agent`、`admin-agent` | Agent API、Agent Schema | Agent、Tag、Permission、Audit | API E2E、Playwright | Review：chat-minimal 查询和筛选通过 |
| CAP-V3-002 | Chat 工作台流式对话 | `biz-chat`、`runtime-agentscope`、`platform-stream` | Chat API、SSE Envelope、RuntimeEvent | Session、Message、Run、Span、Stream checkpoint | API/SSE E2E、Playwright、e2e-real | Review：L5 真实链路通过 |
| CAP-V3-003 | Capability Gate | `platform-capability`、`admin-capability` | RuntimeCapabilityPlan、错误码 | Capability、授权、Audit | 单元、集成、API E2E | Review：未审核 capability 拒绝通过 |
| CAP-V3-004 | Provider 管理 | `platform-provider`、`admin-provider` | Provider API | Provider、Credential ref、Audit | API E2E、脱敏检查 | Pending |
| CAP-V3-005 | SSE 持久化 | `platform-stream`、`repo-r2dbc-stream` | SSE event、checkpoint schema | Stream checkpoint、final flush | 并发、断线重连、恢复测试 | Review：真实 R2DBC checkpoint 与 `Last-Event-ID` 回放通过 |
| CAP-V3-006 | Run / Span 观测 | `platform-observability` | Run/Span Schema | Run、Span、Trace、Audit | API E2E、查询验证 | Review：执行详情查询通过 |
| CAP-V3-007 | 任务中心 | `biz-task`、`admin-task` | Task API、Task Schema | Task、Task result、Audit | API E2E、Playwright | Pending |
| CAP-V3-008 | Agent 工作流 | `biz-workflow`、`admin-workflow`、`platform-orchestration` | Workflow API、WorkflowRun Schema | Workflow、Step、Run、Audit | API E2E、Playwright | Pending |
| CAP-V3-009 | 外部复杂 Agent 接入 | `runtime-hermes` 占位、外部 Agent adapter | ExternalAgent Schema | ExternalAgent、授权、Audit | Stub E2E、风险 gate | Pending |
| CAP-V3-010 | 图片型输入 OCR 预处理 | `platform-input`、`platform-capability`、`platform-provider`、`biz-chat` | Attachment API、`input.ocr.*`、`derivedContexts`、`sourceLocator` | Attachment、DerivedContext、Audit | API/SSE E2E、Playwright、OCR provider 健康检查 | Pending |

## 3.1 小工程追踪

| Epic | 小工程 | 范围 | 关键任务 | contract slice | 目标验收 | 状态 |
| --- | --- | --- | --- | --- | --- | --- |
| EPIC-V3-A | chat-minimal | Agent 库、标签筛选、Session、Chat、SSE、Run/Span、最小前端闭环 | `TASK-V3-010` 至 `TASK-V3-015`、`TASK-V3-020` 至 `TASK-V3-024` | `工程迭代/01-chat-minimal/02-工程契约切片.md` | L5 passed | Review |
| EPIC-V3-B | admin-config | Agent、Provider、Capability、用户授权管理 | `TASK-V3-030`、`TASK-V3-031`、`TASK-V3-034` | 待抽取 | L4-L5 | Pending |
| EPIC-V3-C | task-center | 任务模板、任务运行、任务成果 | `TASK-V3-040` | 待抽取 | L4 | Pending |
| EPIC-V3-D | workflow-sequential | 顺序工作流定义、执行和进度 | `TASK-V3-041` | 待抽取 | L4-L5 | Pending |
| EPIC-V3-E | external-agent | 外部复杂 Agent 登记、stub 调用、审计 | `TASK-V3-042`、`TASK-V3-043` | 待抽取 | L3-L4 | Pending |
| EPIC-V3-F | hardening-ci | CI、真实 E2E、失败恢复、安全加固 | `TASK-V3-900`、`TASK-V3-901`、`TASK-V3-903` | `工程迭代/01-chat-minimal/ai-tasks/cross-cutting/` | L4-L5 | Review：默认 PR CI 已接入，L5 real 保留手动 / self-hosted |
| EPIC-V3-G | input-ocr | 图片型输入、文件内嵌图片抽取、OCR 预处理、派生上下文、非多模态主模型对话适配 | `TASK-V3-050` | 待抽取 | L4-L5 | Pending |

## 4. 任务追踪

| 任务编号 | 阶段 | 任务包 | owned modules | 是否触碰公共契约 | 目标验收等级 | 状态 |
| --- | --- | --- | --- | --- | --- | --- |
| TASK-V3-000 | POC-0 | `工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-000-文档索引复核/` | docs | 否 | L0 | Review |
| TASK-V3-001 | POC-0 | `工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/` | docs | 是 | L0 | Review |
| TASK-V3-002 | POC-0 | `工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/` | docs | 是 | L0 | Review |
| TASK-V3-003 | POC-0 | `工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/` | docs | 是 | L0 | Review |
| TASK-V3-010 | POC-1 | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-010-Maven多模块骨架/` | `source/v3-agent-platform` | 否 | L2 | Review |
| TASK-V3-011 | POC-1 | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-011-agent-domain契约/` | `agent-domain` | 是 | L1 | Review |
| TASK-V3-012 | POC-1 | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-012-runtime-spi契约/` | `agent-runtimes/runtime-spi` | 是 | L1 | Review |
| TASK-V3-013 | POC-1 | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-013-repo-spi契约/` | `agent-repo/repo-spi` | 是 | L1 | Review |
| TASK-V3-014 | POC-1 | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-014-architecture-test/` | `source/v3-agent-platform` | 否 | L2 | Review |
| TASK-V3-015 | POC-1 | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-015-agent-api契约/` | `agent-api` | 是 | L1 | Review |
| TASK-V3-020 | POC-2 | `工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-020-runtime-agentscope最小适配/` | `runtime-agentscope` | 是 | L5 | Review |
| TASK-V3-021 | POC-2 | `工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-021-platform-capability最小gate/` | `platform-capability`、`biz-chat` | 是 | L3-L5 | Review |
| TASK-V3-022 | POC-2 | `工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-022-biz-chat主流程/` | `biz-chat`、`biz-session`、`biz-agent`、`biz-execution`、`agent-boot` | 是 | L5 | Review |
| TASK-V3-023 | POC-2 | `工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-023-stream-persistence/` | `platform-stream`、`repo-spi`、`repo-mybatis`、`repo-r2dbc-stream` | 是 | L3-L5 | Review |
| TASK-V3-024 | POC-2 | `工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-024-Chat前端最小闭环/` | `source/v3-agent-platform-ui` | 是 | L4-L5 | Review |
| TASK-V3-030 | 后续 | 待创建 | `admin-agent`、`admin-provider` | 是 | L4-L5 | Pending |
| TASK-V3-031 | 后续 | 待创建 | `admin-capability`、`platform-capability` | 是 | L4-L5 | Pending |
| TASK-V3-033 | 后续 | 待创建 | `admin-audit`、`platform-observability` | 是 | L4-L5 | Pending |
| TASK-V3-034 | 后续 | 待创建 | `admin-iam`、`platform-security` | 是 | L4-L5 | Pending |
| TASK-V3-040 | 后续 | 待创建 | `biz-task`、`admin-task` | 是 | L4 | Pending |
| TASK-V3-041 | 后续 | 待创建 | `biz-workflow`、`admin-workflow`、`platform-orchestration` | 是 | L4-L5 | Pending |
| TASK-V3-042 | 后续 | 待创建 | `admin-agent`、`agent-domain` | 是 | L3-L4 | Pending |
| TASK-V3-043 | 后续 | 待创建 | `runtime-hermes`、外部 Agent adapter | 是 | L3-L4 | Pending |
| TASK-V3-050 | 后续 | 待创建 | `platform-input`、`platform-capability`、`biz-chat`、`agent-api`、`agent-domain` | 是 | L4-L5 | Pending |
| TASK-V3-900 | 横切 | `工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-900-Playwright-E2E套件/` | `source/v3-agent-platform-ui/tests` | 否 | L4 | Review |
| TASK-V3-901 | 横切 | `工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-901-e2e-real套件/` | `source/test/v3` | 否 | L5 | Review |
| TASK-V3-902 | 横切 | `工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-902-AGENTS分层规则/` | AGENTS | 否 | L0 | Review |
| TASK-V3-903 | 横切 | `工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-903-CI质量门禁/` | `.github/workflows` | 否 | L2-L4 | Review |

## 5. 契约追踪

| 契约类型 | 主文档 | 任务草案 | 是否冻结 | 关联 ADR | 状态 |
| --- | --- | --- | --- | --- | --- |
| API / SSE / Runtime | `03-API-SSE-运行时契约-全局基线.md` | `工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-001-API-SSE-Runtime契约冻结/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-012-runtime-spi契约/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-015-agent-api契约/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-020-runtime-agentscope最小适配/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-022-biz-chat主流程/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-023-stream-persistence/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-024-Chat前端最小闭环/contract-notes.md` | 是 | `ADR-0001` | Review |
| 数据模型 / 安全 / 审计 | `04-数据模型与安全审计-全局基线.md` | `工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-002-数据模型与安全审计冻结/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-013-repo-spi契约/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-021-platform-capability最小gate/contract-notes.md`、`工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-023-stream-persistence/contract-notes.md` | 是 | `ADR-0002` | Review |
| 小工程 contract slice | `工程迭代/01-chat-minimal/02-工程契约切片.md` | `工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/` | 是 | 待定 | Review |
| AI Native 任务规则 | `06-AI-Native交付规约.md` | `工程迭代/01-chat-minimal/ai-tasks/README.md` | 部分冻结 | 待定 | Review |
| 后端模块依赖方向 | `02-Agent中台技术架构方案.md` | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-010-Maven多模块骨架/`、`工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-014-architecture-test/` | 否 | 待定 | Review |
| 运行时选型 | `02-Agent中台技术架构方案.md`、`docs/调研/README.md` | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-012-runtime-spi契约/`、`工程迭代/01-chat-minimal/ai-tasks/poc-2/TASK-V3-020-runtime-agentscope最小适配/` | 部分冻结 | `ADR-0001` | Review |
| 工具链基线 | `07-开发规范与本地环境.md` | `工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-010-Maven多模块骨架/` | 是 | `ADR-0003` | Review |

## 6. 测试追踪

| 场景 | API/SSE E2E | Playwright UI E2E | e2e-real | testRunId | 当前状态 |
| --- | --- | --- | --- | --- | --- |
| 文档与任务包质量 | 不需要 | 不需要 | 不需要 | 不需要 | L0 检查中 |
| 后端模块骨架 | 不需要 | 不需要 | 不需要 | 不需要 | `mvn test` 通过 |
| Chat 流式主链路 | 已覆盖 | 已覆盖 | 已执行，含 DB E2E 脚本 | 已使用并清理 | Review：L5 通过 |
| Agent 库与授权 | 已覆盖 | 已覆盖 | 按场景 | 已使用 | Review |
| Capability Gate | 已覆盖 | 管理端后续 | 按场景 | 已使用 | Review：未审核拒绝通过 |
| SSE 断线恢复 | 已覆盖 | 建议 | 已执行 | 已使用并清理 | Review：真实 DB checkpoint `Last-Event-ID` 通过 |
| 任务中心 | 必须 | 必须 | 按场景 | 必须 | Pending |
| 工作流顺序编排 | 必须 | 必须 | 按场景 | 必须 | Pending |
| 外部复杂 Agent 接入 | 必须 | 建议 | 按场景 | 必须 | Pending |
| 图片型输入 OCR 预处理 | 必须 | 必须 | 按 OCR provider 场景 | 必须 | Pending |

## 7. 知识依据追踪

| 知识编号 | 主题 | 权威来源 | 用途 | 状态 |
| --- | --- | --- | --- | --- |
| K-V3-001 | AgentScope Java 与 AgentScope Runtime Java 分工 | `docs/调研/README.md`、`docs/调研/AgentScope-Java与AgentScope-Runtime-Java区别.md` | 运行时选型 | Adopted |
| K-V3-002 | AgentScope Java 源码接口 | 本地 fork commit `13a71676`；`source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamableAgent.java`、`EventType.java`、`StreamOptions.java` | Runtime SPI、SSE 映射；后续 fork 升级必须先评估接口差异 | Adopted |
| K-V3-003 | AgentScope Runtime Java 源码接口 | 本地 fork commit `7b9032b`；`source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/AgentHandler.java`、`agentscope/AgentScopeAgentHandler.java` | sandbox runtime 参考；后续 fork 升级必须先评估接口差异 | Adopted |
| K-V3-004 | Hermes Agent 取舍 | `docs/调研/HermesAgent替代AgentScope执行侧方案对比.md` | `runtime-hermes` 占位和外部 Agent 接入参考 | Adopted as reference |
| K-V3-005 | UI E2E 与 testRunId | `05-测试策略与验收等级-全局基线.md` | 验收与数据隔离 | Adopted |
| K-V3-006 | AI Native 交付方法 | `06-AI-Native交付规约.md`、`08-小工程切片与验收门禁.md`、`AGENTS.md` | 小工程内多 Agent 并行开发 | Review |
| K-V3-007 | 图片型输入与文档内图片处理 | Codex image input issues、Dify Knowledge Pipeline、Docling、Unstructured `partition_image` | 显式图片输入、文件内图片抽取、sourceLocator、OCR 策略和派生上下文 | Adopted as reference |
| K-V3-008 | V3 后端工具链基线 | `07-开发规范与本地环境.md`、`AGENTS.md`、`工程迭代/01-chat-minimal/adr/ADR-0003-Spring-Boot-4.0.6与Maven-4-RC工具链选型.md` | JDK 21、Spring Boot 4.0.6、Maven 4.x RC 版本约束和兼容性门禁 | Adopted |

## 8. 决策与开放问题

| 编号 | 类型 | 内容 | 处理方式 | 状态 |
| --- | --- | --- | --- | --- |
| OQ-V3-001 | ADR | AgentScope 作为普通 Chat 默认执行底座是否需要正式 ADR | 已落入 `工程迭代/01-chat-minimal/adr/ADR-0001-AgentScope普通Chat默认执行底座.md` | Done |
| OQ-V3-002 | ADR | MyBatis-Plus + R2DBC 双持久化边界是否需要正式 ADR | 已落入 `工程迭代/01-chat-minimal/adr/ADR-0002-MyBatis-Plus与R2DBC持久化边界.md` | Done |
| OQ-V3-003 | 契约 | RuntimeEvent 与 SSE event 是否一一对应，还是允许聚合映射 | `TASK-V3-001` 明确允许一对一或聚合映射，需保留 runId/sequence/traceId | Review |
| OQ-V3-004 | 安全 | 高风险 capability 的风险等级和默认禁用策略 | `TASK-V3-002` 已冻结默认拒绝和 sandbox/disabled 边界，细化由 `TASK-V3-021` 完成 | Review |
| OQ-V3-005 | 前端 | V3 UI 技术栈和组件库是否先沿用旧实现经验或重新选型 | Vue 3、TypeScript、Pinia、Vite、Vue Router 4、Element Plus、fetch wrapper 和 fetch ReadableStream SSE 作为 POC-2 前端基线；旧实现只通过 `source/htam-agent-platform-ui` 参考体验和局部实现，不作为 V3 代码根目录 | Done |
| OQ-V3-006 | 契约 | input-ocr 小工程的附件上传方式、文件内嵌图片定位方式、OCR provider 选型、派生上下文保留策略和错误码是否一次冻结 | 创建 `TASK-V3-050` 前抽取 contract slice 并补 ADR 或任务内 `contract-notes.md` | Open |
| OQ-V3-007 | 工具链 | Spring Boot、JDK、Maven 版本是否需要重新确认 | 已固定为 JDK 21、Spring Boot 4.0.6、Maven 4.x RC；兼容性门禁见 `工程迭代/01-chat-minimal/adr/ADR-0003-Spring-Boot-4.0.6与Maven-4-RC工具链选型.md`，后续主版本切换必须新增 ADR supersede | Done |
| OQ-V3-008 | 契约 | `WorkflowRun` 是否需要 `waiting` 状态，以及人工审批 / 外部回调是否进入首个工作流小工程 | 当前只作为后续 workflow-sequential 候选状态登记；EPIC-V3-D 启动前冻结状态机和验收口径 | Open |
| OQ-V3-009 | 安全 / 观测 | `HINT`、工具原始参数、MCP 请求和外部响应的管理端展示边界如何冻结 | 当前普通用户只看脱敏摘要；原始参数只允许进入 Run/Span/Audit，管理端展示规则由 `platform-security` / `platform-observability` 后续任务冻结 | Open |
| OQ-V3-010 | 持久化 / L5 | `chat-minimal` 当前持久化是否满足真实 PostgreSQL、MyBatis-Plus、R2DBC checkpoint 和 Flyway seed 要求 | PostgreSQL、Flyway seed、MyBatis-Plus Mapper 普通 CRUD、R2DBC checkpoint 和 L5 已通过；多节点恢复和 `repo-r2dbc-stream` 独立 adapter 拆分留到 hardening | Done |

## 9. 状态更新规则

状态值：

```text
Pending
Planned
In Progress
Blocked
Review
Done
Superseded
```

更新要求：

- 尚未创建任务包的任务保持 `Pending`。
- 任务包创建后，把任务状态从 `Pending` 改为 `Planned`。
- 任务开始执行后，任务包和本文都改为 `In Progress`。
- 任务完成后，更新 `test-report.md`、`handoff.md`，并把本文状态改为 `Review` 或 `Done`。
- 契约冻结后，主文档和本文都要标注冻结状态。
- 重大决策落入 ADR 后，在本文关联 ADR 编号。
