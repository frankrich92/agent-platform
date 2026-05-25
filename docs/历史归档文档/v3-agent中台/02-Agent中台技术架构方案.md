# Agent 中台技术架构全局基线

## 1. 定位

本文是 V3 Agent 中台的全局技术架构候选基线，不等同于任一小工程的完整实现范围。

V3 后续采用小工程迭代：

- 本文维护长期架构不变量、目标模块地图、依赖方向和候选链路。
- `08-小工程切片与验收门禁.md` 维护小工程拆分和阶段门禁。
- `工程迭代/<小工程>/03-工程技术设计.md` 是该小工程的当前实现权威。
- AI 任务单的 owned modules / owned files 决定每次开发允许触碰的真实边界。

当全局基线和小工程实现细节不一致时：

- 当前小工程只按自己的 contract slice、技术设计和任务单验收。
- 若小工程需要改变公共架构不变量，必须回写本文、RTK、相关 ADR 或 `contract-notes.md`。
- 未进入当前小工程的模块和链路不阻塞当前实现、评审和验收。

## 2. 架构不变量

V3 架构必须持续满足：

- 普通 Chat 默认复用 `agentscope-java` 的 `ReActAgent`，不自研 ReAct 循环。
- MCP、Skills、低风险 Tool 优先通过 AgentScope 的 Toolkit、SkillBox、MCP、Memory、Session、StreamOptions 挂载。
- 平台不自研 Agent 编排、工具调用循环、MCP 调用执行引擎或流式事件引擎。
- Tool / MCP / Skill 调用必须来自 AgentScope runtime 事件，不允许平台侧通过关键词绕过 AgentScope 直接触发。
- `platform-capability` 必须在 runtime 执行前生成经过用户授权、Agent 授权、Capability 审核和风险过滤的 `RuntimeCapabilityPlan`。
- `RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent`、`RuntimeResult` 是 `runtime-spi` 的 Java 契约；`agent-api` 只承载 HTTP / SSE DTO 和公开错误结构。
- 是否进入 sandbox 由本次执行动作的系统级风险决定，不由通用配置 Agent / 外部复杂 Agent 类型决定。
- `agentscope-runtime-java` 用于 Shell、Python、Browser 自动化、文件写入、系统命令等系统级高风险沙箱执行。
- 真实业务副作用动作不进入 V3 当前范围，后续作为独立优化项设计。
- 主对话模型不强制要求多模态；独立图片、文件内嵌图片、扫描页等图片型输入通过受控 OCR / input-preprocessor 生成派生上下文后再进入运行时。
- 普通 CRUD 使用 MyBatis-Plus；SSE 高频 checkpoint / final flush 使用 R2DBC。
- API/SSE/RuntimeEvent 由平台契约稳定承载，前端不直接依赖 AgentScope 原始事件。
- Run/Span/Audit、traceId、错误码和脱敏策略必须贯穿 API、SSE、runtime 和持久化链路。

已接受 ADR：

| ADR | 决策 | 影响范围 |
| --- | --- | --- |
| `ADR-0001` | 普通 Chat 默认使用 AgentScope Java 执行底座 | `runtime-agentscope`、RuntimeEvent、SSE 映射 |
| `ADR-0002` | 普通 CRUD 使用 MyBatis-Plus，SSE checkpoint / final flush 使用 R2DBC | `agent-repo`、`platform-stream`、历史查询 |
| `ADR-0003` | JDK 21、Spring Boot 4.0.6、Maven 4.x RC 工具链基线 | `source/v3-agent-platform`、POC-1 Maven 骨架、architecture test |

## 3. 小工程架构使用方式

每个小工程启动时按以下顺序收敛架构：

1. 从本文和全局契约基线读取长期方向。
2. 从 `08-小工程切片与验收门禁.md` 确认小工程拆分原则、并行开发门禁和已启动小工程索引。
3. 在 `工程迭代/<小工程>/02-工程契约切片.md` 冻结本工程 API、SSE、RuntimeEvent、数据、权限和错误码。
4. 在 `工程迭代/<小工程>/01-工程范围与验收目标.md` 和 `03-工程技术设计.md` 明确本工程范围、暂缓范围、激活模块、主链路和前端范围。
5. AI 任务单只授权本工程当前阶段需要的 owned modules / owned files。
6. 任何跨小工程公共契约或架构变化必须回写全局基线和 RTK。

