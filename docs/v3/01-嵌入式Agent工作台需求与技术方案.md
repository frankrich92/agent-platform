# v3 嵌入式 Agent 工作台需求与技术方案

## 1. 背景与目标

v3 需要新增一个可被其他业务系统嵌入的 Agent 工作台页面。该页面服务于业务系统内的受控 Agent 使用场景：宿主系统通过 iframe 打开本平台页面，平台根据 URL 中的宿主 token 和 appCode 完成身份识别、权限校验和能力下发，用户随后在嵌入页内完成对话、文件查看和本次会话的 Agent / skills / MCP 组装。

本需求属于当前项目 v3 需求迭代，不以历史归档版本作为当前版本来源。历史归档文档只能作为背景参考。

当前高保真原型文件为 `docs/v3/prototypes/embed-workbench-prototype.html`。该原型体现的交互基线如下：

- 左侧是工作空间导航，不再放置对话历史；历史对话统一收敛到右侧对话区。
- 中间是文档工作区，包含打开文档 Tabs、面包屑、文档搜索、文档预览和 AI 插入片段。
- 右侧是 Agent 对话区，包含新对话、历史对话弹窗、Agent/Skills 能力选择、工作空间上下文选择、附件上传和发送。
- 页面支持左侧工作空间隐藏、右侧对话隐藏与恢复，以适配 iframe 内的有限宽度。
- 原型中的“插入到文档”“生成初稿”“上传到工作空间”“逻辑删除”需要在正式实现时明确复用现有工作区/文件能力或提供降级行为。

核心目标：

- 新增嵌入式 Agent 工作台页面，不改造现有 Chat、Communication、管理页。
- 支持 iframe 嵌入，页面自包含运行，无平台主布局导航。
- 通过 `token + appCode` 换取本系统 token，后续请求只使用本系统 token。
- 支持用户在本次会话内临时选择已授权 Agent、skills、MCP。
- 以 `docs/v3/prototypes/embed-workbench-prototype.html` 作为当前高保真交互基线：左侧工作空间、中间文档工作区、右侧 Agent 对话。
- 临时组装结果不保存为个人 Agent/Profile，但必须随会话与每次运行持久化，支持历史回放、审计和问题追踪。
- 允许新增表保存嵌入上下文与能力快照，不修改既有 `chat_session`、`chat_message`、`agent_run` 等表结构。

## 2. 范围

### 2.1 本期包含

- 新增前端路由和页面：`/#/embed/workbench?token=xxx&appCode=xxx`。
- 新增嵌入鉴权接口：`POST /api/auth/embed-token`。
- 新增工作台初始化接口：`GET /api/embed/workbench/bootstrap`。
- 新增三栏嵌入式工作台布局：
  - 左栏：工作空间树、新建目录、目录/文件选择和逻辑删除入口。
  - 中栏：多文档 Tabs、面包屑、文档搜索、文档预览和 AI 插入内容展示。
  - 右栏：Agent 对话框、新对话、历史对话、临时能力选择、工作空间上下文、附件上传。
- 新增能力选择快照持久化表。
- 发送消息时携带当前临时选择、当前文档、打开文档、工作空间上下文和附件引用，由后端二次校验并写入运行快照。
- 历史会话恢复时展示会话记录，并使用最近一次会话级上下文作为 UI 默认值。

### 2.2 本期不包含

- 不把临时组装结果保存为可复用个人 Agent/Profile。
- 不做微前端生命周期接入。
- 不改造现有 Chat、Communication、管理页。
- 不修改既有会话、消息、运行账本表结构。
- 不将外部 token 透传到后续业务接口。
- 不在首期实现完整在线文档协同编辑；原型中的“插入到文档”首期作为 Agent 产物写入或文档片段插入能力落地，具体复用现有工作区/文件能力。

## 3. 页面设计

### 3.1 入口

入口 URL：

```text
/#/embed/workbench?token={externalToken}&appCode={appCode}
```

参数说明：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `token` | 是 | 宿主业务系统签发的用户 token，仅用于初始化换取平台 token。 |
| `appCode` | 是 | 宿主业务系统编码，用于定位宿主鉴权配置、权限映射和嵌入场景配置。 |

### 3.2 初始化状态

页面进入后按以下顺序初始化：

