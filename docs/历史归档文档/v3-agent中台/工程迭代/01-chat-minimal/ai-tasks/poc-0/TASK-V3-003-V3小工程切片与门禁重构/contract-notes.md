# TASK-V3-003 契约补充

## 本任务涉及接口

本任务不新增接口，只调整契约评审口径：

- `03-API-SSE-运行时契约-全局基线.md` 是全局候选基线。
- `工程迭代/01-chat-minimal/02-工程契约切片.md` 只冻结 Agent 查询、Agent 标签分组查询、Session、Chat、Chat SSE 和 Execution 查询接口。
- Task、Workflow、ExternalAgent、Admin、Memory 完整接口保留为后续小工程候选契约。

## 本任务涉及 SSE event

`chat-minimal` 只冻结：

```text
run.started
message.created
assistant.thinking.delta
assistant.thinking.done
assistant.delta
assistant.done
tool.call.started
tool.call.delta
tool.call.done
tool.call.failed
message.completed
run.completed
run.failed
run.cancelled
heartbeat
```

Task / Workflow SSE event 不进入 `chat-minimal` 验收门禁。

## 本任务涉及 RuntimeEvent

本任务沿用 `TASK-V3-001` 的 RuntimeEvent 映射规则，不新增 RuntimeEvent 类型。`chat-minimal` 只需要覆盖 Chat 相关 RuntimeEvent 到 SSE 的映射。

## 本任务涉及 Runtime 路由

已调整全局候选基线口径：

- 是否进入 sandbox 由本次执行动作的系统级风险决定，不由通用 Agent / 外部复杂 Agent 类型决定。
- Shell、Python、Browser 自动化、文件写入、系统命令等系统级动作必须进入 `runtime-sandbox` 或 `disabled`。
- 发邮件、修改业务数据、提交审批、触发支付、写外部业务系统等真实业务副作用动作不进入 V3 当前范围，后续作为独立优化项设计。

## 本任务涉及图片型输入与 OCR 预留

已调整全局候选基线口径：

- 主对话模型不强制要求多模态；图片型输入可先通过受控 OCR / input-preprocessor 生成派生上下文，再进入 Agent 对话。
- 图片型输入包括独立图片、截图、扫描页，以及 PDF / Word / PPT / Excel 等上传文件中的内嵌图片；文件内嵌图片必须保留原上传文件和页码 / sheet / slide / 图片序号 / 区域等来源定位。
- `glm-ocr` 这类小模型只作为可配置 OCR provider / model 示例，不在契约中硬编码具体厂商或模型。
- OCR / input-preprocessor 必须纳入 Capability gate、授权、审计、限流和失败可观测，不允许前端或 `biz-chat` 直接调用 OCR provider。
- `RuntimeRequest.input` 预留 `attachments` 和 `derivedContexts`，`RuntimeCapabilityPlan` 预留 `allowedInputPreprocessors` 和 `attachmentPolicy`。
- 全局 SSE 事件预留 `input.ocr.started`、`input.ocr.done`、`input.ocr.failed`；全局错误码预留 `ATTACHMENT_UNSUPPORTED`、`ATTACHMENT_TOO_LARGE`、`OCR_UNAVAILABLE`、`OCR_FAILED`。
- 数据模型预留 `agent_message_attachment` 和 `agent_input_derived_context`，并明确图片原件、包含图片的上传文件、文件内嵌图片、OCR 文本和结构化识别结果都按用户输入数据治理。
- `chat-minimal` 当前只接收文本 `message`，`attachments` 必须为空数组或省略，不冻结 `input.ocr.*` 事件和附件专用错误码。
- 后续独立小工程登记为 `EPIC-V3-G input-ocr` / `TASK-V3-050`，启动前再抽取本工程 contract slice。

## 本任务涉及技术架构

已调整 `02-Agent中台技术架构方案.md`：

