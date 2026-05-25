# API、SSE 与运行时契约

## 1. 定位

本文定义 V3 的 HTTP API、SSE envelope、RuntimeEvent 和错误码全局候选基线。后续可由该文档生成 OpenAPI、前端类型和 Mock Server。

冻结状态：本文是全局候选基线，不再要求人工一次性评审完整 V3 产品契约。真实开发只冻结当前小工程 contract slice；首个冻结切片是 `EPIC-V3-A chat-minimal`，详见 `工程迭代/01-chat-minimal/02-工程契约切片.md`。未进入当前小工程的接口、事件和错误码保留为后续候选契约，不能作为 `chat-minimal` 的验收门禁。

契约归属：

- `agent-api`：HTTP DTO、SSE DTO、错误结构、分页结构和前端公开 DTO，不承载 runtime-spi Java 类型。
- `runtime-spi`：`RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent`、`RuntimeResult`、cancel、health。
- `repo-spi`：Repository port 和查询对象。

## 2. 参考项目与采纳边界

本契约不是从零设计，主要参考了以下已有项目和本地调研结论。V3 只采纳适合“企业内部 Agent 中台”的部分，不直接照搬任何一个项目的完整协议。

| 参考对象 | 参考内容 | V3 采纳 | V3 不采纳 |
| --- | --- | --- | --- |
| OpenAI-compatible Chat / Responses API | 通用模型请求结构、流式输出、工具调用事件、对话续接思路。 | 保留 provider 兼容意识；Chat 请求和 SSE 事件命名尽量贴近行业常见语义；外部复杂 Agent 可用 OpenAI-compatible API 接入。 | 不把前端直接绑定到某个模型厂商协议；不把 OpenAI 原始事件直接暴露为平台契约。 |
| Hermes Agent | API Server、Runs API、Capability API、conversation continuation、Run-first 事件流、工具进度 SSE。 | 采纳 Run-first、Capability 抽象、外部 Agent adapter、工具进度事件、运行状态可观测思路。 | 不让前端直接连接 Hermes；不采纳 Hermes Profiles 作为用户体系；不默认开放自动 Skill 创建、自修复和 Cron 长任务。 |
| AgentScope Java | `ReActAgent`、Toolkit、SkillBox、MCP、Memory、Session、StreamOptions、运行事件体系。 | 作为普通 Chat 默认执行底座；平台把 AgentScope stream 事件映射为统一 `RuntimeEvent` 和 SSE envelope。 | 不把 AgentScope 原始事件直接暴露给前端；不在平台侧重写 ReAct 循环。 |
| AgentScope Runtime Java | Agent-as-API、沙箱工具、Shell/Python/Browser、独立 runtime 服务化方向。 | 作为高风险执行和沙箱能力底座；通过 `runtime-sandbox` 接入；失败时返回明确 `RUNTIME_UNAVAILABLE`。 | 不作为普通 Chat 默认路径；不绕过平台权限、审计和 Run/Span。 |
| LobeChat / Open WebUI | 成熟 Chat 产品的会话、模型选择、流式消息、重新生成、编辑重发、错误态等体验。 | 采纳会话/消息/重新生成/编辑重发等用户侧 API 语义和前端 SSE 解析稳定性要求。 | 不直接复用其后端数据模型；不把产品做成单纯消费级 Chat。 |
| OpenClaw / WorkBuddy 类 AI 工作台 | 任务化入口、执行状态、成果交付、高危操作确认、多入口助手体验。 | 采纳任务中心、任务状态、成果视图、执行前授权和高风险能力 gate。 | 不做桌面接管、手机控制、多端 Gateway 和长期无人值守执行。 |
| Codex App / Codex CLI 图片输入公开讨论 | 显式 attach / paste / filepath、图片输入同意边界、多图片输入诉求、不能任意读取本地图片。 | 采纳显式输入、附件可追踪、用户同意和隐私边界；图片必须由用户上传、粘贴或显式引用后才进入处理链路。 | 不把 Codex 当前图片输入体验视为已完成的文档解析方案；不采纳“自动读取本地文件”的模式。 |
| Codex App / Claude Code 类 AI Native 开发工具 | 任务单、owned files、并行 agent、验证命令、真实链路验收、审查闭环。 | 采纳 AI 任务单、L0-L5 验收、owned modules/files、验证命令汇报和 Review Agent 检查。 | 不把业务产品做成代码 Agent；这些规则主要服务平台研发和后续内部 Agent 运营。 |
| Dify Knowledge Pipeline | 文档处理器、文档内图片抽取、图片附着到 chunk、图片数量和大小限制、多模态 embedding 可选路径。 | 采纳“图片作为独立可管理附件并保留与文本 chunk / 来源位置关系”的思路，用于 `parentAttachmentId`、`sourceLocator` 和附件限制。 | 不直接采纳 Dify 的知识库 chunk 模型作为 Chat 主数据模型。 |
| Docling | 多格式文档解析、统一文档表示、扫描页 OCR、阅读顺序、表格 / 公式 / 图片分类、bounding boxes。 | 采纳“文件解析先生成结构化文档组件，再把图片型输入转为可追溯派生上下文”的思路，用于 `sourceType`、`sourceLocator`、page / region 元数据。 | 不把 Docling 作为 V3 默认内置依赖；后续 `input-ocr` 小工程再决定是否通过 adapter / MCP / 外部服务接入。 |
| Unstructured | `partition_image` / `partition_pdf_or_image`、`hi_res` / `ocr_only` 策略、图片 block 抽取、OCR 语言和 layout metadata。 | 采纳“解析策略可配置、图片块可输出为文件或 payload、元素级元数据可追溯”的思路，用于 input-preprocessor 策略和 OCR provenance。 | 不直接复用其元素类型为平台公共 DTO；只作为 adapter 参考。 |