已启动小工程目录入口维护在 `工程迭代/README.md`；`08-小工程切片与验收门禁.md` 只保留轻量索引。本文不维护小工程索引，也不记录任一小工程的激活模块、主链路、页面范围或暂缓项。

## 4. 目标模块地图

V3 重构代码不直接在旧 `source/htam-agent-platform` 和 `source/htam-agent-platform-ui` 上继续堆功能。建议新增以下两个目录作为 V3 本地代码开发实际根目录：

```text
source/v3-agent-platform
source/v3-agent-platform-ui
```

旧目录定位：

- `source/htam-agent-platform`：历史实现参考，除非任务单明确要求迁移，不作为 V3 新代码开发根目录。
- `source/htam-agent-platform-ui`：历史前端参考，除非任务单明确要求迁移，不作为 V3 新前端开发根目录。
- `source/agentscope-demo` 和 `source/fork_source`：AgentScope 能力调研和适配参考。

模块定位标记：

```text
[Core] 全局基础边界或长期核心能力，是否实现仍以小工程任务单为准
[Ext]  后续能力候选，进入对应小工程前不作为实现或验收范围
[Off]  占位，默认 disabled
```

后端目标地图：

```text
source/v3-agent-platform
├── agent-domain                         [Core]
├── agent-api                            [Core]
├── agent-biz
│   ├── biz-agent                        [Core]
│   ├── biz-session                      [Core]
│   ├── biz-chat                         [Core]
│   ├── biz-memory                       [Ext]
│   ├── biz-execution                    [Core]
│   ├── biz-task                         [Ext]
│   └── biz-workflow                     [Ext]
├── agent-admin
│   ├── admin-iam                        [Ext]
│   ├── admin-agent                      [Ext]
│   ├── admin-task                       [Ext]
│   ├── admin-workflow                   [Ext]
│   ├── admin-capability                 [Ext]
│   ├── admin-provider                   [Ext]
│   ├── admin-system                     [Ext]
│   └── admin-audit                      [Ext]
├── agent-platform
│   ├── platform-capability              [Core]
│   ├── platform-input                   [Ext]
│   ├── platform-provider                [Core]
│   ├── platform-stream                  [Core]
│   ├── platform-security                [Core]
│   ├── platform-observability           [Core]
│   └── platform-orchestration           [Ext]
├── agent-runtimes
│   ├── runtime-spi                      [Core]
│   ├── runtime-agentscope               [Core]
│   ├── runtime-sandbox                  [Ext]
│   └── runtime-hermes                   [Off]
├── agent-repo
│   ├── repo-spi                         [Core]
│   ├── repo-mybatis                     [Core]
│   └── repo-r2dbc-stream                [Core]
└── agent-boot                           [Core]
```

前端目标地图：

```text
source/v3-agent-platform-ui
├── src
│   ├── views                            [Core/Ext]
│   ├── components                       [Core/Ext]
│   ├── stores                           [Core/Ext]
│   ├── api                              [Core]
│   ├── types                            [Core]
│   └── styles                           [Core]
├── tests
│   ├── unit                             [Ext]
│   └── e2e                              [Core]
└── package.json                         [Core]
```

小工程可以只创建或实现自身需要的模块，但不得破坏目标地图中的依赖方向。

## 5. 后端模块职责

后端模块职责是全局边界，不代表任一小工程必须一次性实现全部能力。具体激活模块、owned modules 和 owned files 以任务所属小工程技术设计和任务单为准。

`agent-domain`：

- Agent、Session、Message、Attachment、DerivedContext、Run、Span、Memory、Capability、Workflow、Task 等领域对象。
- 值对象、枚举、状态机、风险等级、执行模式。
- 不依赖 Spring、Web、MyBatis、R2DBC、AgentScope。

`agent-api`：

- HTTP request / response DTO。
- SSE Event Envelope。
- 错误码、分页结构、前端公开事件 DTO。
- 不定义 `RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent`、`RuntimeResult` 的 runtime-spi Java 类型。
- 不放 Controller 和业务逻辑。

`agent-biz`：