- 将文档定位从一次性完整技术方案调整为全局技术架构候选基线。
- 明确小工程实现范围以 `工程迭代/<小工程>/03-工程技术设计.md`、contract slice 和任务单为准。
- 已启动小工程目录入口维护在 `工程迭代/README.md`；`08-小工程切片与验收门禁.md` 只保留轻量索引，不展开具体小工程目标、验收命令、任务列表或暂缓范围。
- 将模块标记调整为全局定位：`[Core]` 表示全局基础边界或长期核心能力，`[Ext]` 表示后续能力候选，`[Off]` 表示默认 disabled 占位。
- 明确激活模块、主链路、前端页面、E2E 路径和暂缓项只写入对应小工程技术设计、测试策略和任务包。
- 将 Admin、Task、Workflow、ExternalAgent、高风险 sandbox、Hermes 等完整能力放入后续候选架构，进入对应小工程前不作为实现或验收范围。
- 合并后端模块职责、核心链路、Capability Plan、Runtime 路由、Stream 持久化、Run / Span / Audit 链路。
- 下线独立 `03-后端模块与核心链路.md`，避免模块地图和链路边界出现双重权威。
- 新增 `platform-input` 作为后续输入附件、文件内容抽取、内嵌图片定位、OCR / vision preprocessor 和派生上下文候选模块，进入 `input-ocr` 小工程前不作为实现范围。

## 本任务涉及错误码

`chat-minimal` 只冻结：

```text
AUTH_REQUIRED
PERMISSION_DENIED
AGENT_NOT_FOUND
AGENT_DISABLED
AGENT_NOT_AUTHORIZED
CAPABILITY_NOT_APPROVED
CAPABILITY_NOT_AUTHORIZED
RUNTIME_UNAVAILABLE
RUNTIME_EXECUTION_FAILED
SESSION_NOT_FOUND
SESSION_BUSY
RUN_NOT_FOUND
RUN_CANCELLED
STREAM_RESUME_FAILED
STREAM_CHECKPOINT_FAILED
VALIDATION_FAILED
RATE_LIMITED
INTERNAL_ERROR
```

Task、Workflow、ExternalAgent 相关错误码保留为后续候选契约。

Attachment / OCR 相关错误码保留为后续 `input-ocr` 候选契约，不进入 `chat-minimal` 当前错误码切片。

## 本任务涉及数据对象

`chat-minimal` 只冻结：

```text
sys_user
sys_role
sys_permission
sys_user_role
sys_role_permission
agent_definition
agent_version
agent_tag
agent_definition_tag
agent_user_grant
agent_capability_binding
agent_capability
agent_provider
agent_model
agent_session
agent_message
agent_message_checkpoint
agent_run
agent_span
agent_tool_call
agent_audit_log
```

Task、Workflow、ExternalAgent、MCP、Skill、Tool 配置表保留为后续候选契约。

Attachment / DerivedContext 表保留为后续 `input-ocr` 候选契约，不进入 `chat-minimal` 当前数据切片。

## 本任务涉及 Agent 标签

已调整全局候选基线口径：

- Agent 标签采用维度化模型，包括 `domain`、`scenario`、`task_intent`、`output_form`、`input_context`、`technical_capability`、`audience`。
- 标签只用于检索、筛选、推荐和展示，不作为权限、风险或能力授权的事实来源。
- `agent_tag` 应保存稳定 `tag_group`、`code` 和展示 `name`；`agent_definition_tag` 负责 Agent 与标签绑定。
- `technical_capability` 和 `audience` 只做摘要；实际能力以 `agent_capability_binding` / Capability gate 为准，可见范围以 `visibility_scope` / `agent_user_grant` 为准。
- `chat-minimal` 当前冻结 `GET /api/agents/tags`，并要求 `GET /api/agents` 支持单个 `tagGroup + tagCode` 过滤。
- 标签后台管理、标签推荐算法和多标签复杂组合查询不进入 `chat-minimal`。

## 本任务涉及小工程并行开发

已调整小工程和 AI Native 交付口径：