本契约最终服务于平台自身边界：

- 前端只依赖平台 API/SSE，不直接依赖 AgentScope、Hermes 或模型厂商原始协议。
- 平台数据库是会话、消息、任务、Run/Span 和审计主数据源。
- runtime 只负责执行，权限、授权、审计、持久化由平台掌握。
- 外部项目协议只能通过 adapter 映射进平台契约。

本节对应的本地依据：

```text
docs/调研/README.md
docs/调研/AgentScope-Java与AgentScope-Runtime-Java区别.md
docs/调研/AgentScope源码对比Hermes与V1V2开发复杂度评估.md
docs/调研/HermesAgent替代AgentScope执行侧方案对比.md
docs/v2-通用大模型对话/实施计划/v2-第一轮执行步骤.md
docs/v2-通用大模型对话/评审/agentscope源码分析.md
docs/v1-通用大模型对话/评审/
docs/v1-通用大模型对话/调研/
```

### 2.1 参考内容到平台契约的映射

```text
OpenAI-compatible / Hermes stream
 -> 平台 SSE eventType / payload

Hermes Runs API / V2 Run-first 设计
 -> runId、run.started、run.completed、Run/Span 查询

AgentScope StreamOptions / Event
 -> RuntimeEvent，再映射为平台 SSE envelope

AgentScope Toolkit / SkillBox / MCP
 -> RuntimeCapabilityPlan.allowedMcpServers / allowedSkills / allowedTools

OpenClaw / WorkBuddy 任务化体验
 -> taskRunId、TaskRun、TaskResult、任务中心 API

Codex App / Claude Code AI Native 实践
 -> traceId、testRunId、验证命令、owned modules/files，不直接进入业务 API

Codex App / Codex CLI 图片输入体验
 -> 显式附件、粘贴图片、用户同意边界、禁止任意读取本地图片

Dify / Docling / Unstructured 文档处理
 -> 文件内图片抽取、sourceLocator、bounding boxes、OCR 策略、派生上下文
```

### 2.2 API 与模型设计参考依据要求

子 Agent 设计任务内 API、SSE、RuntimeEvent、DTO、数据模型、表结构、状态机或错误码时，不能只停留在“参考某开源产品”的总结层面，必须精确到实际 API、模型、代码接口、数据结构或官方 endpoint。

最低要求：

- 本地已有源码优先引用 `source/fork_source/`、`source/agentscope-demo/` 或历史实现中的具体文件路径。
- 引用 Java/TypeScript/Python 代码时，写明类名、方法名、接口签名、枚举名或字段名。
- 引用 HTTP API 时，写明 endpoint、method、请求字段、响应字段、流式事件字段。
- 引用数据模型时，写明 table / entity / DTO / schema / state enum / index / relation 的实际名称和字段。
- 写明参考项目版本、commit 或“本地 fork 当前版本”。
- 写明本任务采纳、不采纳的具体点和原因。
- 如果没有可采纳的开源参考，必须显式写“无直接参考”，并说明为什么选择自定义设计。
- 禁止只写“参考 AgentScope / Hermes / Open WebUI / Dify”等泛泛表述。

可参考的代码级接口示例：

本地 fork 版本锚点：

```text
agentscope-java: 13a71676
agentscope-runtime-java: 7b9032b
```