1. 检查 URL 参数是否存在。
2. 调用 `POST /api/auth/embed-token` 换取本系统 token。
3. 保存本系统 token 到前端现有 token 存储。
4. 调用 `GET /api/embed/workbench/bootstrap` 获取可用 Agent、skills、MCP 和默认选择。
5. 若存在默认 Agent，则创建或恢复会话并展示工作台。

初始化失败时展示独立错误态，不跳转现有登录页：

- 缺少 `token` 或 `appCode`。
- `appCode` 未配置。
- 宿主 token 无效或宿主鉴权接口失败。
- 平台用户映射失败。
- 当前用户无可用 Agent。
- 当前用户无嵌入工作台访问权限。

### 3.3 三栏布局

页面整体高度为 iframe 容器高度，宽度为 iframe 容器宽度，不依赖主应用 Layout。

左栏：

- 定位为工作空间导航区，不承载对话历史。
- 顶部展示“工作空间”标题和新建目录入口。
- 主体展示工作空间树，按根空间、目录、文件层级展开。
- 支持选择目录或文件；选择文件后在中栏打开文档。
- 支持根空间下的新建目录和非根节点的逻辑删除确认。
- 左栏可折叠隐藏，隐藏后中栏扩展占用空间。

中栏：

- 定位为文档工作区。
- 顶部展示已打开文档 Tabs，支持切换和关闭。
- 面包屑展示当前文档在工作空间树中的路径。
- 支持搜索当前文档内容。
- 主体展示当前选中文档内容、结构化段落、图示和 AI 插入片段。
- 未选择文件或打开文档为空时展示文档空态。

右栏：

- 定位为 Agent 对话与任务控制区。
- 顶部展示 Agent 品牌、创建新对话、历史对话弹窗和隐藏右栏入口。
- 右栏隐藏后显示“对话”恢复按钮。
- 对话控制区展示当前会话的主 Agent 和 skills/MCP 能力选择。
- 消息区复用现有消息、流式、停止、工具过程展示能力，并支持 Agent 回复的“插入到文档”“生成初稿”等行动按钮。
- 输入区支持文本输入、发送、附件上传，以及选择一个或多个工作空间作为本次提问上下文。
- 历史对话通过右侧弹窗展示，不占用左侧工作空间区域。

### 3.4 工作空间上下文与附件

工作空间规则：

- 工作空间树由 bootstrap 返回或由现有工作区接口加载，前端只展示当前用户在该 `appCode` 下可访问的根空间、目录和文件。
- 用户可以在输入区通过 `+` 打开工作空间选择器，搜索并勾选一个或多个根空间作为当前提问上下文。
- 上传文件时必须落到一个工作空间；默认保存到当前选择的工作空间，未选择时使用当前文档所属根空间或默认根空间。
- 上传完成后文件应在工作空间树中可见，并可作为当前对话附件被 Agent 使用。
- 删除目录或文件首期按逻辑删除处理，不要求物理删除已有存储对象。
- 当前文档、打开文档列表、选中工作空间和附件引用都属于本次会话上下文；发送消息时需要提交给后端，并进入运行快照。

文档写回规则：

- Agent 回复可以生成可插入片段，前端提供“插入到文档”操作。
- 插入结果应以工作区文档片段、Agent 产物或等价结构持久化，避免只存在浏览器内存。
- 若目标文件类型暂不支持写回，前端应降级为“生成草稿/保存为 Agent 产物”。

### 3.5 临时能力选择

能力选择规则：

- 用户必须选择一个主 Agent。
- skills 和 MCP 只能从 bootstrap 返回的授权清单中选择；高保真原型当前显式展示 Agent 与 Skills，MCP 可作为能力菜单独立展示或合并进 Skills/MCP 能力选择器，但请求字段必须保留。
- 选择结果属于当前会话上下文，可以在后续消息前调整。
- 选择结果不保存为新的 Agent 定义，不进入 Agent/Profile 管理列表。
- 每次发送消息时都提交当前选择。
- 后端必须二次校验前端提交的 Agent、skills、MCP 是否仍然对当前用户和 appCode 可用。

## 4. 鉴权与权限

### 4.1 鉴权流程

```text
宿主系统 iframe
  -> /#/embed/workbench?token=externalToken&appCode=appA
  -> POST /api/auth/embed-token
  -> 平台按 appCode 查宿主鉴权配置
  -> 平台调用宿主用户信息接口
  -> 平台映射本地用户与权限上下文
  -> 返回本系统 LoginResponse
  -> 后续请求使用本系统 Authorization token
```