- 普通用户侧 Agent 产品能力。
- 不直接访问 Mapper、R2DBC Client、AgentScope 实现类。
- 子模块是否启用由小工程 contract slice 决定。

`agent-biz` 子模块：

- `biz-agent`：用户侧 Agent 库、详情、默认 Agent、用户可见范围过滤、Agent 与模型 / 能力 / 记忆策略的业务聚合。
- `biz-session`：会话创建、重命名、删除、收藏、搜索、分页、归属校验、上下文读取和并发状态查询。
- `biz-chat`：发送消息、停止、重新生成、编辑后重发、组装 Chat 请求上下文、调用 capability plan、调用 runtime、协调 stream / run / message 状态。
- `biz-execution`：Run / Span 查询、执行详情、工具调用过程展示、执行取消、失败原因和耗时统计。
- `biz-memory`：用户记忆 CRUD、Agent 记忆策略、记忆启用 / 禁用 / 清理 / 检索入口。
- `biz-task`：任务列表、任务详情、任务成果查询、从任务模板发起任务、聚合 Chat / Agent / Workflow / External Agent 执行结果。
- `biz-workflow`：用户侧工作流列表、详情、执行、取消、进度查询和运行记录查询。

`agent-admin`：

- 后台管理能力，包括 IAM、Agent、Task、Workflow、Capability、Provider、System、Audit。
- 负责配置管理，不负责运行时决策。
- 完整后台只有进入对应小工程 contract slice 后才成为实现范围。

`agent-admin` 子模块：

- `admin-iam`：用户、角色、权限、用户-角色绑定、用户可用 Agent 范围配置。
- `admin-agent`：Agent 创建、复制、编辑、审核、发布、下架、通用 Agent 配置、外部复杂 Agent 登记和治理、模型 / 能力包 / 记忆策略 / 标签 / 可见范围配置。
- `admin-task`：任务模板配置、标签、输入字段、输出类型、推荐 Agent / Workflow 绑定、发布、下架、授权。
- `admin-workflow`：工作流定义、版本、节点、输入输出映射、发布、下架、授权。
- `admin-capability`：MCP Server、Skills、Tool 注册与审核、风险等级、启停、Agent 能力白名单。
- `admin-provider`：模型供应商、模型列表、默认模型、API base URL、密钥引用。
- `admin-system`：系统参数、字典、菜单、功能开关。
- `admin-audit`：登录日志、操作日志、Agent 执行审计、工具调用审计。

`agent-platform`：

- 可复用平台基础能力。
- 包括 Capability gate、输入预处理、Provider 解析、SSE stream、Security、Observability、Orchestration。
- 不反向依赖 `agent-biz` 或 `agent-admin`。

`agent-platform` 子模块：

- `platform-capability`：生成 `RuntimeCapabilityPlan`，完成能力授权、风险过滤、runtime 路由决策和 disabled reason 汇总。
- `platform-input`：后续承载附件归一化、文件内容抽取、内嵌图片定位、OCR / vision preprocessor 适配、派生上下文组装和输入处理审计；进入对应小工程前只作为候选模块。
- `platform-provider`：解析模型供应商、模型、密钥引用和 endpoint 引用，不向业务层暴露密钥明文。
- `platform-stream`：SSE envelope、sequence、checkpoint、final flush 和断线恢复。
- `platform-security`：用户上下文、权限、可见范围、敏感字段脱敏和安全策略。
- `platform-observability`：Run / Span / Audit、traceId、错误观测和指标。
- `platform-orchestration`：后续工作流、任务编排等跨 Agent 协调能力。

`agent-runtimes`：

- Agent 执行适配层。
- `runtime-spi` 定义 Runtime 端口、`RuntimeRequest`、`RuntimeCapabilityPlan`、`RuntimeEvent`、`RuntimeResult`、cancel、health。
- `runtime-agentscope` 适配 `agentscope-java`。
- `runtime-sandbox` 适配 `agentscope-runtime-java`，用于系统级高风险执行。
- `runtime-hermes` 占位，默认 disabled，仅作为 API、Run、Capability 和 AI Native 方法参考。

`agent-repo`：

- 持久化端口与实现。
- `repo-spi` 定义 repository port。
- `repo-mybatis` 处理普通 CRUD 和历史查询。
- `repo-r2dbc-stream` 处理 SSE checkpoint / final flush。