| 方向 | 本地源码定位 | 实际接口 |
| --- | --- | --- |
| AgentScope 普通流式执行 | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamableAgent.java` | `stream(List<Msg>, StreamOptions)` |
| AgentScope 流式事件类型 | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/EventType.java` | `EventType.REASONING`、`EventType.TOOL_RESULT`、`EventType.SUMMARY` |
| AgentScope 流式选项 | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/agent/StreamOptions.java` | `StreamOptions.builder().eventTypes(...)` |
| AgentScope 工具注册 | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/tool/Toolkit.java` | `Toolkit` 工具容器和注册能力 |
| AgentScope Skill 加载 | `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/skill/SkillBox.java` | `SkillBox` |
| AgentScope Runtime handler | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/AgentHandler.java` | `streamQuery(AgentRequest, Object)` |
| AgentScope Runtime Java 适配 | `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/agentscope/AgentScopeAgentHandler.java` | `AgentScopeAgentHandler` |
| 沙箱工具 | `source/fork_source/agentscope-runtime-java/agents/agentscope/src/main/java/io/agentscope/runtime/engine/agents/agentscope/tools/ToolkitInit.java` | `RunPythonCodeTool(Sandbox)`、`RunShellCommandTool(Sandbox)` |
| Codex 图片输入体验 | `https://github.com/openai/codex/issues/2573`、`https://github.com/openai/codex/issues/19143`、`https://github.com/openai/codex/issues/12439` | 图片输入、多图片、显式 attach / paste / filepath、隐私同意边界 |
| Dify 文档内图片处理 | `https://docs.dify.ai/en/use-dify/knowledge/knowledge-pipeline/knowledge-pipeline-orchestration` | 文档图片抽取、图片附着到 chunk、图片大小和数量限制 |
| Docling 文档结构化 | `https://github.com/docling-project/docling`、`https://www.docling.ai/` | 多格式解析、扫描页 OCR、结构化文档组件、bounding boxes、图片分类 |
| Unstructured 图片 / PDF 分区 | `https://github.com/Unstructured-IO/unstructured/blob/main/unstructured/partition/image.py` | `partition_image`、`partition_pdf_or_image`、`hi_res` / `ocr_only`、图片块抽取参数 |

## 3. 通用响应

通用字段约定：

- 所有 ID 使用字符串，不在前端假设数字自增。
- 时间字段使用 ISO-8601 字符串，并带时区。
- `traceId` 必须贯穿 HTTP、SSE、Run/Span、Audit。
- 请求失败时 HTTP 状态码和业务 `error.code` 都应有意义。

成功响应：

```json
{
  "success": true,
  "data": {},
  "traceId": "trace-xxx"
}
```

失败响应：

```json
{
  "success": false,
  "error": {
    "code": "AGENT_NOT_FOUND",
    "message": "Agent not found",
    "details": {}
  },
  "traceId": "trace-xxx"
}
```

分页响应：

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "total": 0
}
```

分页约定：

- `page` 从 1 开始。
- `pageSize` 默认 20，最大 100；超过最大值时返回 `VALIDATION_FAILED` 或按服务端最大值截断，具体口径由小工程 contract slice 冻结。
- `total` 表示当前筛选条件下可见数据总数，不包含用户无权访问的数据。
- 高频增长的消息列表、执行事件或审计明细后续可以增加 cursor 分页，但当前公共分页结构仍以 `page` / `pageSize` 为基线。

OpenAPI / TypeScript 类型策略：

- `agent-api` 是 HTTP DTO、SSE DTO、错误结构和分页结构的类型来源。
- POC-1 先用 Java DTO、枚举和契约测试保证类型边界。
- POC-2 前应从 `agent-api` 导出 OpenAPI，或在任务单中明确暂时手工对齐 TypeScript 类型并列入 Review 检查。
- 前端 `types` 不得自行创造与 `agent-api` 冲突的字段语义；如需扩展，先写入本工程 `contract-notes.md`。
- SSE envelope 和 payload 类型必须纳入前端类型检查，不能只靠字符串拼接解析。

## 4. API 分组

用户侧：

```text
GET  /api/agents
GET  /api/agents/{agentId}
GET  /api/agents/tags

POST /api/sessions
GET  /api/sessions
GET  /api/sessions/{sessionId}
PATCH /api/sessions/{sessionId}
DELETE /api/sessions/{sessionId}

POST /api/chat/send
POST /api/chat/{runId}/cancel
POST /api/chat/{messageId}/regenerate
POST /api/chat/{messageId}/edit-resend
GET  /api/chat/{runId}/stream

POST /api/attachments
GET  /api/attachments/{attachmentId}
DELETE /api/attachments/{attachmentId}

GET  /api/tasks
POST /api/tasks
GET  /api/tasks/{taskRunId}
POST /api/tasks/{taskRunId}/cancel
GET  /api/tasks/{taskRunId}/stream

GET  /api/workflows
GET  /api/workflows/{workflowId}
POST /api/workflows/{workflowId}/runs
GET  /api/workflow-runs/{workflowRunId}