外部 token 只用于初始化，不在后续业务请求中透传。

### 4.2 appCode 职责

`appCode` 是宿主业务系统配置键，应能定位以下配置：

- 宿主系统名称和编码。
- 宿主用户信息接口地址。
- 宿主鉴权调用方式。
- 外部用户 ID 字段映射。
- 本系统用户映射规则。
- 嵌入工作台可用 Agent、skills、MCP 的权限范围。
- 是否启用该宿主系统的嵌入访问。

### 4.3 平台 token

`POST /api/auth/embed-token` 返回现有登录结构兼容的 `LoginResponse`，包括：

- `accessToken`
- `accessTokenTTL`
- `refreshToken`
- `refreshTokenTTL`
- `userDetail`

后续前端请求继续走现有请求拦截器和 `Authorization` 头。

## 5. 接口契约

### 5.1 换取平台 token

```text
POST /api/auth/embed-token
```

请求：

```json
{
  "appCode": "contract-system",
  "token": "external-token"
}
```

成功响应：

```json
{
  "code": 200,
  "data": {
    "accessToken": "platform-token",
    "accessTokenTTL": 3600000,
    "refreshToken": "platform-refresh-token",
    "refreshTokenTTL": 604800000,
    "userDetail": {
      "id": 10001,
      "username": "external-user-001",
      "name": "张三",
      "email": "zhangsan@example.com"
    }
  },
  "msg": "成功"
}
```

失败场景：

- `EMBED_APP_NOT_CONFIGURED`
- `EMBED_APP_DISABLED`
- `EXTERNAL_TOKEN_INVALID`
- `EXTERNAL_AUTH_UNAVAILABLE`
- `EMBED_USER_MAPPING_FAILED`
- `EMBED_PERMISSION_DENIED`

### 5.2 工作台初始化

```text
GET /api/embed/workbench/bootstrap?appCode=contract-system
```

响应：

```json
{
  "code": 200,
  "data": {
    "appCode": "contract-system",
    "externalUserId": "u-001",
    "defaultAgentId": "1001",
    "agents": [
      {
        "id": "1001",
        "agentCode": "contract-review",
        "name": "合同审核 Agent",
        "description": "用于合同条款审核"
      }
    ],
    "skills": [
      {
        "id": "2001",
        "name": "付款条款识别",
        "description": "识别付款节点和风险"
      }
    ],
    "mcpServers": [
      {
        "id": "3001",
        "name": "合同系统 MCP",
        "description": "读取合同元数据",
        "protocol": "stdio"
      }
    ],
    "workspace": {
      "defaultWorkspaceId": "w-001",
      "roots": [
        {
          "id": "w-001",
          "name": "营销物料审核",
          "type": "folder",
          "childrenLoaded": true
        }
      ]
    },
    "pageConfig": {
      "brandName": "Hermes",
      "allowLeftCollapse": true,
      "allowRightCollapse": true,
      "allowUpload": true,
      "allowDocumentInsert": true,
      "allowLogicalDelete": true
    },
    "lastSelection": {
      "sessionId": "9001",
      "agentId": "1001",
      "selectedSkillIds": ["2001"],
      "selectedMcpIds": ["3001"],
      "currentDocId": "doc-001",
      "openDocIds": ["doc-001"],
      "selectedWorkspaceIds": ["w-001"],
      "attachmentIds": []
    }
  },
  "msg": "成功"
}
```

### 5.3 发送消息扩展字段

前端发送消息时，在现有 AGUI `forwardedProps` 或等价请求扩展字段中携带：

```json
{
  "embedContext": {
    "appCode": "contract-system",
    "externalUserId": "u-001",
    "agentId": "1001",
    "selectedSkillIds": ["2001"],
    "selectedMcpIds": ["3001"],
    "currentDocId": "doc-001",
    "openDocIds": ["doc-001", "doc-002"],
    "selectedWorkspaceIds": ["w-001"],
    "attachmentIds": ["file-001"]
  }
}
```

后端处理规则：

