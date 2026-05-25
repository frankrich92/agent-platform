# 通用大模型对话 v1 文档评审

**评审模型**: GLM-5.1
**评审日期**: 2026-05-13
**评审范围**: 01-需求文档、02-技术方案、03-原型设计、04-详细设计

---

## 一、总体评价

四份文档从需求到详细设计的递进关系清晰，覆盖了第一版 Web 端通用大模型对话的核心功能。需求边界明确（非首版范围列得充分），技术选型合理（WebFlux + agentscope-java + parts 化存储），原型覆盖了主要页面状态，详细设计对接口、数据、流程描述完整。

以下按问题严重程度分级：**严重**（可能导致返工或上线阻塞）、**中等**（需要补充说明或调整）、**建议**（优化项，可后续迭代）。

---

## 二、跨文档一致性问题

### 2.1 【严重】Spring Boot 版本不一致

- `02-技术方案` 和 `04-详细设计` 均写 **Spring Boot 4.0.6**。
- `agentscope-demo` 实际使用的是 **Spring Boot 4.0.5**。
- `agentscope-spring-boot-starter` 1.0.12 是否已适配 4.0.6 未验证。

**建议**: 明确标注以 4.0.5 起步，待验证 4.0.6 兼容性后再升级；或在详细设计中加注"需验证 agentscope-java starter 与 4.0.6 的兼容性"。

### 2.2 【中等】原型数据模型页 ID 类型与详细设计不一致

- 原型 `page-data` 中所有表 ID 标注为 `BIGINT PK`。
- `04-详细设计` 4.1 节明确使用 `varchar(64)` 字符串 ID，并解释了原因（避免 JS 大整数精度问题）。
- 详细设计 4.1 节有一句"当前原型的数据结构页将 ID 标注为 BIGINT，属于视觉示意"，但这句仅出现在详细设计中，原型本身未标注。

**建议**: 在原型 `page-data` 页补充脚注说明"ID 实际使用 VARCHAR(64)，此处 BIGINT 为视觉示意"，保持两个交付物自洽。

### 2.3 【中等】SSE 流程页与详细设计的事件类型差异

- 原型 `page-sse` 流程图中流式请求路径写的是 `fetch POST /api/chat/message`。
- `04-详细设计` 5.5 节接口路径为 `POST /api/agent/sessions/{sessionId}/messages/stream`。
- 原型未展示 `meta` 事件（详细设计新增的）。

**建议**: 更新原型 SSE 流程页的请求路径和事件类型，使其与详细设计 API 一致。

---

## 三、需求文档 (01) 评审

### 3.1 【建议】排队消息的前端交互规则需要更明确的边界条件

需求文档 2 节提到"大模型响应过程中，用户仍可以继续发送下一条消息；该消息先进入排队区，待当前回复完成后自动进入下一轮对话"，但未明确：

- 用户在排队状态下关闭页面，排队消息是否丢失？按详细设计 7.4 节，排队消息属于前端临时状态，不持久化，这意味着页面刷新后丢失。需求侧应确认这是预期行为。
- 排队消息是否有数量上限？如果用户连续快速发送 20 条消息，是否全部排队？

**建议**: 在需求文档或详细设计中补充排队消息的生命周期说明（刷新丢失是预期行为）和数量上限建议。

### 3.2 【建议】thinking 展示的交互规则缺少"无 thinking"场景

需求文档提到"对话过程中可以展示大模型 thinking 内容"，但未说明：如果模型返回不含 ThinkingBlock 的响应（thinkingBudget 未启用或模型不支持 thinking），前端应如何展示。

**建议**: 补充说明 thinking 为可选内容，无 thinking 时不展示 thinking 折叠区域。

---

## 四、技术方案 (02) 评审

### 4.1 【严重】agentscope-java 的流式事件映射未充分考虑实际 API 行为

技术方案和详细设计中 SSE 事件设计为 `thinking`/`message`/`done`/`error` 四种，并期望在流式过程中能独立区分 ThinkingBlock 和 TextBlock 增量。但参考 `agentscope-demo` 的实际实现：

1. **agentscope-java 的 stream 回调中，ThinkingBlock 和 TextBlock 可能出现在同一个事件中**。`Msg.getContent()` 返回的是 `List<ContentBlock>`，一个事件可能同时包含 ThinkingBlock 和 TextBlock（尤其是 thinking 结束后 text 开始的过渡帧）。
2. **demo 的 SSE 实现实际上没有区分 thinking 和 text**——`MsgUtils.getTextContent()` 将二者拼接后统一作为 `message` 事件发出。

