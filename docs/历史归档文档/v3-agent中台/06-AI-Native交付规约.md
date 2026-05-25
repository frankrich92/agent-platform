# AI Native 交付规约

## 1. 定位

AI Native 交付不是“让 AI 随便写代码”，而是在清晰任务、清晰边界、清晰验收等级下，让 AI agent 可持续完成真实工程任务。

本文是后续所有 AI agent 开发任务的执行规约。

## 2. 基本流程

每个真实开发任务按以下流程执行：

```text
需求澄清
 -> 确认所属小工程
 -> 方案竞争
 -> 契约冻结
 -> 任务单确认
 -> 模块并行实现
 -> 自动化自检
 -> 真实链路验证
 -> 独立评审
 -> 文档与交接
```

不得跳过任务单直接开工。

## 3. AI 任务单模板

```text
任务名称：

所属小工程：

背景：

目标：

非目标：

必须阅读：
- 全局规则（精读 / 了解）：
- 本小工程（精读 / 了解）：
- 任务包（精读）：

owned modules：

owned files：

禁止修改：

输入契约：

输出契约：

目标验收等级：L0/L1/L2/L3/L4/L5

必须执行的验证命令：

真实 E2E 要求：

风险：

完成后输出：
- 已完成内容
- 修改范围
- 未完成内容
- 测试结果
- 风险
- 是否触碰公共契约
```

任务阅读隔离规则：

- 新任务必须先声明所属小工程，例如 `01-chat-minimal`、`02-admin-config`。
- `必须阅读` 只列全局规则、本小工程文档和本任务包文档。
- `精读` 表示任务执行前必须理解并遵守；`了解` 表示只用于背景，不得把其中未冻结内容扩大为当前任务范围。
- 不得把其他小工程文档加入默认阅读路径。
- 跨工程任务必须在任务单中显式列出影响的小工程、补读文档和原因。
- 小工程未创建时，先创建小工程目录和契约切片，再创建代码任务。

## 4. owned modules / owned files

每个 AI 子 Agent 必须有明确所有权。

规则：

- 只能修改任务单授权的模块和文件。
- 公共契约变更必须先冻结并通知相关任务。
- 不允许跨模块顺手修。
- 不允许为了测试方便绕过真实接口或权限。
- 如果必须修改未授权文件，先停下来说明原因。

## 5. 子 Agent 并行开发

子 Agent 并行开发必须发生在明确的小工程和任务包内。小工程按可验收业务闭环拆分，子 Agent 只是在该小工程内部按 owned modules / owned files 并行执行。

不允许为了并行开发，把小工程拆成 `runtime-agent 工程`、`frontend-agent 工程`、`repo-agent 工程` 这类纯技术模块。技术模块可以成为任务包或子 Agent 分工，但必须回到所属小工程的 contract slice 和真实验收链路。

并行开发前先冻结本小工程当前阶段的公共契约：

```text
agent-domain
agent-api
runtime-spi
repo-spi
```

并行就绪条件：

- 本小工程 `02-工程契约切片.md` 已冻结当前阶段 API、SSE、RuntimeEvent、数据、权限、错误码和验收等级。
- 本小工程 `03-工程技术设计.md` 已明确激活模块、主链路、依赖方向和暂缓技术项。
- 任务包 `task.md` 已列出子 Agent 分工、owned modules、owned files、禁止修改范围和验证命令。
- 并行子 Agent 的 owned files 尽量互斥；共享文件必须指定唯一 owner。
- 公共契约只能由 contract owner、主 Agent 或 Review Agent 汇总回写；其他子 Agent 的细化先写入任务目录 `contract-notes.md`。
- E2E / Review 子 Agent 默认只做验证和复核，不顺手改业务实现；如需修改，必须先补充 owned files。
- `pom.xml`、Flyway 迁移、公共 DTO、公共枚举、测试 fixture、AGENTS 规则等共享文件必须在任务单中指定唯一 owner；非 owner 只能在 `contract-notes.md` 或交接记录中提出变更建议。
- 子 Agent 发现未授权文件必须修改时，先停止该部分修改，在交接中说明原因、影响、建议 owner 和最小变更范围，不直接顺手改。

推荐分工可按任务包裁剪，不要求每次全部启用：

```text
domain-agent：agent-domain
api-contract-agent：agent-api + SSE 契约
runtime-agent：agent-runtimes
repo-agent：agent-repo
biz-agent：agent-biz
admin-agent：agent-admin
workflow-agent：platform-orchestration + biz-workflow + admin-workflow
task-agent：biz-task + admin-task
frontend-agent：前端页面和组件
e2e-agent：Playwright、API/SSE、e2e-real
security-review-agent：权限、安全、审计
docs-agent：文档和交接
```