GET  /api/executions/{runId}
GET  /api/memories
DELETE /api/memories/{memoryId}
```

管理侧：

```text
GET/POST/PATCH /api/admin/users
GET/POST/PATCH /api/admin/roles
GET/POST/PATCH /api/admin/agents
GET/POST/PATCH /api/admin/external-agents
GET/POST/PATCH /api/admin/task-templates
GET/POST/PATCH /api/admin/workflows
GET/POST/PATCH /api/admin/capabilities
GET/POST/PATCH /api/admin/providers
GET /api/admin/audits
GET /api/admin/system/settings
PATCH /api/admin/system/settings
```

### 4.1 小工程 contract slice 维护规则

本文只维护公共 API 分组和公共语义，不维护任一小工程的详细冻结清单。

小工程当前冻结接口只写入：

```text
工程迭代/<小工程>/02-工程契约切片.md
```

规则：

- 小工程接口清单以工程 contract slice 为实现和评审权威。
- 全局基线只接收跨工程复用的 API 语义、公共请求/响应结构、错误码和兼容规则。
- 后续小工程启动时，应从本节全局候选基线抽取自己的 contract slice。

### 4.2 Agent 标签返回口径

`GET /api/agents/tags` 是 Agent 标签公共语义的一部分。接口应返回按维度分组的用户侧可见标签，而不是一组无结构字符串。

最低返回结构：

```json
{
  "groups": [
    {
      "group": "domain",
      "name": "业务域",
      "tags": [
        {
          "tagId": "tag-contract",
          "code": "contract",
          "name": "合同"
        }
      ]
    }
  ]
}
```

标签分组：

```text
domain
scenario
task_intent
output_form
input_context
technical_capability
audience
```

约束：

- Agent 查询可以按 `tagGroup + tagCode` 过滤。
- 小工程可在自己的 contract slice 中决定是否启用单标签过滤、多标签组合过滤或仅展示标签摘要。
- 标签过滤只影响检索和展示，不替代用户授权、能力授权和运行时风险判定。
- 前端展示可用 `technical_capability` 和 `audience` 做摘要，但实际能力和可见范围必须来自 Agent 详情、授权结果和 Capability gate。

## 5. Chat Send Request

```json
{
  "sessionId": "session-xxx",
  "agentId": "agent-xxx",
  "message": "帮我总结这段材料",
  "attachments": [
    {
      "attachmentId": "att-xxx",
      "type": "image",
      "mimeType": "image/png",
      "sourceType": "uploaded_image",
      "ocrPolicy": "auto"
    }
  ],
  "clientMessageId": "client-msg-xxx",
  "metadata": {}
}
```

响应：

```json
{
  "runId": "run-xxx",
  "sessionId": "session-xxx",
  "messageId": "msg-xxx",
  "streamUrl": "/api/chat/run-xxx/stream",
  "traceId": "trace-xxx"
}
```

兼容规则：

- `streamUrl` 是服务端返回的可直接访问地址，当前等价于 `/api/chat/{runId}/stream`。
- `clientMessageId` 由前端生成，用于发送重试和幂等去重；服务端仍生成权威 `messageId`。
- `attachments` 是后续附件输入预留字段；附件可以是独立图片，也可以是包含图片的文档、表格、演示稿、PDF 或其他文件。未进入附件 / OCR 小工程前，小工程可要求该字段为空数组或直接省略。
- `metadata` 只能保存非敏感扩展字段，不允许放入 API key、token、数据库密码或用户敏感数据。

### 5.1 图片型输入与 OCR 预留语义

图片型输入不要求主对话模型具备多模态能力。平台应先把上传内容作为附件登记，再由已授权的 OCR / input-preprocessor 处理独立图片、截图、扫描页或文件内嵌图片，生成文本或结构化派生上下文。

附件最低字段：

```text
attachmentId
type image|document|table|other
mimeType
sizeBytes
storageRef
sha256
sourceType uploaded_image|uploaded_file|embedded_image|scanned_page|rendered_page
parentAttachmentId
sourceLocator
contentHints
status uploaded|processing|ready|failed|deleted
metadata
```

OCR 策略：

```text
disabled  不执行 OCR，附件仅作为记录或后续能力输入
auto      由平台按 Agent 能力、主模型类型和用户授权决定是否执行 OCR
required  必须完成 OCR，失败则本次 Chat send 返回错误或 run.failed
```

派生上下文最低字段：

```text
derivedContextId
attachmentId
sourceType uploaded_image|embedded_image|scanned_page|rendered_page
sourceLocator
processorType ocr
providerId
modelId
contentText
contentJson
confidence
status
traceId
```

约束：

- OCR / input-preprocessor 必须纳入 Capability gate，不允许前端或 `biz-chat` 直接调用 OCR provider。
- `glm-ocr` 这类模型只作为可配置 provider / model 示例，不作为契约固定值。
- 非多模态主模型默认只接收 `derivedContexts`，不接收图片原件。
- `sourceLocator` 用于定位图片来源，例如 `page`、`sheet`、`slide`、`paragraph`、`imageIndex`、`region` 或 `renderedPage`；文件内嵌图片必须能追溯到原上传文件。
- 图片原件、包含图片的上传文件、文件内嵌图片、OCR 文本和结构化识别结果都按用户输入数据治理，不得进入普通日志或错误响应明文。
- 附件 API、OCR SSE event、附件表和派生上下文表只有进入对应小工程 contract slice 后才成为实现和验收范围。

### 5.2 Chat 运行语义

公共语义：

- 同一 `sessionId` 同一时刻只允许一个 active run；入口层应在 `POST /api/chat/send` 判定并返回 `SESSION_BUSY`，不得等 runtime 进入执行后再冲突。
- `POST /api/chat/{runId}/cancel` 只取消指定 run，不取消同一用户的其他 session 或其他 run。
- `regenerate` 基于被选中的历史消息创建新 run，不修改原消息正文。
- `edit-resend` 基于用户编辑后的消息创建新 run，必须保留原消息与编辑后消息的关联。
- 具体请求体字段、历史消息分叉展示和数据层锁策略由小工程 contract slice 冻结。

## 6. SSE Envelope

所有 SSE 事件使用统一 envelope：

```json
{
  "eventId": "evt-xxx",
  "eventType": "assistant.delta",
  "sequence": 12,
  "runId": "run-xxx",
  "sessionId": "session-xxx",
  "messageId": "msg-xxx",
  "taskRunId": null,
  "workflowRunId": null,
  "spanId": null,
  "traceId": "trace-xxx",
  "timestamp": "2026-05-17T08:00:00+08:00",
  "payload": {}
}
```

SSE 线协议示例：

```text
id: evt-xxx
event: assistant.delta
data: {"eventId":"evt-xxx","eventType":"assistant.delta","sequence":12,"runId":"run-xxx","payload":{}}
```

响应要求：

```text
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive
```

约束：

- `sequence` 在同一个 run 内单调递增。
- `eventId` 可用于断线重连去重。
- 客户端可通过 `Last-Event-ID` 恢复，服务端至少应保证同一 run 内的已持久化事件可去重。
- 同一个 `runId` 下，`eventId`、`sequence`、`eventType`、`timestamp`、`traceId` 是前端和 stream checkpoint 的稳定字段。
- `payload` 按 `eventType` 解释；前端必须忽略未知 payload 字段，服务端不得通过自然语言文本表达机器状态。
- 任务 SSE 必须带 `taskRunId`。
- 工作流 SSE 必须带 `workflowRunId`。
- 前端不得依赖自然语言 message 判断状态。
- 前端遇到未知 `eventType` 应忽略并记录调试日志，不应中断整个流。
- `heartbeat` 的 `payload` 可以为空对象，用于保活，不改变 run、message、task 或 workflow 状态。

### 6.1 heartbeat 与超时

默认建议：

- 服务端在无业务事件输出时每 15 秒发送一次 `heartbeat`。
- 客户端 30 秒未收到任何 SSE event 时，可以判定连接疑似中断并发起恢复。
- heartbeat 间隔和客户端超时可由部署配置调整，但同一环境内前后端必须使用一致配置。
- 长耗时 runtime、sandbox 或外部 Agent 调用期间不得因为无 token 输出而让连接长期无事件。

### 6.2 断线恢复

恢复请求：

```text
GET /api/chat/{runId}/stream
Last-Event-ID: evt-xxx
```

恢复规则：

- 服务端按 `runId + Last-Event-ID` 定位 checkpoint，回放该事件之后已持久化的 SSE event。
- 如果 `Last-Event-ID` 为空，服务端从当前可恢复起点发送运行态事件；对新 run 通常是 `run.started`。
- 如果 run 已经 `completed`、`failed` 或 `cancelled`，服务端应回放 `Last-Event-ID` 之后剩余的已持久化终态事件，然后关闭连接。
- 如果 checkpoint 不存在、事件不属于该 run、事件已过期或无法确认恢复点，返回 `STREAM_RESUME_FAILED`。
- 客户端必须按 `sequence` 或 `eventId` 去重，不能重复渲染同一事件。

## 7. SSE Event 类型

Chat：

```text
run.started
message.created
input.ocr.started
input.ocr.done
input.ocr.failed
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