这意味着详细设计中"收到 thinking 增量时追加到助手消息的 thinking part，收到文本增量时追加到 text part"的假定，在实际对接 agentscope-java 时可能需要额外的拆分逻辑。

**建议**:
- 在详细设计中明确说明后端需要对每个流式事件的 `ContentBlock` 列表逐一判断类型，同一个事件可能同时产出 `thinking` 和 `message` 两种 SSE 事件。
- 补充 agentscope-java `StreamOptions.incremental(true)` 时 ThinkingBlock/TextBlock 的累积模式 vs 增量模式的处理说明。demo 中的 `printStreamContent` 方法采用双模式兼容（先检测内容是否以旧内容为前缀），说明增量模式的实际行为可能因模型/版本而异。

### 4.2 【中等】并发控制方案需要考虑 WebFlux 线程模型

技术方案和详细设计建议使用 `ConcurrentHashMap<String, Semaphore>` 做会话内串行化。但项目使用 Spring WebFlux，请求运行在 Netty 事件循环线程上：

- `Semaphore.acquire()` 是阻塞调用，在事件循环线程上阻塞会拖垮整个事件循环。
- 详细设计 4.4 节提到"获取锁失败返回 409"，但 WebFlux 中阻塞等待锁是不合适的。

**建议**:
- 如果使用 Semaphore，必须在 `Schedulers.boundedElastic()` 上执行 acquire/release（demo 的 SSE 实现也用了 `subscribeOn(Schedulers.boundedElastic())`）。
- 或者改用基于 Reactor 的非锁方案，如 `Sinks.Many` 做会话内消息排队，或使用 `AtomicReference<Flux>` 串行化同一会话的流。
- 详细设计应明确锁的获取方式（tryAcquire 非阻塞 + 返回 409，还是 acquire 阻塞 + boundedElastic 调度）。

### 4.3 【中等】agentscope-demo 中每个请求新建 ReActAgent 的模式不宜直接照搬

demo 的 SSE 实现每个请求创建一个新的 `ReActAgent`，通过 `JsonSession` 加载/保存上下文。但详细设计要求：

- 使用 PostgreSQL 持久化消息（而非 JsonSession）。
- 基于 `agent_message` + `agent_message_part` 组装多轮上下文。
- 只使用 `completed` 状态的消息。

这意味着后端需要自行将数据库中的消息转换为 agentscope-java 的 `Msg` 列表。详细设计 6.2 节描述了上下文组装逻辑，但未说明 `Msg` 的构造方式。

**建议**: 补充后端如何将数据库中的消息转换为 `agentscope-java` 的 `Msg` 对象列表的映射规则，尤其是：
- 用户消息: `Msg.builder().role(MsgRole.USER).content(TextBlock.builder().text(content).build()).build()`
- 助手消息: `Msg.builder().role(MsgRole.ASSISTANT).content(TextBlock.builder().text(content).build()).build()`
- 确认 `OpenAIChatModel` 的 chat 接口是否接受纯 `Msg` 列表（而非 `ReActAgent`），以及是否需要 SystemMessage 单独处理。

### 4.4 【建议】MyBatis-Plus 与 WebFlux 的兼容性

MyBatis-Plus 是基于 JDBC 的同步阻塞 ORM，在 WebFlux 应用中直接调用 Mapper 方法会阻塞 Netty 事件循环线程。

**建议**: 详细设计应明确数据库操作需在 `Schedulers.boundedElastic()` 或虚拟线程上执行（项目已启用虚拟线程 `spring.threads.virtual.enabled: true`，可利用此特性），避免在 Reactor 链中直接阻塞调用 Mapper。

---

## 五、原型设计 (03) 评审

### 5.1 【中等】原型缺少"助手消息失败状态"的展示

详细设计 8.2 节列出了"流式响应态"但原型中没有展示助手消息 `status = failed` 时的视觉表现。这是联调中必然遇到的场景。

**建议**: 补充一个助手消息失败状态的页面或标注，展示 `error_message` 的展示方式（如红色错误提示条、重试按钮等）。

### 5.2 【中等】侧边栏会话列表分组规则未在文档中定义