任务包应在 `task.md` 中保留一张并行分工表，记录每个子 Agent 的职责、owned files、依赖输入、输出物和验证命令。

## 6. 子 Agent 输出格式

每个子 Agent 完成后必须输出：

```text
已完成内容：

修改范围：

未完成内容：

测试结果：

风险：

是否触碰公共契约：

后续建议：
```

测试结果必须包含实际执行过的命令和结果。不能只写“已测试”。

## 7. AI 任务过程文档目录

多 AI agent 并行开发时，应按“任务包”隔离过程文档，不按长期 Agent 名字隔离。

推荐目录：

```text
docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/
├── README.md
├── poc-0/
│   ├── README.md
│   └── TASK-V3-001-API-SSE-Runtime契约冻结/
│       ├── task.md
│       ├── contract-notes.md
│       ├── test-report.md
│       └── handoff.md
├── poc-1/
│   ├── README.md
│   └── TASK-V3-010-Maven多模块骨架/
├── poc-2/
│   ├── README.md
│   └── TASK-V3-020-chat-main-flow/
│       ├── task.md
│       ├── contract-notes.md
│       ├── runtime-agent.md
│       ├── biz-agent.md
│       ├── frontend-agent.md
│       ├── e2e-agent.md
│       ├── test-report.md
│       └── handoff.md
└── cross-cutting/
    ├── README.md
    └── TASK-V3-900-playwright-e2e/
```

目录规则：

- `ai-tasks` 可以提交，用于保存高价值任务单、交接记录、测试摘要和评审结论。
- 不按 `agent-a`、`agent-b` 这种长期身份建目录；AI agent 是临时执行者，任务才是追溯单元。
- POC 阶段目录由小工程自行定义；不得因为某个小工程已有 `poc-*` 目录，就要求其他小工程照搬。
- 一个 `TASK-V3-xxx-*` 目录只服务一个任务包，必须放在所属阶段目录下。
- 子 Agent 文件名使用角色名，例如 `runtime-agent.md`、`frontend-agent.md`。
- 正式架构规则不在任务目录重复维护，必须回写到正式文档。
- `03-API-SSE-运行时契约-全局基线.md` 是稳定主契约；任务内接口、SSE、RuntimeEvent、错误码细化先写入 `contract-notes.md`，不要让多个子 Agent 并行直接频繁修改主契约。
- 临时日志、截图、trace、模型中间过程不放入该目录，可放入被 `.gitignore` 忽略的本地临时目录。

每个子 Agent 文件建议固定格式：

```text
# 子 Agent 名称

## owned modules / owned files

## 已完成内容

## 修改范围

## 未完成内容

## 测试结果

## 风险

## 是否触碰公共契约

## 交接说明
```

`task.md` 建议保存完整任务单，`test-report.md` 汇总验证命令和结果，`handoff.md` 汇总最终交接和人工评审结论。

`contract-notes.md` 建议格式：

```text
# TASK-V3-xxx 契约补充

## 本任务涉及接口

## 本任务涉及 SSE event

## 本任务涉及 RuntimeEvent

## 本任务涉及错误码

## 本任务涉及 DTO / 数据模型 / 表结构 / 状态机

## 参考开源产品与 API / 模型依据

| 参考项目 | 版本/commit | 源码或文档定位 | 实际 API / 模型 / 事件 / 字段 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |

示例：

| AgentScope Java | 本地 fork commit `13a71676` | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamableAgent.java` | `stream(List<Msg>, StreamOptions)` | 映射为平台 `RuntimeEvent` 输入来源 | 不直接暴露 AgentScope 原始 `Event` 给前端 | 前端只依赖平台 SSE 契约 |
| AgentScope Runtime Java | 本地 fork commit `7b9032b` | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/AgentHandler.java` | `streamQuery(AgentRequest, Object)` | 作为 `runtime-sandbox` adapter 参考 | 不作为普通 Chat 默认路径 | 高风险执行需要沙箱和权限 gate |

## 与参考实现差异

## 待合并建议

## 是否需要修改主契约

## 待人工确认
```

契约合并规则：