`agent-boot`：

- Spring Boot 启动入口。
- Controller、Filter、异常处理、SSE endpoint、配置装配、Flyway。
- 不写核心业务逻辑。

## 6. 依赖方向与并行边界

全局依赖方向：

```text
agent-domain
  ↑
agent-api / runtime-spi / repo-spi
  ↑
agent-platform
  ↑
agent-biz / agent-admin
  ↑
agent-boot
```

实现模块依赖：

```text
runtime-agentscope -> runtime-spi
runtime-sandbox    -> runtime-spi
runtime-hermes     -> runtime-spi

repo-mybatis       -> repo-spi
repo-r2dbc-stream  -> repo-spi
```

约束：

- `agent-domain` 不依赖 Spring、Web、MyBatis、R2DBC、AgentScope。
- `agent-biz` 和 `agent-admin` 不直接依赖 Mapper、R2DBC Client、AgentScope 实现类。
- `agent-platform` 提供基础能力，不反向依赖 `agent-biz` 或 `agent-admin`。
- `agent-boot` 只做启动、Controller、Filter、异常处理、SSE endpoint、配置装配、Flyway。
- 小工程任务只能实现任务单授权的模块，不允许顺手启用未进入 contract slice 的模块。
- 多子 Agent 并行开发前必须冻结公共契约，且每个子 Agent 的 owned files 应尽量互斥。
- 禁止循环依赖。

## 7. 小工程技术设计归属

本文不承载具体小工程的实现切片。以下内容只写入对应小工程目录：

- 本工程激活模块和 owned modules。
- 本工程 API / SSE / RuntimeEvent / 数据 / 权限 / 错误码切片。
- 本工程主链路、前端页面、E2E 路径和验证命令。
- 本工程暂缓范围、阶段门禁和真实任务包。

小工程完成后，只有以下变化需要回写本文：

- 新增或调整长期模块地图。
- 改变全局依赖方向、端口边界或 runtime / repo / provider 抽象。
- 改变架构不变量，例如 AgentScope 复用边界、sandbox 判定原则、SSE 持久化原则。
- 把某个小工程验证稳定的能力提升为跨工程公共架构约束。

## 8. 核心链路基线

本节只维护跨小工程复用的链路语义，不记录任一小工程的实施链路。具体链路编排和验收路径写入对应小工程 `03-工程技术设计.md` 与 `04-工程测试策略与真实E2E.md`。

### 8.1 Capability Plan 链路

```text
AgentDefinition
 + UserContext
 + SessionContext
 + RequestedCapability
 -> platform-security 权限校验
 -> platform-capability 风险和环境 gate
 -> RuntimeCapabilityPlan
 -> runtime-agentscope / runtime-sandbox / disabled
```

`RuntimeCapabilityPlan` 至少包含：

```text
agentId
userId
runtimeType
providerModel
allowedMcpServers
allowedSkills
allowedTools
allowedInputPreprocessors
attachmentPolicy
memoryPolicy
riskLevel
auditRequired
disabledReasons
```

约束：

- Controller 不能直接拼装工具。
- Chat 服务不能通过关键词触发工具。
- Runtime 不能自行扩大能力范围。
- `agent_definition.runtime_type` 只表达 Agent 默认运行偏好或运行约束；`RuntimeCapabilityPlan.runtimeType` 才是 `platform-capability` 基于本次请求、用户授权、Agent 授权、能力风险和环境健康做出的最终执行决策。
- 标签只用于检索、筛选、推荐和展示，不参与权限、能力授权或运行时风险事实判断。
- 高危脚本、浏览器自动化、文件写入、系统命令等系统级动作必须进入 `runtime-sandbox` 或 `disabled`。
- 真实业务副作用动作不进入 V3 当前 runtime。
- OCR / input-preprocessor 属于受控 capability，必须出现在 `RuntimeCapabilityPlan` 中；未授权时不得解析图片，也不得把图片原件透传给非多模态主模型。

### 8.2 Runtime 路由链路