原型侧边栏按"今天"、"昨天"、"过去7天"分组展示会话，但需求文档和详细设计均未定义分组规则：

- 是前端按 `lastMessageAt`/`updatedAt` 本地计算？
- 还是后端返回分组标识？
- 分组的时间基准是什么（服务端时间 vs 客户端本地时间）？

**建议**: 在详细设计前端章节补充会话列表分组规则。

### 5.3 【建议】原型搜索框的处理方式建议更明确

原型侧边栏包含搜索框，详细设计 8.1 节提到"搜索不属于第一版需求，实现时作为禁用或静态占位"。但原型的搜索框看起来是完全可交互的（有 focus 状态样式）。

**建议**: 在原型中给搜索框添加 `disabled` 视觉状态（灰色、placeholder 改为"搜索功能开发中..."），避免实现时产生歧义。

### 5.4 【建议】原型 model-badge 硬编码 "GLM-4" 与实际配置可能不一致

原型多处展示 `GLM-4` 模型标签，但详细设计中默认模型名为 `ark-code-latest`，配置通过环境变量管理。

**建议**: 原型中的 model-badge 改为从后端配置动态读取，或标注为示意值。

---

## 六、详细设计 (04) 评审

### 6.1 【严重】流式过程中 part 内容追加的原子性问题

详细设计 4.3 节和 5.5 节描述"收到 thinking 增量时追加到助手消息的 thinking part"、"收到文本增量时追加到 text part"。第一版采用"同类型内容聚合为一条 part"的策略。

但实际流式过程中，多个增量可能在短时间内到达（尤其是 thinking 阶段 token 产出很快），如果每次增量都 `UPDATE agent_message_part SET content = content + delta`：

- 并发追加可能导致内容丢失（两个事务同时读旧值，各自追加不同 delta，后提交的覆盖前者）。
- 即使加了乐观锁，高频 UPDATE 也会带来数据库压力。

**建议**:
- 方案 A：在应用层使用 `StringBuilder` 累积当前 part 的完整内容，流结束时一次性写入最终内容。流式过程中只追加内存中的缓存，不写数据库。缺点是中途崩溃会丢失未持久化的内容。
- 方案 B：保留流式写入，但使用 `SELECT ... FOR UPDATE` 锁行追加，或使用 PostgreSQL 的 `UPDATE ... SET content = content || :delta`。
- 方案 C：改为每个 chunk 一条 part（详细设计已预留此路径），牺牲查询简洁性换取写入安全。
- 无论选择哪种，应在详细设计中明确流式追加的并发安全策略。

### 6.2 【严重】meta 事件缺少必要的上下文信息

详细设计 5.5 节定义的 `meta` 事件包含 `sessionId`、`userMessageId`、`assistantMessageId`。但前端在收到 `thinking`/`message` 事件时需要知道这些内容属于哪个 part，以便正确更新 UI。

**建议**:
- `meta` 事件中增加 `thinkingPartId` 和 `textPartId`，或至少在首个 `thinking`/`message` 事件的 data 中携带 part ID。
- 或者前端在收到 `meta` 后自行生成本地 part ID，在 `done` 后再与后端同步。

### 6.3 【中等】用户消息 content 长度校验位置不明确

详细设计 5.5 节提到"校验 content 非空，最大长度第一版建议 8000 字符"。但未说明：

- 8000 是字符数还是字节数（中文字符占 3 字节 UTF-8）。
- 超长时返回什么错误码和 HTTP 状态码。
- 校验是在 Filter 层、Controller 层还是 Service 层。

**建议**: 明确长度单位（字符数）、错误响应格式和校验位置。

### 6.4 【中等】会话标题自动生成的时机和并发问题

详细设计 5.5 节步骤 5："如果会话标题为新对话，使用用户第一条消息截断生成标题，最大 30 字符"。但：

- 流式发送接口中，标题更新和用户消息写入在同一个请求中。如果同一会话并发两个请求（虽然前端有排队，但 API 层面应考虑），可能导致标题被第二次请求覆盖。
- 截断规则未说明：是直接取前 30 字符，还是按标点/空格截断？

**建议**:
- 标题更新使用 `UPDATE ... SET title = :newTitle WHERE id = :sessionId AND title = '新对话'` 加条件保护。
- 明确截断规则（直接截断即可，第一版不需要复杂规则）。

### 6.5 【中等】删除会话后 agent_message 和 agent_message_part 的 user_code 查询效率