Chat event payload 最低字段：

```text
run.started: { "status": "running" }
message.created: { "role": "user|assistant|tool", "contentType": "text|multimodal|tool" }
input.ocr.started: { "attachmentId": "...", "sourceType": "uploaded_image|embedded_image|scanned_page|rendered_page", "sourceLocator": {}, "processorType": "ocr" }
input.ocr.done: { "attachmentId": "...", "derivedContextId": "...", "sourceType": "...", "sourceLocator": {}, "resultSummary": "..." }
input.ocr.failed: { "attachmentId": "...", "sourceType": "...", "sourceLocator": {}, "errorCode": "...", "message": "..." }
assistant.thinking.delta: { "delta": "..." }
assistant.thinking.done: { "content": "..." }
assistant.delta: { "delta": "..." }
assistant.done: { "content": "..." }
tool.call.started: { "toolCallId": "...", "toolName": "...", "displayName": "...", "riskLevel": "low|medium|high" }
tool.call.delta: { "toolCallId": "...", "delta": "..." }
tool.call.done: { "toolCallId": "...", "resultSummary": "...", "status": "completed" }
tool.call.failed: { "toolCallId": "...", "errorCode": "...", "message": "..." }
message.completed: { "messageId": "...", "status": "completed" }
run.completed: { "status": "completed", "usage": {} }
run.failed: { "status": "failed", "errorCode": "...", "message": "..." }
run.cancelled: { "status": "cancelled", "reason": "user_cancelled|system_cancelled" }
heartbeat: {}
```