- `agentId` 必须是当前用户在该 `appCode` 下可用的 Agent。
- `selectedSkillIds` 必须全部在当前用户授权清单内。
- `selectedMcpIds` 必须全部在当前用户授权清单内。
- `currentDocId`、`openDocIds`、`selectedWorkspaceIds`、`attachmentIds` 必须全部属于当前用户在该 `appCode` 下可访问的工作空间范围。
- 校验通过后写入能力选择快照，再进入 CapabilityPlan。
- 校验失败时拒绝本次运行，不写入运行快照。

## 6. 数据模型

### 6.1 新增表

新增 Flyway 迁移：

```text
source/agent-platform/agent-boot/boot-app/src/main/resources/db/migration/V7__embed_workbench_context.sql
```

新增表：`embed_conversation_context`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键。 |
| `session_id` | varchar(128) | 会话 ID。 |
| `run_id` | varchar(128) | 运行 ID；会话级当前状态可为空，运行快照必须有值。 |
| `app_code` | varchar(100) | 宿主应用编码。 |
| `external_user_id` | varchar(128) | 宿主系统用户 ID。 |
| `agent_id` | bigint | 本次选择的 Agent ID。 |
| `agent_code` | varchar(100) | 本次选择的 Agent Code。 |
| `selected_skill_ids_json` | text | 本次选择的 skill ID 列表 JSON。 |
| `selected_mcp_ids_json` | text | 本次选择的 MCP ID 列表 JSON。 |
| `current_doc_id` | varchar(128) | 本次运行时中栏当前激活文档 ID。 |
| `open_doc_ids_json` | text | 本次运行时打开的文档 Tabs ID 列表 JSON。 |
| `selected_workspace_ids_json` | text | 本次运行时选择的工作空间上下文 ID 列表 JSON。 |
| `attachment_ids_json` | text | 本次运行时引用的附件 ID 列表 JSON。 |
| `ui_state_json` | text | 会话恢复所需的轻量 UI 状态，例如左/右栏显隐、当前工作空间路径。 |
| `capability_snapshot_json` | text | 后端校验后的能力快照。 |
| `created_by` | bigint | 本系统用户 ID。 |
| `created_at` | timestamp | 创建时间。 |

建议索引：

- `idx_embed_context_session_id(session_id)`
- `idx_embed_context_run_id(run_id)`
- `idx_embed_context_app_user(app_code, external_user_id)`
- `idx_embed_context_created_by(created_by)`

### 6.2 快照语义

- 会话级当前选择：用于恢复 UI 默认选择，可按 `session_id` 查询最近一条记录。
- 运行级不可变快照：每次发送消息生成一条带 `run_id` 的记录，用于审计、回放和排查。
- 快照内容必须覆盖能力选择、当前文档、打开文档、工作空间上下文和附件引用；历史回复回放时以对应 `run_id` 快照为准。
- 同一会话多次调整能力时，不覆盖历史运行快照。
- 不修改 `chat_session`、`chat_message`、`agent_run` 表。

## 7. 后端处理规则

### 7.1 bootstrap 权限过滤

bootstrap 返回数据必须经过以下过滤：

- 宿主应用启用。
- 当前平台用户具备嵌入工作台访问权限。
- Agent 已启用，并允许该 appCode 访问。
- skills 已启用，并允许该 appCode 与当前用户访问。
- MCP 已启用、运行状态可用，并允许该 appCode 与当前用户访问。
- 工作空间、文件和附件只返回当前用户在该 appCode 下可访问的范围。
- 页面配置只返回该宿主应用允许开启的能力，例如上传、文档插入、逻辑删除、左右栏隐藏。

### 7.2 运行前校验

发送消息时后端必须重新校验，而不是信任前端提交：

1. 解析 `embedContext`。
2. 校验当前 token 对应的用户。
3. 校验 `appCode` 仍启用。
4. 校验 Agent、skills、MCP 仍在授权范围内。
5. 校验当前文档、打开文档、工作空间上下文和附件引用仍在授权范围内。
6. 生成能力与上下文快照并写入 `embed_conversation_context`。
7. 使用校验后的能力集合、工作空间上下文和附件引用生成 CapabilityPlan。

### 7.3 历史恢复

- 会话列表仍使用现有会话能力。
- 打开历史会话时，前端读取会话消息。
- 能力选择区、当前文档、打开文档 Tabs、工作空间上下文和附件提示使用该会话最近一次 `embed_conversation_context` 作为默认展示。
- 每条回复的真实运行能力以对应 `run_id` 的快照为准。
- 历史对话入口位于右侧对话区弹窗，不放在左侧工作空间树中。