详细设计 5.3 节"第一版不物理删除 agent_message 和 agent_message_part"。但 `agent_message` 和 `agent_message_part` 都有 `user_code` 字段，而删除的会话 `status = deleted` 不会出现在会话列表中。

如果后续需要后台管理查看已删除会话的消息，需要通过 `session_id` 关联查询。当前索引 `idx_agent_message_session_seq` 包含 `user_code`，对已删除会话的查询需要先查 `agent_session(status = deleted)` 再关联。

**建议**: 当前索引设计可以满足需求，但建议在 `agent_message` 上增加 `idx_agent_message_session_id`（不含 user_code 的简单索引），便于后台管理按 session_id 直接查询。

### 6.6 【建议】错误响应格式与成功响应格式不统一

详细设计 5 节定义的统一错误响应：
```json
{
  "code": "UNAUTHORIZED",
  "message": "...",
  "traceId": "..."
}
```

但成功响应（如新建会话、查询会话列表）没有外层包裹，直接返回业务数据。这种混合风格会导致前端需要根据 HTTP 状态码决定解析方式。

**建议**: 第一版可以保持当前设计，但建议在 API 契约中明确"2xx 返回业务数据，4xx/5xx 返回错误对象"的约定，方便前端统一处理。

### 6.7 【建议】流式接口的客户端断开检测

详细设计未说明：如果客户端在流式响应过程中断开连接（关闭页面、网络中断），后端应如何处理：

- agentscope-java 的模型调用是否会继续运行并消耗 token？
- 助手消息是否应标记为 `failed`？
- 会话锁是否正常释放？

**建议**: 补充客户端断开时的处理策略。WebFlux 中可通过 `Flux.doOnCancel()` 检测断开，取消模型调用并更新消息状态。

### 6.8 【建议】系统提示词应可配置化

详细设计 6.2 节给出了默认系统提示词：
```
You are a helpful AI assistant. Be concise, accurate, and explain reasoning when useful.
```

但该提示词硬编码在后端代码中。第一版虽然不要求前端切换模型，但系统提示词是影响对话质量的关键参数。

**建议**: 将系统提示词提取到 `application.yml` 配置中，支持通过环境变量覆盖。

---

## 七、遗漏与待补充项

| 序号 | 项 | 说明 |
|------|------|------|
| 1 | CORS 配置 | 开发期 Vite proxy 已覆盖，但如果前端需要直连后端（如生产独立部署），需明确 CORS 策略。 |
| 2 | 请求限流 | 详细设计未提及接口限流。同一用户可能短时间内发送大量请求（即使前端有排队，API 层面应保护）。 |
| 3 | 分页 | 查询会话列表和消息列表接口均未定义分页参数。长会话场景下消息可能很多。 |
| 4 | 并发用户测试 | 测试计划中未包含并发场景的压测或基准。 |
| 5 | agentscope-java 版本锁定 | 详细设计未指定 agentscope-java 的版本号，应与 demo 一致锁定为 1.0.12 并在父 POM 中统一管理。 |
| 6 | Flyway 脚本命名规范 | 详细设计提到使用 Flyway，但未定义迁移脚本命名规范（如 `V1__init_tables.sql`）。 |
| 7 | 日志规范 | 未定义关键操作的日志级别和格式（如流式开始/结束、鉴权失败、锁获取失败等）。 |

---

## 八、评审结论

**总体**: 四份文档质量较高，需求边界清晰，技术选型有 demo 验证背书，详细设计粒度足够指导开发。

**必须修复（阻塞开发）**:
1. Spring Boot 版本与 agentscope-java starter 兼容性验证（2.1）。
2. agentscope-java 流式事件中 ThinkingBlock/TextBlock 同帧处理策略（4.1）。
3. 流式 part 内容追加的原子性方案（6.1）。

**建议修复（降低返工风险）**:
1. WebFlux 中 Semaphore 阻塞调度方案（4.2）。
2. MyBatis-Plus 阻塞调用调度方案（4.4）。
3. SSE meta 事件补充 part ID 信息（6.2）。
4. 客户端断开时模型调用取消策略（6.7）。

**可延后至后续迭代**:
- 会话列表分页、请求限流、CORS 策略。
- 搜索框禁用状态、model-badge 动态化等原型细节。

---

## 九、业界同类产品数据模型对比分析