工具事件约束：

- 普通用户侧 payload 只允许出现 `displayName`、`riskLevel`、`resultSummary` 和脱敏后的进度摘要。
- 原始工具参数、MCP 请求、Skill 输入、外部响应和敏感错误详情只能进入 `raw`、Run/Span 或 Audit，并按权限查看。
- `toolName` 使用平台登记的稳定工具名，不使用用户输入或模型自然语言生成的名称。

Task：

```text
task.started
task.step.started
task.step.delta
task.step.completed
task.result.created
task.completed
task.failed
task.cancelled
```

Workflow：

```text
workflow.started
workflow.step.started
workflow.step.completed
workflow.step.failed
workflow.completed
workflow.failed
workflow.cancelled
```

### 7.1 小工程 SSE event 维护规则

本文只维护公共 SSE event 类型和 envelope 语义。小工程当前冻结事件只写入对应工程的 `02-工程契约切片.md`。

Task / Workflow 事件属于全局候选事件，只有进入对应小工程 contract slice 后才成为该工程验收门禁。

## 8. RuntimeRequest

本节定义 runtime-spi 的 Java 契约语义。`agent-api` 可定义面向 HTTP / SSE 的 DTO，但不得把这些 runtime-spi 类型复制成另一套业务语义。

```json
{
  "runId": "run-xxx",
  "sessionId": "session-xxx",
  "agentId": "agent-xxx",
  "userId": "user-xxx",
  "input": {
    "messages": [],
    "attachments": [],
    "derivedContexts": []
  },
  "capabilityPlan": {},
  "memoryContext": {},
  "providerConfig": {},
  "stream": true,
  "metadata": {}
}
```

runtime 只能使用 `capabilityPlan` 内允许的能力。

RuntimeCapabilityPlan 最低字段：

```json
{
  "runtimeType": "agentscope",
  "providerModel": {
    "providerId": "provider-xxx",
    "modelId": "model-xxx"
  },
  "allowedMcpServers": [],
  "allowedSkills": [],
  "allowedTools": [],
  "allowedInputPreprocessors": [],
  "attachmentPolicy": {
    "allowedTypes": [],
    "maxSizeBytes": 0,
    "ocrPolicy": "disabled"
  },
  "memoryPolicy": "enabled",
  "riskLevel": "low",
  "auditRequired": true,
  "disabledReasons": []
}
```

约束：

- `runtimeType` 可取 `agentscope`、`sandbox`、`hermes`、`disabled`。
- `runtimeType` 是 `platform-capability` 对本次执行的最终决策；`agent_definition.runtime_type` 只作为 Agent 默认偏好或约束输入，不能绕过 Capability Gate。
- `runtimeType` 由本次执行动作风险、用户授权、Agent 授权、能力审核、provider 可用性和 runtime 健康状态共同决定，不由通用 Agent / 外部复杂 Agent 类型决定。
- 高危脚本、浏览器自动化、文件写入或系统命令必须由 `platform-capability` 判定为 `sandbox` 或 `disabled`。
- 发邮件、改业务数据、提交审批、触发支付、写外部业务系统等真实业务副作用动作不进入 V3 当前契约，后续作为独立优化项设计。
- `hermes` 当前仅用于占位或 PoC，默认不进入普通 Chat 候选。
- `allowedInputPreprocessors` 用于授权 OCR、文档解析、表格解析等输入预处理能力；未授权时不得执行对应预处理。
- `attachmentPolicy` 用于限制附件类型、大小和 OCR 策略；小工程未启用附件时可固定为 disabled。
- `disabledReasons` 非空时 runtime 不应执行。

## 9. RuntimeEvent

最低字段：

```json
{
  "runtimeEventId": "rt-evt-xxx",
  "type": "assistant.delta",
  "runId": "run-xxx",
  "spanId": "span-xxx",
  "traceId": "trace-xxx",
  "sequence": 12,
  "timestamp": "2026-05-17T08:00:00+08:00",
  "payload": {},
  "raw": {}
}
```

约束：