```text
biz-chat / biz-execution / biz-workflow
 -> platform-security
 -> platform-capability
 -> RuntimeCapabilityPlan
 -> runtime-agentscope / runtime-sandbox / disabled
 -> RuntimeEvent
 -> platform-stream
 -> Run/Span/Audit
```

约束：

- 普通 Chat 默认进入 `runtime-agentscope`。
- Shell、Python、Browser 自动化、文件写入、系统命令等系统级高风险动作进入 `runtime-sandbox` 或被禁用。
- sandbox 未配置或健康检查失败时返回 runtime unavailable，不允许静默降级到普通 runtime。
- 外部复杂 Agent 只能通过登记、授权、契约校验和 adapter 映射进入平台运行链路。

### 8.3 Stream 持久化链路

```text
RuntimeEvent
 -> platform-stream 分配 sequence
 -> SSE 推送
 -> repo-r2dbc-stream checkpoint
 -> final flush
 -> repo-mybatis 历史查询可见
```

约束：

- 不逐 token 使用 MyBatis 阻塞写入。
- checkpoint 必须幂等。
- final flush 失败必须可观测。
- MyBatis 和 R2DBC 不做跨技术事务，依靠 runId、messageId、sequence、状态机保证一致性。

### 8.4 Run / Span / Audit 链路

```text
API request / RuntimeEvent / ToolEvent
 -> traceId / runId / spanId
 -> platform-observability
 -> agent_run / agent_span / agent_audit_log
 -> Execution detail / Audit query
```

约束：

- API、SSE、RuntimeEvent、Run、Span、Audit 必须能通过 traceId / runId 互相追踪。
- 工具调用、能力拒绝、runtime unavailable、stream final flush 失败必须可观测。
- 审计日志不得写入 API key、token、数据库密码或用户敏感明文。

### 8.5 图片型输入 OCR 预处理链路

以下是后续 `input-ocr` 小工程的候选架构。当前 `chat-minimal` 不实现附件上传、文件内嵌图片抽取、OCR 调用或 `input.ocr.*` 事件，只保留运行时和数据模型的预留边界。

```text
Chat attachment / uploaded file
 -> platform-security 归属、类型和大小校验
 -> platform-input 解析文件结构，定位独立图片、内嵌图片、扫描页或页面渲染区域
 -> platform-capability 生成 input-preprocessor 授权
 -> platform-input 调用 OCR / vision preprocessor adapter
 -> provider model，例如可配置的 glm-ocr
 -> DerivedContext，保留 attachmentId、sourceType、sourceLocator
 -> RuntimeRequest.input.messages / attachments / derivedContexts
 -> runtime-agentscope / runtime-sandbox / disabled
```

约束：

- OCR 是输入预处理能力，不等同于主对话 runtime；主对话模型可以继续使用非多模态模型。
- OCR provider / model 必须通过配置选择，不能在业务代码里硬编码具体厂商或模型。
- 图片原件和包含图片的上传文件只进入附件存储和受控预处理链路；非多模态主模型默认只接收 OCR 后的文本或结构化派生上下文。
- OCR 输出必须带来源附件、来源类型、页码 / sheet / slide / 图片序号 / 区域等定位信息、processor、provider、model、traceId 和状态，便于用户追溯和管理员审计。
- OCR 不可用、未授权、文件类型不支持或大小超限时，必须返回明确错误，不允许静默忽略附件。
- 该链路是后续候选能力，进入独立小工程前不作为当前小工程实现或验收范围。

## 9. 后续候选架构

以下内容是全局候选架构，只有进入对应小工程并完成 contract slice 后才成为实现范围。

Admin 配置：

- 管理 Agent 定义、Provider、Capability、用户授权、可见范围和审计查询。
- 主要涉及 `agent-admin/*`、`platform-security`、`platform-capability`、`platform-provider`。
- 启动前必须冻结 Admin API、权限模型、审计事件和 Playwright 管理端路径。

任务中心：

- 从任务模板、Agent、Chat 或工作流发起任务，并展示状态、过程和成果。
- 主要涉及 `biz-task`、`admin-task`、Task API、TaskRun Schema、任务成果页面。
- 不应反向污染 Chat 最小链路。

顺序工作流：

- 管理员把多个已有 Agent 编排成顺序流程，业务老师查看步骤进度。
- 主要涉及 `biz-workflow`、`admin-workflow`、`platform-orchestration`。
- V3 基线先不做 DAG、并行节点、人工审批和定时触发。