本节对比 OpenCode、OpenAI Codex CLI、ChatGPT、DeepSeek、Claude Code 等产品的会话/消息数据模型，分析其与本项目详细设计的异同，提炼可借鉴之处。

### 9.1 产品概览

| 产品 | 定位 | 存储方式 | 消息内容模型 | thinking 处理 |
|------|------|----------|------------|--------------|
| **OpenCode** | 终端 Agent | SQLite + parts JSON | Message + JSON parts 数组 | `reasoning` part 类型 |
| **OpenAI Codex CLI** | 终端 Agent | JSONL 文件 | 事件流追加写入 | 无独立 reasoning 字段 |
| **ChatGPT** | Web 对话 | 服务端存储 | Message + content.parts 数组 | `content_type: "thinking"` |
| **DeepSeek** | API/对话 | 无持久化规范 | 消息级 `reasoning_content` 字段 | 与 `content` 同级字段，流式互斥输出 |
| **Claude Code** | 终端 Agent | npm 包内置 | 未公开 | 未公开 |

### 9.2 详细对比

#### 9.2.1 OpenCode（Go，SQLite）

**数据模型**: `session` + `message` 两张表，无独立 part 表。

```text
session
  id (string UUID)
  parent_session_id (nullable string)
  title
  message_count
  prompt_tokens / completion_tokens / cost
  summary_message_id (nullable)
  created_at / updated_at (int64 Unix epoch)

message
  id (string UUID)
  session_id
  role
  parts (JSON text — 序列化的 ContentPart 数组)
  model (nullable)
  finished_at (nullable int64)
  created_at / updated_at (int64 Unix epoch)
```

**parts 存储方式**: 所有 part 序列化为一个 JSON 字段，存在 `message.parts` 列中。Part 类型使用 tagged union 模式：

```json
[
  { "type": "reasoning", "data": { ... } },
  { "type": "text",      "data": { ... } },
  { "type": "tool_call", "data": { ... } },
  { "type": "tool_result","data": { ... } },
  { "type": "finish",    "data": { "reason": "stop", "time": 1700000000 } }
]
```

**7 种 part 类型**: `reasoning`、`text`、`image_url`、`binary`、`tool_call`、`tool_result`、`finish`。

**关键设计决策**:
- **无独立 part 表**：parts 作为 JSON 整体存储在 message 行中，update 时整体覆盖写入。
- **`finish` 是一种 part 类型**：流结束时追加 `Finish{Reason, Time}` 作为 part，而非 message 级别的字段。非 Assistant 角色的消息创建时自动追加 `Finish{Reason: "stop"}`。
- **Session 分支**：`parent_session_id` 支持从现有会话创建子会话（用于 auto-compact 压缩续聊、task 子任务等）。标题生成使用独立 session（ID 为 `title-{parentId}`）。
- **硬删除**：`DeleteSession` 直接 `DELETE FROM sessions`，不做软删除。
- **列表过滤**：`ListSessions` 只返回 `parent_session_id IS NULL` 的顶层会话。

**与本项目对比**:

| 方面 | OpenCode | 本项目详细设计 | 差异分析 |
|------|----------|------------|---------|
| part 存储 | JSON 字段，整体读写 | 独立 `agent_message_part` 表，关系型 | 本项目使用关系表更利于 SQL 查询和用户隔离，但写入更复杂 |
| part 类型 | 7 种（含 tool_call/tool_result/finish） | 4 种（text/thinking/finish/error） | 本项目第一版不含工具调用，但预留了 finish/error |
| thinking 命名 | `reasoning` | `thinking` | 语义相同，命名差异不影响实现 |
| 流式写入 | 内存累积 → 完成后整体 UPDATE parts | 逐 chunk 追加 UPDATE content | OpenCode 方案规避了原子性问题（见 6.1 节） |
| session 分支 | `parent_session_id` 已启用 | `parent_session_id` 预留为空 | 第一版不实现分支，字段预留即可 |
| 删除策略 | 硬删除 | 软删除（status=deleted） | 本项目保留审计能力更合理 |
| ID 类型 | string UUID | varchar(64) | 一致 |