- `raw` 只用于调试和审计，默认不向普通用户返回。
- runtime 不直接写数据库。
- runtime 不决定用户权限。
- runtime 不自动扩大工具范围。
- RuntimeEvent 与 SSE event 不要求一一对应；`platform-stream` 可以把一个 RuntimeEvent 聚合为一个 SSE event，也可以把一个 RuntimeEvent 拆成多个 SSE event。
- 无论一对一、聚合或拆分，映射后必须保留 `runId`、`spanId`、`sequence`、`traceId` 和可审计的原始 runtime 事件引用。
- `RuntimeEvent.type` 优先使用第 7 节 SSE event 名称；runtime adapter 确实无法判断时可使用 `runtime.raw`，再由 `platform-stream` 映射为稳定 SSE event。

AgentScope Java 到 RuntimeEvent 的最低映射：

| AgentScope `EventType` | AgentScope 字段 | RuntimeEvent / SSE 映射 |
| --- | --- | --- |
| `REASONING` | `Event.isLast=false`、`Msg` 增量内容 | `assistant.thinking.delta` 或 `assistant.delta`，由 adapter 按内容块区分 thinking 和最终回复 |
| `REASONING` | `Event.isLast=true` | `assistant.thinking.done`、`assistant.done` 或 `message.completed` |
| `TOOL_RESULT` | `MsgRole.TOOL`、工具结果内容 | `tool.call.delta`、`tool.call.done` 或 `tool.call.failed` |
| `HINT` | RAG、Memory、Planning 上下文 | 默认不向普通用户展示，可进入 `raw`、Run/Span 或审计 |
| `AGENT_RESULT` | 最终助手结果 | 映射为 `assistant.done` 和 `message.completed`，避免重复输出 |
| `SUMMARY` | 达到迭代上限后的总结 | 映射为 `assistant.delta` / `assistant.done`，并在 payload 标记 `summary=true` |

AgentScope Runtime Java 到 RuntimeEvent 的最低映射：

| Runtime 字段 | 平台映射 |
| --- | --- |
| `AgentRequest.input`、`session_id`、`user_id` | 平台 `RuntimeRequest.input`、`sessionId`、`userId` |
| `Event.sequence_number` | 平台 `RuntimeEvent.sequence`，进入 SSE `sequence` |
| `Event.status=created` | 平台内部可记录为 RuntimeEvent；对前端 SSE 映射为 `run.started`，payload.status 固定为 `running` |
| `Event.status=in_progress` | 保持 run 为 running；如携带内容则映射为对应 `assistant.*` / `tool.call.*` delta |
| `Event.status=completed` | `message.completed`、`assistant.done`、`tool.call.done` 或 `run.completed`，由 `object` 和内容类型决定 |
| `Event.status=failed` | `run.failed` 或 `tool.call.failed` |
| `Event.status=rejected` | `run.failed`，错误码按拒绝原因映射为 `PERMISSION_DENIED`、`CAPABILITY_NOT_AUTHORIZED` 或 `VALIDATION_FAILED` |
| `Event.status=canceled` | `run.cancelled` |
| `Event.error` | 平台错误结构和 `RUNTIME_EXECUTION_FAILED` / `RUNTIME_UNAVAILABLE` 等错误码 |

## 10. Task Schema

TaskTemplate：

```json
{
  "templateId": "tpl-xxx",
  "name": "合同初审",
  "description": "提取关键条款并生成初审意见",
  "tags": ["合同", "初审"],
  "recommendedAgentId": "agent-xxx",
  "recommendedWorkflowId": null,
  "inputSchema": {},
  "outputType": "report",
  "riskNotice": "仅供初筛",
  "status": "enabled"
}
```

TaskRun：

```json
{
  "taskRunId": "task-run-xxx",
  "sourceType": "template",
  "templateId": "tpl-xxx",
  "agentId": "agent-xxx",
  "workflowId": null,
  "sessionId": "session-xxx",
  "runId": "run-xxx",
  "workflowRunId": null,
  "status": "running",
  "resultSummary": null,
  "traceId": "trace-xxx"
}
```

## 11. ExternalAgent Schema

ExternalAgentConfig：

```json
{
  "externalAgentId": "external-agent-xxx",
  "agentId": "agent-xxx",
  "protocol": "openai-compatible",
  "endpointRef": "secret-ref-or-config-ref",
  "inputSchema": {},
  "outputSchema": {},
  "timeoutMs": 60000,
  "retryPolicy": {
    "maxAttempts": 1
  },
  "riskLevel": "medium",
  "status": "disabled"
}
```

约束：

- 外部复杂 Agent 默认 disabled。
- endpoint 和密钥只保存引用，不在 API 响应中返回明文。
- 外部 Agent 的执行结果必须映射为 TaskResult、Run/Span 或等价审计记录。