- 小工程按可验收业务闭环拆分，不按 `runtime-agent`、`frontend-agent`、`repo-agent` 这类技术角色拆分。
- 多 Agent 并行发生在小工程内部的阶段和任务包中，并共同服务同一个 contract slice。
- 并行前必须冻结本小工程 `02-工程契约切片.md` 和 `03-工程技术设计.md`，任务包必须列出子 Agent 分工、owned modules、owned files、禁止修改范围和验证命令。
- 公共 DTO、SPI、SSE envelope、错误码、Flyway 迁移等共享文件必须指定唯一 owner。
- 公共契约只允许 contract owner、主 Agent 或 Review Agent 汇总回写；其他子 Agent 的细化先写入任务目录 `contract-notes.md`。
- `templates/AI任务单-模板.md` 已新增“并行开发计划”，要求任务包显式声明是否启用多子 Agent 并行。
- `工程迭代/01-chat-minimal/05-工程路线图与任务.md` 已补充 POC-2 并行角色建议。
- `08-小工程切片与验收门禁.md` 已收窄为小工程拆分原则、Epic 列表、并行开发门禁和已启动小工程索引，不再维护 `chat-minimal` 的详细验收门禁或暂缓范围。
- `08-小工程切片与验收门禁.md`、RTK 已新增 `EPIC-V3-G input-ocr` / `TASK-V3-050`，作为图片型输入 OCR 的后续独立小工程。

## 参考依据

| 参考来源 | 版本/状态 | 定位 | 实际对象/字段/规则 | 本任务采纳 | 本任务不采纳 | 原因 |
| --- | --- | --- | --- | --- | --- | --- |
| V3 API/SSE/Runtime 主契约 | 当前文档版本 | `docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md` | API 分组、SSE event、RuntimeEvent、错误码 | 采纳为全局候选基线 | 不要求完整契约一次性进入人工验收 | 降低评审范围，先跑通小工程 |
| V3 数据模型 / 安全 / 审计主契约 | 当前文档版本 | `docs/v3-agent中台/04-数据模型与安全审计-全局基线.md` | 表清单、状态、安全审计边界 | 采纳为全局候选基线 | 不要求完整数据模型一次性实现 | 降低 POC-1/POC-2 实现和评审复杂度 |
| V3 小工程门禁 | 当前文档版本 | `docs/v3-agent中台/08-小工程切片与验收门禁.md` | 小工程拆分、阶段门禁、后续工程边界 | 采纳为小工程治理入口 | 不在该文档重复维护具体 API/SSE/数据清单 | 避免双重权威 |
| chat-minimal 工程契约切片 | 当前文档版本 | `docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md` | `EPIC-V3-A chat-minimal` API/SSE/Runtime/数据/错误码切片 | 采纳为首个真实交付范围 | 不让 Admin、Task、Workflow、ExternalAgent 阻塞 chat-minimal | 确保先交付可运行闭环 |
| OCR 输入预留 | 当前文档版本 | `01`、`02`、`04`、`05`、`09`、RTK | 图片型输入、文件内嵌图片定位、OCR 预处理、派生上下文、附件治理 | 采纳为后续 `input-ocr` 小工程预留 | 不进入 `chat-minimal` 当前实现和验收 | 避免当前 POC 被多模态和附件链路扩大 |
| Codex App / Codex CLI | GitHub issue | `https://github.com/openai/codex/issues/2573`、`https://github.com/openai/codex/issues/19143`、`https://github.com/openai/codex/issues/12439` | 图片输入、粘贴图片、多图片、显式 attach / filepath、隐私边界 | 采纳显式输入、用户同意、附件可追踪、禁止任意读取本地文件 | 不作为文档内图片抽取或 OCR 实现依据 | Codex 公开实现重点不在文档解析 |
| Dify Knowledge Pipeline | 官方文档 | `https://docs.dify.ai/en/use-dify/knowledge/knowledge-pipeline/knowledge-pipeline-orchestration` | 文档内图片抽取、图片附着到 chunk、图片大小和数量限制 | 采纳附件与文本来源关系、大小 / 数量限制、独立图片治理 | 不采纳其知识库 chunk 为 V3 Chat 主数据模型 | V3 以 Session / Message / Attachment / DerivedContext 为主模型 |
| Docling | GitHub / 官方站点 | `https://github.com/docling-project/docling`、`https://www.docling.ai/` | 多格式文档解析、扫描页 OCR、结构化文档组件、bounding boxes、图片分类 | 采纳文件结构解析、sourceLocator、page / region 元数据和 OCR adapter 方向 | 不作为默认强依赖 | 后续 `input-ocr` 小工程再决定接入方式 |
| Unstructured | GitHub source | `https://github.com/Unstructured-IO/unstructured/blob/main/unstructured/partition/image.py` | `partition_image`、`partition_pdf_or_image`、`hi_res` / `ocr_only`、图片块抽取 | 采纳 OCR 策略可配置、图片块抽取和 metadata provenance | 不直接复用其 DTO 为平台公共契约 | 避免平台契约绑定第三方内部类型 |