**可借鉴**:
1. **parts JSON 整体写入策略**：OpenCode 将流式内容在内存中累积，完成后一次性写入，规避了并发追加的原子性问题。本项目 6.1 节提出的方案 A 与此思路一致，建议采纳。
2. **`finish` 作为 part 类型**：将流结束信息（reason、时间戳）作为 part 而非 message 字段，使得消息状态流转更统一。本项目的 `finish_reason` 在 message 级别，也可以考虑作为 part 保存。
3. **标题生成使用独立 session**：OpenCode 创建 `ID = "title-{parentId}"` 的独立 session 调用 LLM 生成标题。本项目使用首条消息截断，第一版足够简单，但后续可参考此方式提升标题质量。

#### 9.2.2 OpenAI Codex CLI（Rust，JSONL 文件）

**数据模型**: 基于文件的 JSONL 事件流，每个 session 一个 `.jsonl` 文件。

**关键特征**:
- **文件级持久化**：不使用数据库，每个会话是一个 JSONL 文件，每行一个 JSON 事件。
- **事件流模式**：类似 Codex 开源方案的描述，包含 `session_meta`、`user_input`、`model_output`、`tool_execution` 等事件类型。
- **Rust 多 crate 架构**：`thread-store`（线程存储）、`message-history`（消息历史）、`rollout`（会话快照）等模块。
- **Session rollout 文件**：可通过 `--ephemeral` 标记跳过持久化。
- **Memories**：存储在 `~/.codex/memories/`，独立于会话历史。

**与本项目对比**:

| 方面 | Codex CLI | 本项目详细设计 | 差异分析 |
|------|----------|------------|---------|
| 存储介质 | JSONL 文件 | PostgreSQL 关系表 | Codex 是单用户 CLI，文件足够；本项目是多用户 Web，需关系型 DB |
| 事件模型 | 追加式事件流 | 当前状态快照 | 详见技术方案 6 节的 parts 化 vs JSONL 对比 |
| 并发控制 | 单进程无并发 | ConcurrentHashMap + Semaphore | CLI 天然单线程，无需并发控制 |
| 上下文组装 | 从文件重放 | 从 DB 查询 completed 消息 | 本项目方案更适合 Web 多用户场景 |

**可借鉴**:
1. **事件流可选择性保留**：详细设计提到可后续增加 `agent_session_event` 作为 JSONL 事件日志表。Codex 的 rollout 文件模式证明此路径可行，但不适合替代 parts 化存储作为主数据模型。
2. **Ephemeral 模式**：Codex 支持 `--ephemeral` 跳过持久化。本项目暂不需要，但如果后续支持"临时对话不留痕"可参考。

#### 9.2.3 ChatGPT（Web 对话，服务端存储）

**数据模型**: `Conversation` + `Message` + `MessageNode` 树结构，消息内容使用 `content.parts` 数组。

```text
Conversation
  id (UUID)
  title
  create_time / update_time
  mapping: { [nodeId]: MessageNode }   ← 消息树，支持分支
  current_node: nodeId                  ← 当前分支指针

MessageNode
  id
  parent: nodeId
  children: [nodeId]
  message: Message

Message
  id
  author: { role }
  content: {
    content_type: "text" | "thinking" | "code" | ...
    parts: [string | object]            ← 内容数组
  }
  status: "finished_successfully" | "in_progress" | ...
  metadata: { ... }
```

**关键设计决策**:
- **树形消息结构**：`mapping` 字段是一棵以 node 为节点的树，支持消息编辑和重新生成（同一 parent 下多个 children 形成分支）。
- **`content.parts` 数组**：即使是纯文本消息，内容也包裹在 `parts[0]` 中，而非直接用字符串。
- **thinking 独立 content_type**：o1/o3 等推理模型的 thinking 内容使用 `content_type: "thinking"`，与 `text` 类型区分。
- **`current_node` 指针**：指向当前活跃的对话分支，支持用户在不同分支间切换。

**与本项目对比**:

| 方面 | ChatGPT | 本项目详细设计 | 差异分析 |
|------|---------|------------|---------|
| 消息结构 | 树形（支持分支） | 线性（sequence 递增） | 第一版线性足够，`parent_session_id` 预留分支能力 |
| part 存储 | JSON 数组在 message 内 | 独立关系表 | 本项目关系表方案更利于 SQL 查询 |
| thinking 展示 | 独立 content_type | 独立 part_type | 语义一致 |
| 删除 | 无恢复（用户视角） | 软删除（保留审计） | 一致 |
| ID 类型 | UUID | varchar(64) | 一致 |