- 子 Agent 先在任务目录的 `contract-notes.md` 写局部契约草案。
- 子 Agent 设计 API、SSE event、RuntimeEvent、错误码、DTO、数据模型、表结构、状态机时，必须参考已有开源产品或本地调研结论，并在 `contract-notes.md` 标注到实际 API、模型、代码接口、官方 endpoint 或 schema。
- 标注粒度至少包括：参考项目、版本或 commit、源码文件路径或官方文档 URL、类名/方法名/接口签名/API endpoint/event name/DTO/schema/table/entity/state enum/field/relation/index、采纳点、不采纳点和原因。
- 对本仓库已有源码，优先引用 `source/fork_source/`、`source/agentscope-demo/` 或历史实现中的具体文件和符号，不只写“参考 AgentScope / Hermes / Open WebUI”。
- 如果只能参考文档而没有源码，必须写明官方文档 URL、API endpoint、请求/响应字段或事件字段。
- 如果没有直接参考实现，必须写明“无直接参考”，并说明自定义设计的原因、风险和测试方式。
- 并行开发期间尽量不直接修改 `03-API-SSE-运行时契约-全局基线.md`。
- 子 Agent 完成后，先由 Review Agent 或主 Agent 复核 `contract-notes.md` 的采纳点、差异点和风险。
- 需要合入主契约的内容，由主 Agent 或 contract owner 统一修改全局基线、小工程 contract slice 和 RTK。
- 合并后必须在 `handoff.md` 记录已采纳、不采纳、仍待确认的契约项。
- `handoff.md` 必须说明哪些契约已合入主契约，哪些仍是任务内局部约定。
- 字段删除、重命名、事件语义变化等破坏性调整必须新增 ADR。

共享文件冲突处理：

- 同一共享文件只能有一个任务 owner；其他任务需要修改时，先写入本任务 `contract-notes.md` 的“待合并建议”。
- 主 Agent 或 Review Agent 统一合并共享文件变更，并在 `handoff.md` 记录采纳、不采纳和原因。
- 发现其他 Agent 或用户已修改同一文件时，不回滚、不覆盖，先阅读差异并调整自己的变更；无法兼容时在交接记录中标记 Blocked。
- 未授权改动进入工作区后，任务交接必须列出文件、改动来源判断、影响范围和下一步处理建议。

## 8. 验收等级

```text
L0 文档/静态检查
L1 单元测试
L2 模块集成测试
L3 API/SSE E2E
L4 Playwright UI E2E
L5 e2e-real 真实链路
```

任务验收规则：

- 每个任务至少声明目标验收等级。
- Chat 主链路目标应达到 L5。
- 管理后台核心流程至少达到 L4。
- 契约任务至少达到 L1，并应包含 DTO / Schema / 状态 / 映射的单元测试或静态契约检查。
- 数据和持久化任务至少达到 L2；如果暴露 API / SSE 行为，必须达到 L3。
- UI 体验任务必须有 Playwright 验证。

## 9. 禁止事项

AI agent 不允许：

- 自动扩大任务范围。
- 静默新增依赖。
- 修改历史 Flyway 迁移语义。
- 读取、输出、提交 `.env` 密钥。
- 在平台侧通过关键词规则绕过 AgentScope 触发工具。
- 自研 ReAct 循环替代 AgentScope。
- 跳过权限、审计、Capability gate。
- 用 mock 测试冒充真实链路完成。
- 不说明原因就跳过失败测试。
- 随手格式化大量无关文件。

## 10. 契约冻结规则

以下内容变更必须先说明影响范围：

- API DTO 字段删除、重命名、语义变化。
- SSE event 类型、payload 变化。
- RuntimeEvent 字段变化。
- Repository port 变化。
- 数据表主键、状态枚举、唯一约束变化。
- 权限和审计模型变化。

重大变更必须新增 ADR。

并行开发中的契约细化应优先写入对应任务目录的 `contract-notes.md`。只有跨模块、跨任务、需要长期稳定的内容，才合并回主契约文档。

## 11. 真实 E2E 和 testRunId

所有 E2E 必须使用 `testRunId` 隔离数据。

AI agent 不应使用本地已有脏数据证明功能完成。

Playwright UI E2E 必须真实操作页面：

- 点击。
- 输入。
- 等待 SSE 输出。
- 断言页面文本、状态和错误提示。

API E2E 不能替代 UI E2E。

## 12. Review Agent 检查清单

Review agent 优先检查：

- 是否超出 owned files。
- 是否破坏公共契约。
- 是否绕过 AgentScope。
- 是否绕过权限和审计。
- 是否误用 MyBatis 处理 SSE 高频写入。
- 是否修改历史迁移。
- 是否泄露密钥。
- 是否缺少真实链路测试。
- 是否缺少失败状态和取消状态处理。

## 13. 完成定义

一个任务只有同时满足以下条件才算完成：

- 功能达到任务目标。
- 未扩大范围。
- 验证命令已执行并汇报。
- 目标验收等级达到。
- 风险和未完成项已说明。
- 文档或契约同步更新。
- `工程迭代/<小工程>/ai-tasks/poc-*/TASK-V3-xxx-*/` 或 `工程迭代/<小工程>/ai-tasks/cross-cutting/TASK-V3-xxx-*/` 中的任务交接记录已更新，或明确说明本任务不需要保留过程文档。
- 不破坏既有测试和架构规则。