## 是否需要修改主契约

已修改：

- `03-API-SSE-运行时契约-全局基线.md`：调整为全局候选基线，只维护 API/SSE/RuntimeEvent/错误码的公共语义、命名约束和小工程切片维护规则；不维护 `chat-minimal` 的详细冻结清单。
- `04-数据模型与安全审计-全局基线.md`：调整为全局候选基线，只维护数据、安全、审计和标签模型的公共语义及小工程切片维护规则；不维护 `chat-minimal` 的详细冻结表清单。
- `01-产品蓝图与体验目标.md`、`02-Agent中台技术架构方案.md`、`03-API-SSE-运行时契约-全局基线.md`、`04-数据模型与安全审计-全局基线.md`：统一 sandbox 判定口径，真实业务副作用不进入 V3 当前范围。
- `01-产品蓝图与体验目标.md`、`03-API-SSE-运行时契约-全局基线.md`、`04-数据模型与安全审计-全局基线.md`：统一 Agent 维度化标签口径和标签治理边界。
- `03-API-SSE-运行时契约-全局基线.md`：定义 `GET /api/agents/tags` 的公共语义和标签分组约束。
- `工程迭代/01-chat-minimal/02-工程契约切片.md`：将 `GET /api/agents/tags` 纳入 `chat-minimal` 当前冻结接口，并限定为用户侧标签分组查询。
- `02-Agent中台技术架构方案.md`：调整为全局技术架构候选基线，只保留架构不变量、目标模块地图、依赖方向、候选架构和变更规则，不维护具体小工程实现切片。
- `08-小工程切片与验收门禁.md`：补充“小工程按纵向业务闭环拆分、任务包按多 Agent 并行边界拆分”的规则，并新增并行开发就绪门禁。
- `06-AI-Native交付规约.md`：将子 Agent 并行开发限定在已确认的小工程和任务包内，要求任务包保留并行分工表。
- `templates/AI任务单-模板.md`：新增“并行开发计划”，要求声明是否启用多子 Agent 并行及对应 owned files。
- `工程迭代/01-chat-minimal/05-工程路线图与任务.md`：补充 POC-2 多 Agent 并行角色建议。
- `02-Agent中台技术架构方案.md`：合并后端模块职责和核心链路，删除独立 `03-后端模块与核心链路.md`。
- `工程迭代/01-chat-minimal/02-工程契约切片.md`：作为当前小工程的真实评审和实现范围。
- `01-产品蓝图与体验目标.md`、`02-Agent中台技术架构方案.md`、`03-API-SSE-运行时契约-全局基线.md`、`04-数据模型与安全审计-全局基线.md`、`08-小工程切片与验收门禁.md`、RTK、`chat-minimal` 工程文档：预留图片型输入 OCR 能力，并明确当前 `chat-minimal` 不实现。
- `03-API-SSE-运行时契约-全局基线.md`、`02-Agent中台技术架构方案.md`、RTK 和本任务记录：补充 Codex、Dify、Docling、Unstructured 的参考边界，区分输入体验参考和文档解析实现参考。

## 待人工确认

- `chat-minimal` 当前冻结接口是否仍然过大，是否需要把 regenerate / edit-resend 放到后续增强任务。
- `chat-minimal` 当前冻结数据表是否应暂缓 IAM 五张表，改用本地测试用户 stub。
- 后续是否将 `工程迭代/01-chat-minimal/02-工程契约切片.md` 作为 POC-1/POC-2 的最高优先级评审入口。