**可借鉴**:
1. **树形消息结构**：第一版不需要，但 `parent_session_id` 已预留。如果后续需要"基于某条消息重新生成"或"编辑消息后分支"，可参考 ChatGPT 的 mapping 树结构。
2. **parts 始终是数组**：ChatGPT 即使纯文本也用 `parts[0]` 包裹，保证了扩展性。本项目的 `agent_message_part` 表天然支持此模式。

#### 9.2.4 DeepSeek（API 服务）

**数据模型**: 不涉及持久化，定义 API 层的消息格式。

**关键特征**:
- **`reasoning_content` 与 `content` 同级字段**：在消息对象中，`reasoning_content` 和 `content` 是同级的两个字符串字段，不是嵌套在 parts 数组中。
- **流式输出互斥**：在流式响应中，`delta` 对象同一时刻只包含 `reasoning_content` 或 `content` 之一，不会同时出现。推理阶段只输出 `reasoning_content`，推理结束后切换到 `content`。
- **上下文组装规则**：
  - 无工具调用时：历史 `reasoning_content` 不需要传回 API，API 会忽略。
  - 有工具调用时：历史 `reasoning_content` **必须**传回，否则返回 400 错误。
- **thinking 参数**：通过 `thinking: { type: "enabled" }` 和 `reasoning_effort` 控制。

**与本项目对比**:

| 方面 | DeepSeek API | 本项目详细设计 | 差异分析 |
|------|-------------|------------|---------|
| thinking 格式 | 消息级同级字段 | 独立 part 行 | 本项目 parts 化更结构化 |
| 流式互斥性 | reasoning 与 content 不同时出现 | 可能同帧（agentscope-java） | DeepSeek 的互斥输出简化了前端处理，但本项目受限于 agentscope-java 的实际行为 |
| 上下文中 thinking | 无工具调用时忽略 | 不进入上下文 | 一致（详细设计 6.2 节已规定 thinking 不入上下文） |
| thinking 控制 | thinking.type + reasoning_effort | thinkingBudget | 控制方式不同，由底层 SDK 决定 |

**可借鉴**:
1. **流式输出的互斥保证**：DeepSeek 保证同一时刻只输出 reasoning 或 content 之一。本项目后端如果能在处理 agentscope-java 事件时做类似保证（即使底层同帧，也在后端拆分后逐个发送 SSE 事件），将大大简化前端解析逻辑。这与 4.1 节的建议一致。
2. **thinking 不入上下文的明确规则**：DeepSeek 明确了"无工具调用时 reasoning 不需要传回"，本项目的规则更严格（thinking 永远不入上下文），更安全简单。
3. **API 层面的 thinking 控制**：后续如果支持用户切换是否展示 thinking，可参考 DeepSeek 的 `thinking.type` 参数设计。

### 9.3 综合对比：数据模型架构选择

| 架构模式 | 代表产品 | 优点 | 缺点 | 适用场景 |
|----------|---------|------|------|---------|
| **parts JSON 字段** | OpenCode | 写入简单（整体覆盖），无并发问题，扩展灵活 | 不利于 SQL 级别的 part 查询和过滤 | 单用户/嵌入式场景 |
| **parts 独立关系表** | 本项目 | 利于 SQL 查询、用户隔离、分页、管理 | 流式追加写入有原子性问题 | 多用户 Web 场景 |
| **JSONL 事件流** | Codex CLI | 完整回放、审计友好 | 不适合复杂查询和并发 | CLI/Agent 单用户场景 |
| **树形消息映射** | ChatGPT | 支持分支、编辑、重新生成 | 实现复杂度高 | 高级对话产品 |
| **消息级同级字段** | DeepSeek API | 简单直观，前端易处理 | 不支持结构化 part，扩展性差 | 简单 API 接口 |

### 9.4 对本项目详细设计的借鉴建议

#### 9.4.1 【严重→方案推荐】采纳 OpenCode 的 parts JSON 整体写入策略

如评审 6.1 节分析，本项目"同类型内容聚合为一条 part，流式增量追加"的策略存在原子性问题。对比各产品：

- **OpenCode**：parts 序列化为 JSON，流式过程中在内存累积，完成后整体 UPDATE。这是最简单可靠的方案。
- **ChatGPT**：服务端存储，具体写入策略未公开，但 `content.parts` 数组模式暗示也可能是整体写入。
- **DeepSeek**：API 层面不涉及持久化，但流式输出天然互斥，不存在并发追加问题。