外部复杂 Agent：

- 可以是 OpenAI-compatible API、内部 HTTP/gRPC 服务、后续 runtime adapter 或手工登记的受控外部 Agent。
- 必须登记名称、描述、负责人、输入输出契约、endpoint 引用、密钥引用、风险等级、可见范围、授权范围、超时、重试、幂等策略、审计和观测字段映射。
- 外部复杂 Agent 可作为任务中心来源或工作流节点，但必须先经过授权、契约校验、风险分级和审计。

高风险执行：

- Shell、Python、Browser 自动化、文件写入、系统命令等系统级动作必须进入 `runtime-sandbox` 或被禁用。
- sandbox 未配置或健康检查失败时返回 runtime unavailable，不允许静默降级到普通 runtime。
- V3 当前不执行发邮件、改业务数据、提交审批、触发支付、写 CRM / 工单 / 外部业务系统等真实业务副作用动作。

图片型输入与 OCR：

- 作为后续独立小工程候选，主要涉及 `platform-input`、`platform-capability`、`platform-provider`、`biz-chat`、`agent-api`、`agent-domain` 和附件 / 派生上下文持久化。
- 支持非多模态主模型通过 OCR 派生上下文理解独立图片、扫描页和文件内嵌图片。
- 启动前必须冻结附件 API、文件内容抽取、内嵌图片定位、OCR 预处理 RuntimeEvent / SSE event、错误码、数据表、脱敏策略和 Playwright 验收路径。
- 参考边界：Codex App / Codex CLI 只作为显式图片输入、附件体验和隐私同意边界参考；Dify、Docling、Unstructured 作为文件内图片抽取、OCR 策略、结构化文档组件和来源定位参考。

Hermes Agent：

- 仅作为 API、Run、Capability、AI Native 方法参考。
- `runtime-hermes` 可以保留占位，但默认 disabled，不进入普通 Chat 默认路径。

## 10. 前端架构基线

前端不应把 Agent、Provider、Capability、Memory、Runtime、Run/Span 逻辑写死在单个页面中。

全局建议结构：

```text
views/
  chat/
  agents/
  tasks/
  workflows/
  admin/
components/
  chat/
  agent/
  task/
  execution/
stores/
api/
types/
```

前端必须以 API/SSE 契约为边界：

- 类型由契约生成或手工对齐。
- SSE 事件统一解析。
- Playwright 用稳定 `data-testid` 验证页面真实路径。
- 具体激活页面、导航入口和 E2E 路径只写入对应小工程技术设计和测试策略。
- Admin、Task、Workflow 等页面进入各自小工程后再扩展，不阻塞其他小工程。

## 11. 设计模式

适合本项目的设计模式：

- Ports and Adapters：runtime、repo、provider、external agent 均通过端口隔离实现。
- Strategy：不同 runtime、provider、capability 规划策略可替换。
- Factory / Builder：组装 AgentScope ReActAgent、RuntimeRequest、CapabilityPlan。
- Template Method：Chat、Workflow、Task 执行流程可复用前置校验、执行、观测、收尾。
- State Machine：Session、Run、Message、Task、WorkflowRun 状态流转。
- Specification：Agent 授权、Capability gate、风险判断、可见范围过滤。
- Observer / Event Mapping：RuntimeEvent 转 SSE、Run/Span、Audit。

不要为了模式而加抽象。只有当它能隔离变化、支持并行开发或减少重复时才引入。

## 12. 架构变更规则

- 小工程新增或修改公共模块边界时，必须更新本文的目标模块地图或依赖约束。
- 小工程修改 API、SSE、RuntimeEvent、错误码或数据表时，先更新本工程契约切片和 RTK；只有改变跨工程公共语义、兼容规则或生成规则时，才更新对应全局契约基线。
- 小工程引入新的 runtime、repo、provider、capability 策略时，应先写入任务目录的 `contract-notes.md`，必要时补 ADR。
- 后续小工程不得直接复制其他小工程的实现细节作为通用规则；应从全局基线抽取自己的 contract slice。
- 所有真实代码开发仍必须以 AI 任务单为入口，且验证结果必须回写任务报告。