## 8. 前端实现约束

- 新增页面和路由，不改造现有页面。
- 可以复用现有组件：
  - Chat 消息列表。
  - Chat 输入框。
  - 会话列表或历史对话弹窗。
  - 工作区文件树。
  - 工作区文件预览。
  - 附件上传。
- 新页面应有独立样式文件或局部样式，避免影响现有 Chat 页面。
- 页面应适配 iframe 小宽度场景，支持左栏和右栏显隐；至少保证中栏文档和右侧对话可用。
- 左侧只承载工作空间，不承载新对话和历史对话。
- 中栏需要支持打开文档 Tabs、关闭文档、面包屑、当前文档搜索和空态。
- 右侧需要支持新对话、历史对话弹窗、Agent/skills/MCP 能力选择、工作空间选择器、附件上传和发送。
- 对话回复的“插入到文档”“生成初稿”等操作必须有明确降级策略：目标文件可写时写回，目标不可写时保存为 Agent 产物或放入输入框草稿。
- 现有 `/chat/:agentId` 和 `/communication/:chatKey` 行为不得变化。

## 9. 验收标准

### 9.1 前端验收

- iframe 中打开 `/#/embed/workbench?token=xxx&appCode=xxx` 后能完成初始化。
- 缺少 URL 参数时展示错误态。
- 鉴权失败时展示错误态，不跳转登录页。
- 页面展示左中右三栏。
- 左栏支持工作空间树、新建目录、选择文件、逻辑删除确认，并可折叠隐藏。
- 中栏支持打开文档 Tabs、切换/关闭文档、面包屑、当前文档搜索、选中文件预览和文档空态。
- 右栏支持新对话、历史对话弹窗、隐藏/恢复对话区、对话发送、流式输出、停止生成。
- 能力选择区能选择授权 Agent、skills、MCP。
- 输入区能选择工作空间上下文并展示选择结果。
- 附件上传能选择或拖拽文件，上传后保存到目标工作空间，并在对话附件提示中展示。
- Agent 回复支持插入到文档或生成初稿；不可写目标有明确降级提示。
- 发送消息时携带当前临时选择。
- 发送消息时携带当前文档、打开文档、工作空间上下文和附件引用。
- 打开历史会话时恢复消息记录、最近一次能力选择和最近一次工作台上下文。
- 现有 Chat、Communication、管理页不受影响。

### 9.2 后端验收

- `POST /api/auth/embed-token` 能用宿主 token 换取本系统 token。
- `GET /api/embed/workbench/bootstrap` 只返回当前用户授权能力。
- bootstrap 只返回当前用户在 appCode 下可访问的工作空间、文件和页面能力开关。
- 未授权 Agent、skill、MCP 在发送消息时被拒绝。
- 未授权工作空间、文件或附件在发送消息时被拒绝。
- 授权能力在发送消息时写入 `embed_conversation_context`。
- 当前文档、打开文档、工作空间上下文和附件引用在发送消息时写入 `embed_conversation_context`。
- 每次运行都生成独立 `run_id` 快照。
- 不修改既有会话、消息、运行账本表结构。

### 9.3 集成验收

使用本地 iframe 测试页嵌入工作台，完成以下闭环：

1. 宿主 token 初始化。
2. bootstrap 获取授权能力。
3. 选择 Agent、skills、MCP。
4. 打开工作空间文件，切换和关闭文档 Tabs。
5. 选择工作空间上下文并上传附件。
6. 创建新会话，发送消息并流式展示。
7. 使用 Agent 回复执行插入文档或生成初稿。
8. 从右侧历史弹窗恢复历史会话。
9. 隐藏和恢复左侧工作空间、右侧对话区。
10. 刷新 iframe 后恢复平台 token、会话记录、最近一次能力选择和工作台上下文。
11. 后端可按 `session_id` 和 `run_id` 查询能力与上下文快照。

## 10. 后续扩展

后续可在不改变本期基础契约的前提下扩展：

- 宿主应用配置管理页面。
- appCode 级别的 Agent / skill / MCP 授权配置。
- 嵌入工作台访问审计。
- 能力选择模板。
- 将临时组装结果提交为草稿 Agent/Profile，但必须走独立审核流程。