**建议**：将 `agent_message_part` 表的流式写入策略改为：

1. 流式过程中，在应用内存中维护当前助手消息的 `thinking` part 和 `text` part 的 `StringBuilder`。
2. 收到 thinking 增量时，追加到内存中的 thinking StringBuilder，并发送 SSE `thinking` 事件。
3. 收到 text 增量时，追加到内存中的 text StringBuilder，并发送 SSE `message` 事件。
4. 流结束时，将完整的 thinking 和 text 内容一次性写入 `agent_message_part` 表（每条 part 一行 INSERT 或 UPDATE）。
5. 更新助手消息状态为 `completed`，更新会话统计。

**优点**：
- 规避并发追加的原子性问题。
- 减少数据库写入次数（从 N 次 UPDATE 变为 1-2 次 INSERT/UPDATE）。
- 与 OpenCode 验证过的策略一致。

**缺点**：
- 中途崩溃时，未完成的助手消息内容丢失。但此时消息状态为 `streaming`，前端可检测并标记为失败，用户重新发送即可。这与其他产品的处理方式一致。

#### 9.4.2 【中等】借鉴 DeepSeek 的流式输出互斥保证

DeepSeek 保证流式输出中 `reasoning_content` 和 `content` 互斥。本项目后端可以在处理 agentscope-java 事件时做类似保证：

- 即使底层 agentscope-java 在同一事件中同时返回 ThinkingBlock 和 TextBlock，后端也按"先发完所有 thinking 事件的 SSE，再发 message 事件的 SSE"的顺序发送。
- 这样前端可以简化处理逻辑：收到 `thinking` 事件追加到 thinking 区域，收到 `message` 事件追加到回复区域，不需要处理同一 SSE 事件中同时包含两种内容的情况。

#### 9.4.3 【建议】借鉴 OpenCode 的 `finish` part 类型

OpenCode 将流结束信息（`reason`、时间戳）作为一种 part 类型保存。本项目的 `finish_reason` 在 `agent_message` 级别。

**优点**（保留当前设计）：
- 当前设计已经足够，`finish_reason` 在 message 级别更直观。
- 独立 part 表中 `finish`/`error` 类型已预留，但第一版不一定需要使用。

**建议**：第一版保持当前设计（`finish_reason` 在 message 级别），如后续需要更精细的回放（如记录每个 tool call 的结束原因），再启用 `part_type = finish`。

#### 9.4.4 【建议】后续可参考 ChatGPT 的树形消息结构

ChatGPT 的 `mapping` 树结构支持消息分支和重新生成，是高级对话产品的标准模式。本项目第一版使用线性 sequence 已足够，`parent_session_id` 预留了分支能力。

**后续演进路径**：
1. 第一版：线性消息流，`parent_session_id` 为空。
2. 第二版（如需支持"重新生成"）：增加 `agent_message.parent_message_id`，形成树形结构，增加 `current_node_id` 指针。
3. 第三版（如需完整分支）：参考 ChatGPT 的 mapping 模式，增加分支切换 UI。

---

## 十、评审结论（更新）

在纳入业界同类产品对比分析后，对原评审结论的补充：

**数据模型方向确认**：本项目选择"parts 独立关系表"的方案，相比 OpenCode 的"parts JSON 字段"更契合多用户 Web 场景，但需要解决流式写入的原子性问题。建议采纳 OpenCode 的"内存累积 + 完成后整体写入"策略（9.4.1 节），这是当前最实用的改进。

**thinking 处理方向确认**：各产品对 thinking 的处理方式差异较大，但核心共识是"thinking 与 text 分开存储/展示"。本项目 parts 化的 `thinking` part 类型设计方向正确。建议后端保证流式输出的 thinking 与 text 事件互斥发送（9.4.2 节），降低前端复杂度。

**原评审结论中"必须修复"项更新**：
1. Spring Boot 版本与 agentscope-java starter 兼容性验证（2.1）——不变。
2. agentscope-java 流式事件中 ThinkingBlock/TextBlock 同帧处理策略（4.1）——补充：后端应保证 thinking 与 text 事件互斥发送（9.4.2 节）。
3. 流式 part 内容追加的原子性方案（6.1）——更新：推荐采纳 OpenCode 的内存累积 + 完成后整体写入策略（9.4.1 节）。