## 12. 错误码全局候选基线

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
TASK_NOT_FOUND
TASK_CANCELLED
WORKFLOW_NOT_FOUND
WORKFLOW_STEP_FAILED
EXTERNAL_AGENT_UNAVAILABLE
EXTERNAL_AGENT_TIMEOUT
ATTACHMENT_UNSUPPORTED
ATTACHMENT_TOO_LARGE
OCR_UNAVAILABLE
OCR_FAILED
STREAM_RESUME_FAILED
STREAM_CHECKPOINT_FAILED
VALIDATION_FAILED
RATE_LIMITED
INTERNAL_ERROR
```

默认 HTTP 状态码：

| 错误码 | 默认 HTTP 状态码 |
| --- | --- |
| `AUTH_REQUIRED` | 401 |
| `PERMISSION_DENIED` | 403 |
| `AGENT_NOT_FOUND` | 404 |
| `AGENT_DISABLED` | 409 |
| `AGENT_NOT_AUTHORIZED` | 403 |
| `CAPABILITY_NOT_APPROVED` | 403 |
| `CAPABILITY_NOT_AUTHORIZED` | 403 |
| `RUNTIME_UNAVAILABLE` | 503 |
| `RUNTIME_EXECUTION_FAILED` | 500 |
| `SESSION_NOT_FOUND` | 404 |
| `SESSION_BUSY` | 409 |
| `RUN_NOT_FOUND` | 404 |
| `RUN_CANCELLED` | 409 |
| `TASK_NOT_FOUND` | 404 |
| `TASK_CANCELLED` | 409 |
| `WORKFLOW_NOT_FOUND` | 404 |
| `WORKFLOW_STEP_FAILED` | 500 |
| `EXTERNAL_AGENT_UNAVAILABLE` | 503 |
| `EXTERNAL_AGENT_TIMEOUT` | 504 |
| `ATTACHMENT_UNSUPPORTED` | 400 |
| `ATTACHMENT_TOO_LARGE` | 413 |
| `OCR_UNAVAILABLE` | 503 |
| `OCR_FAILED` | 500 |
| `STREAM_RESUME_FAILED` | 400 |
| `STREAM_CHECKPOINT_FAILED` | 500 |
| `VALIDATION_FAILED` | 400 |
| `RATE_LIMITED` | 429 |
| `INTERNAL_ERROR` | 500 |

### 12.1 小工程错误码维护规则

本文只维护公共错误码候选集合和兼容规则。小工程当前冻结错误码只写入对应工程的 `02-工程契约切片.md`。

Task、Workflow、ExternalAgent 相关错误码只有进入对应小工程 contract slice 后才成为该工程验收门禁。

错误码兼容规则：

- 错误码只追加，不删除、不重命名、不改变既有语义。
- `message` 面向用户或管理员展示，必须脱敏；调试细节进入 `details` 并受权限控制。
- 运行时未配置或健康检查失败使用 `RUNTIME_UNAVAILABLE`。
- 运行时开始后执行失败使用 `RUNTIME_EXECUTION_FAILED`。
- 外部 Agent 超时使用 `EXTERNAL_AGENT_TIMEOUT`；不可用、禁用或健康检查失败使用 `EXTERNAL_AGENT_UNAVAILABLE`。
- 附件类型不支持使用 `ATTACHMENT_UNSUPPORTED`；附件大小超限使用 `ATTACHMENT_TOO_LARGE`。
- OCR provider 未配置、禁用或健康检查失败使用 `OCR_UNAVAILABLE`；OCR 执行开始后失败使用 `OCR_FAILED`。
- SSE 断线恢复失败使用 `STREAM_RESUME_FAILED`；checkpoint 写入失败但主流程仍可结束时记录 `STREAM_CHECKPOINT_FAILED` 并进入 Run/Span/Audit。

## 13. 契约冻结规则

- 公共字段删除、重命名、语义变化必须新增 ADR。
- 前后端并行开发前必须冻结 DTO 和 SSE event。
- 本文档是 API/SSE/Runtime 的稳定主契约，不承载每个任务包的临时细节。
- 多子 Agent 并行开发时，任务内接口、SSE、RuntimeEvent、错误码细化先写入 `docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/poc-*/TASK-V3-xxx-*/contract-notes.md` 或 `docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/cross-cutting/TASK-V3-xxx-*/contract-notes.md`。
- 子 Agent 提出接口设计时，必须标注参考的开源产品、版本/commit、源码路径或官方 endpoint、实际类/方法/接口签名/事件/字段、采纳点、不采纳点和原因。
- 任务结束后，由主 Agent 或 Review Agent 统一判断哪些局部契约合并回本文档。
- runtime 新增事件类型必须同步更新本文档和前端解析。
- 错误码只增不随意改语义。
- AI agent 修改契约时必须说明影响的 owned modules 和需要同步的测试。
