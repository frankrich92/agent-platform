# Hermes Agent 设计调研

> 调研版本：v1
> 调研模型：DeepSeek-v4-pro
> 调研日期：2026-05-14
> 调研对象：NousResearch Hermes Agent (v0.10+)
> 对照范围：通用大模型对话 01/02/03/04 文档
> 补研说明：本调研在 GPT-5.5 版本基础上，补充架构内层、gstack 集成、内存系统详细分析，以及面向 Java/Spring Boot 技术栈的具体落地建议。

## 1. 背景与定位

Hermes Agent 是 NousResearch 开源的长期运行自主 Agent runtime（Python, ~13,700 行核心编排代码）。其核心差异化能力：

- **三层记忆架构**：冻结快照 Prompt（MEMORY.md） + 情景技能（Skills System） + SQLite FTS5 全文检索（Session Search）
- **闭环学习**：完成任务后自动创建或修补技能文档，跨会话复用经验
- **多平台统一核心**：CLI、Telegram、Discord、Slack、WhatsApp、Signal、ACP 编辑器、Cron 调度器共享同一个 `AIAgent` 类
- **多 Provider 抽象**：支持 18+ provider，三种 API 模式（chat_completions / codex_responses / anthropic_messages），统一收敛到 OpenAI 消息格式
- **gstack 技能系统集成**：Hermes 作为 gstack host，通过工具重写（terminal/patch/delegate_task）适配技能文档；通过 GBrain 实现跨会话持久记忆

当前项目 V1 定位是 Spring Boot + WebFlux + agentscope-java 的 Web 端通用大模型对话。两者的技术栈（Python vs Java）和产品阶段（成熟 Agent runtime vs 基础对话应用）差异很大，但 Hermes 的架构设计原则、数据流模式、可演进性规划思路高度可参考。

## 2. 架构设计原则（可直接引入）

Hermes 文档中总结了五条设计原则，其中三条对当前项目有直接指导意义：

### 2.1 Prompt 稳定性

> "System prompt doesn't change mid-conversation. No cache-breaking mutations except explicit user actions"

**Hermes 做法**：Memory 写入磁盘即时生效，但不更新当前会话的 prompt 快照。新内容在下一次会话启动时才注入。这保护了 Anthropic 的 prompt cache prefix，避免每次对话都支付全量输入费用。

**对我们项目的启示**：
- 当前 V1 对话上下文由消息历史组装，不存在显式的 memory prompt，此问题不直接适用。
- 但后续引入系统 prompt 或附加指令时，需要明确"会话内不变"的约束。
- 如果未来对接 Anthropic 模型并开启 prompt caching，此规则至关重要——频繁修改 system prompt 会在静默中导致费用翻倍。
- **建议**：在 `02-技术方案` 追加一条设计约束："会话启动后，系统级 prompt（包括 memory、用户偏好、项目上下文等）在会话过程中不发生变更。需要更新时仅在下一轮会话中生效。"

### 2.2 平台无关核心

> "One AIAgent class serves CLI, gateway, ACP, batch, and API server. Platform differences live in the entry point, not the agent."

**Hermes 做法**：`run_agent.py` 中的 `AIAgent` 类在所有入口（CLI、Gateway、ACP、Cron、Batch）中行为一致。平台差异化通过 callback 机制注入，而非在核心逻辑中 `if platform = X`。

**对我们项目的启示**：
- 当前 V1 只有 Web 入口，但 Maven 多模块设计（`agent-api` / `agent-core` / `agent-web`）已经体现了入口与核心分离的思路。
- `agent-core` 中的 LLM 适配、会话管理、消息组装等逻辑应保持与 Spring WebFlux 解耦——不依赖 `ServerHttpRequest`、`ServerHttpResponse` 等 Web 层类型。
- **建议**：在 `04-详细设计` 中明确 `agent-core` 不引入 `spring-boot-starter-webflux` 依赖，领域服务通过接口接收上下文，由 `agent-web` 的 Filter/Controller 负责从请求中提取并传入。

### 2.3 松散耦合

> "Optional subsystems (MCP, plugins, memory providers, RL environments) use registry patterns and check_fn gating, not hard dependencies."

**Hermes 做法**：MCP、Honcho、ACP 等可选子系统通过 registry + `check_fn` 门控加载，不存在硬依赖。即使 `hermes-mcp` 未安装，核心对话仍可正常运行。

**对我们项目的启示**：
- 当前 V1 不涉及可选子系统，但 Maven 模块化已经为此打下基础。
- 后续引入工具运行时、向量检索、知识库等模块时，应保持此模式：核心对话不依赖可选模块加载成功。
- **建议**：在 `02-技术方案` 的"非首版范围"后补充说明："后续新增可选模块时，采用 register + availability check 模式，避免硬耦合影响核心对话链路。"

## 3. 三层记忆架构与我们的数据模型演进

这是 Hermes 最具特色的设计，也是与当前项目最直接相关的架构参考。

### 3.1 Hermes 三层记忆概览

| 层级 | 存储 | 加载时机 | 容量 | 用途 |
|------|------|----------|------|------|
| L1 冻结快照 | MEMORY.md / USER.md 文件 | 会话启动时写入 system prompt，会话内不变 | ~1,300 tokens | 环境配置、项目约定、用户偏好 |
| L2 情景记忆 | Skills 目录（Markdown 文件） | 按需 FTS5 搜索后渐进式加载 | 无限制 | 可复用的操作步骤、已知陷阱、验证方法 |
| L3 会话历史 | SQLite state.db (FTS5) | 按需搜索 + LLM 摘要后注入 | 无限制 | 历史对话中的决策、结论、上下文 |

**检索策略对比**：

| 特性 | Hermes (SQLite FTS5) | 典型向量库方案 |
|------|---------------------|---------------|
| 延迟 | ~10ms（10,000+ 文档） | 100ms+（尾部延迟显著） |
| 部署复杂度 | 零（嵌入式 SQLite） | 需要额外服务（pgvector / Pinecone / Milvus） |
| 精确匹配 | 强 | 弱 |
| 语义召回 | 弱（依赖关键词） | 强 |
| 适合场景 | 结构化的步骤/配置/日志 | 非结构化的文档/知识库 |

### 3.2 对我们数据模型的启示

当前项目 `04-详细设计` 中，消息表已预留 `prompt_tokens`、`completion_tokens`、`parent_session_id`、`summary_message_id` 等字段。结合 Hermes 的三层模式，建议为以下演进路径做准备：

**L1 层面——用户/项目上下文表（后续版本）**：

```sql
-- 后续演进参考，V1 不实现
CREATE TABLE user_context (
  id          BIGSERIAL PRIMARY KEY,
  user_code   VARCHAR(64)  NOT NULL,
  context_key VARCHAR(128) NOT NULL,  -- 如 'model_preference', 'language', 'project_stack'
  context_value TEXT       NOT NULL,
  updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
  UNIQUE(user_code, context_key)
);
```

这类数据按 `userCode` 查询后注入会话初始化时的系统 prompt，会话内不变。

**L2 层面——能力模板表（后续版本）**：

当前 V1 不需要，但 Hermes 的 Skills 系统对后续 Agent 编排有重要参考：技能通过 Markdown 文件管理，支持渐进式加载（元数据 → 正文 → 引用文件），避免一次性注入全部内容导致 token 浪费。

**L3 层面——历史消息全文检索**：

- V1 明确不做搜索，但建议在 `02-技术方案` 中提到：PostgreSQL 内置 `tsvector` + `pg_trgm` 扩展即可覆盖中英文关键词检索和模糊匹配。
- 后续如需语义搜索，在消息表旁增加 `message_embedding` 表，写入 OpenAI text-embedding-3 或其他向量即可。

### 3.3 冻结快照模式的实际意义

> "Memory that quietly defeats prompt caching is a common and expensive mistake." — Hermes 生产实践

这一点在 V1 不直接体现，但随着对话轮次增长和 system prompt 膨胀，会变得重要。核心教训：

- **不要在当前对话过程中修改 system prompt 内容的来源数据**（即使写入了磁盘）。
- 如果必须引入会话内的"记忆更新"，应通过追加用户消息（role=user）或系统消息（role=system）到对话历史实现，而非替换头部 prompt。
- Anthropic prompt caching 按前缀匹配，前缀一旦变化整个缓存失效。

## 4. 模型 Provider 抽象——面向 Java 的接口设计

### 4.1 Hermes 的 Provider 解析链

```
1. 显式 api_mode 参数（最高优先级）
2. Provider 名称检测（如 anthropic → anthropic_messages 模式）
3. Base URL 启发式（如 api.anthropic.com → anthropic_messages）
4. 默认 fallback（chat_completions）
```

三种 API 模式对上层收敛为统一的 OpenAI 消息格式（`role`/`content`/`tool_calls`）。模式差异（请求体格式、流式事件结构、缓存策略）由适配器层消化。

### 4.2 面向当前项目的建议

GPT-5.5 版调研中已经建议 `LlmProviderAdapter` 抽象。在此补充更具体的 Java 接口草案：

```java
// 建议放在 agent-core 的 llm 包中，V1 只实现 openai-compatible
public interface LlmProviderAdapter {
    /**
     * 返回此适配器支持的 provider 标识，如 "openai"、"deepseek"、"anthropic"
     */
    String provider();

    /**
     * 将 agentscope-java Msg 事件转为统一 SSE 事件类型。
     * 不同 provider 的 stream 事件结构不同（thinking 字段名、格式位置），
     * 由适配器完成归一化。
     */
    Flux<SseEvent> streamResponse(SessionContext ctx, List<Message> history);

    /**
     * 非流式调用（用于标题生成、摘要、评估等辅助任务）
     */
    Mono<String> complete(SessionContext ctx, List<Message> history);
}
```

V1 阶段 `agentscope-java` 已经承担了大部分适配工作（统一为 `Msg` 事件 > 映射到 SSE），因此 V1 可以只提供一个 `OpenAiCompatibleAdapter` 实现。但通过接口隔离 agentscope-java 的调用，可以在后续切换底层依赖或新增 provider 时减少改动范围。

**建议更新位置**：`02-技术方案` 第 2.2 节后端设计，补充接口定义；`04-详细设计` 第 3.1 节包职责，在 `agent-core` 中新增 `llm` 包子包说明。

## 5. Agent Loop 执行模型参考

### 5.1 Hermes Agent Loop 核心流程

```
run_conversation() {
  1. 生成 task_id
  2. 追加用户消息到对话历史
  3. 组装/复用系统 prompt（prompt_builder.py）
  4. 检查是否需要预压缩（>50% context）
  5. 构建 API 消息（三个模式统一转换）
  6. 注入临时 prompt 层（预算警告、上下文压力）
  7. 应用 prompt 缓存标记（Anthropic only）
  8. 可中断 API 调用（_interruptible_api_call）
  9. 解析响应：
     - 有 tool_calls → 执行 → append 结果 → 回到步骤 5
     - 纯文本响应 → 持久化 session → flush memory → 返回
}
```

### 5.2 对我们的启示

| Hermes 特性 | 当前项目对应 | 建议 |
|------------|------------|------|
| 迭代预算（IterationBudget） | V1 不涉及工具循环 | 后续引入 Agent 工具时，必须设置最大迭代上限（Hermes 默认 90 轮/父代理，子代理 50 轮），防止无限循环消耗 token |
| 可中断 API 调用 | 已有 AbortController + WebFlux doOnCancel | 当前实现已覆盖 |
| 上下文预压缩 | V1 不涉及 | 字段已预留（prompt_tokens / summary_message_id），后续实现时参考 Hermes 的"保留最后 N 条 + 压缩中间"策略 |
| Fallback 模型切换 | V1 单一模型 | 后续多模型场景参考：主模型失败 → fallback 模型（可配置链），辅助任务（vision/压缩/摘要）独立 fallback 配置 |
| 平台回调 | V1 仅 Web | callback 机制不是 V1 需求，但可在 Controller 层预留拦截点 |

**关键教训**：Hermes 的压缩时机是 **请求前**（preflight，>50% 上下文时压缩），而非响应后。这是因为压缩发生在 prompt assembly 阶段，能在发送请求前就降低 token 消耗。我们后续实现上下文压缩时应采用同样策略。

## 6. gstack-Hermes 集成模式

gstack 是 YC President Garry Tan 构建的 Claude Code 技能系统（70,000+ stars），与 Hermes Agent 有深度的集成。这种集成模式对当前项目的技能/插件系统设计有参考价值。

### 6.1 工具重写层

gstack 技能文档面向 Claude Code 编写（工具名：Bash/Write/Read/Edit/Grep/Glob），在 Hermes 上运行时通过 host 配置进行工具名重写：

```
Bash tool    → terminal tool
Write tool   → patch tool
Read tool    → read_file tool
Edit tool    → patch tool
Grep tool    → search for
Glob tool    → find files matching
Agent tool   → delegate_task
```

**对我们的启示**：当前项目基于 agentscope-java，后续如果引入 Agent 工具能力，技能/插件文档中描述的"可执行步骤"需要映射到实际的工具调用接口。建议在技术方案中预留"工具描述→实际实现"的映射层，避免技能文档绑定到特定底层实现。

### 6.2 GBrain 记忆桥接

GBrain 是介于 gstack（编码技能）和 Hermes（Agent runtime）之间的持久记忆层：

```
gstack 技能 ←→ GBrain ←→ Hermes Agent
  (编码)      (记忆/检索)    (执行/对话)
```

GBrain 的关键设计决策：

1. **Self-wiring knowledge graph**：从页面内容中通过正则（非 LLM）提取实体关系，零额外 token 消费。240 页语料基准测试验证了可行性。
2. **Compiled truth + Timeline 双层模式**：页面上半部分是"当前认知"（可重写），下半部分是"证据时间线"（仅追加），解决了"我们现在知道什么"和"当时发生了什么"的区分。
3. **Hybrid search pipeline**：意图分类 → 多查询扩展 → 向量搜索 → 关键词搜索 → RRF 融合 → Cosine 重排序 → 编译真值加权 → 反向链接加权 → 来源感知去重。单靠任何一层都不够，层层叠加才可靠。
4. **Minions 模式**：确定性工作（如实体提取、索引重建）通过 PostgreSQL 存储过程运行，不走 LLM 子代理，成本为零。

**对我们项目的启示**：后续如果引入知识库或长期记忆能力，建议参考 GBrain 的分层设计：
- 结构化实体关系 → 正则提取，不消耗 LLM 调用
- 搜索流水线 → 多层融合优于单一策略
- 确定性任务 → 数据库存储过程 / 计划任务，不做不必要的 LLM 调用

## 7. 安全设计可借鉴点

### 7.1 双层 Prompt 注入扫描

Hermes 对所有注入 system prompt 的用户可控内容进行扫描：

- **Context files**（AGENTS.md、.cursorrules、SOUL.md、HERMES.md）——用户可能编辑的 markdown 文件。
- **Memory writes**——agent 写入 memory 的内容可能被污染（用户之前诱导 agent 写入了恶意指令）。

扫描内容包括：注入模式、隐藏 Unicode、数据外泄模式。

**对当前项目的启示**：
- V1 的鉴权极简（仅校验 token 非空），对话内容无注入风险传导。
- 但后续引入 Agent 工具执行时，用户消息可能触发工具调用（如执行命令、访问文件系统），此时必须对用户输入进行扫描。
- **建议**：在 `02-技术方案` 中记录此安全设计点："后续引入工具执行时，所有可能进入 system prompt 或触发工具调用的用户输入需经过注入扫描。"

### 7.2 命令审批与容器隔离

Hermes 对危险命令（rm -rf、DROP TABLE 等）进行检测和审批。工具执行在隔离的终端后端（Docker / SSH / Daytona）中运行。

这是 Agent 安全的基础设施，当前 V1 不涉及，但应作为平台 Agent 能力的安全前置条件记录。

### 7.3 至多一个外部 Memory Provider

> "Every provider adds tool schemas, and stacking three of them bloats the prompt and confuses the model. The config layer doesn't let you make the mistake."

Hermes 的内置 memory 始终激活，但外部 memory provider（Honcho、Mem0、Hindsight 等）最多同时启用一个。这个策略防止了工具 schema 膨胀。

**对我们项目的启示**：后续如果引入多种记忆/检索后端（向量库 + 全文检索 + 图谱），应在配置层面控制并发激活数量，避免 prompt 中充斥大量相似功能的工具描述导致模型混淆。

## 8. 上下文压缩的工程细节

### 8.1 Hermes 的压缩算法

当对话历史接近模型 context limit 的 50% 时，`ContextCompressor` 自动触发：

1. 先 flush memory 到磁盘（防止数据丢失）
2. 对中间轮次的对话进行摘要（lossy summarization）
3. 保留最后 N 条消息完整（`compression.protect_last_n`，默认 20）
4. 生成 tool_call_id 标记压缩边界

**关键点**：压缩发生在请求发送之前（preflight 检查），而非响应返回之后。这避免了在 token 已经超标后才处理的情况。

### 8.2 对我们项目 `summary_message_id` 字段的语义建议

GPT-5.5 版调研已提到该字段的用途。在此补充更精确的语义定义：

> `summary_message_id` 指向一条"摘要消息"——该消息之前的历史消息在上下文组装时已被摘要替代。当前会话的上下文 = `summary_message_id` 对应的摘要文本 + 后续完整消息。

**建议**：在 `04-详细设计` 的消息表字段说明中补充此语义，即使 V1 不做压缩，也要让字段用途明确，避免后续使用偏差。

## 9. Subagent 委派模式的教训

Hermes 的子代理设计有几个关键约束值得记录：

| 约束 | 值 | 原因 |
|------|-----|------|
| 最大委派深度 | 2 | 防止递归爆炸 |
| 子代理工具白名单 | 不能递归 delegate、不能 clarify、不能 memory、不能 send_message、不能 execute_code | 防止子代理越权或产生副作用 |
| 子代理迭代预算 | 独立，默认 50 轮 | 子代理的循环不计入父代理的总预算 |
| 父代理阻塞 | 阻塞等待子代理返回摘要 | 保证结果确定性 |

**对我们项目的启示**：V1 不涉及子代理。但后续平台 Agent 编排中，委派是核心能力。Hermes 的深度限制和工具白名单策略值得直接参考——在 Agent 编排模块中，子任务必须受限执行，不能获取完整代理权限。

## 10. 建议更新到现有文档的位置（补充）

以下为对 GPT-5.5 版调研建议的补充，标注 *(new)* 的部分为新增建议：

### 10.1 `02-通用大模型对话技术方案.md` *(new)*

新增"设计原则"小节，记录从 Hermes 实践中提炼的三条约束：

- **会话内 Prompt 不变性**：会话启动后系统级 prompt 不修改，变更新会话生效
- **Core 层与 Web 层解耦**：`agent-core` 不依赖 Spring WebFlux，由 `agent-web` 负责 HTTP 协议适配
- **可选模块 check_fn 门控**：后续引入新模块时采用 register + availability check，不硬耦合核心链路

### 10.2 `04-通用大模型对话详细设计.md` *(new)*

在 `agent-core/llm` 包下补充 `LlmProviderAdapter` 接口设计草稿（见第 4.2 节），V1 只实现 `OpenAiCompatibleAdapter`，通过接口预留扩展点。

### 10.3 非首版范围 *(new)*

建议在 `01-需求` 的非首版范围中追加：

- 会话内系统 Prompt 动态修改
- Agent 工具注册表与工具调用
- 上下文压缩与自动摘要
- 子 Agent 委派与并行执行
- 文件操作、命令执行类工具的安全审批
- 多 Memory Provider 并发激活

## 11. 综合判断

Hermes Agent 对当前 V1 通用大模型对话项目的核心价值不是具体实现细节（Python 14K 行单体 vs Java 多模块 Web 应用），而是以下结构性的设计决策：

### 11.1 V1 阶段即可引入的原则

1. **Prompt 稳定性约束**：作为技术方案的显式设计原则写入，即使 V1 的 system prompt 结构简单
2. **Core 层与 Web 层解耦**：当前 Maven 模块结构已支持，需要在详细设计中明确依赖方向约束
3. **消息格式归一化**：内部统一使用标准格式，外部适配器完成不同 provider 的格式转换（agentscope-java 已部分承担）
4. **可中断执行设计**：当前已有 AbortController + doOnCancel，后续扩展到 H2H 中断指令

### 11.2 后续版本的核心参考

5. **分层记忆架构**：L1 用户上下文表（SQL）→ L2 能力模板表（Markdown）→ L3 消息全文检索（PostgreSQL FTS + pg_trgm）
6. **上下文压缩策略**：preflight 检查 + 保留最后 N 轮 + 中间摘要替代 → 字段已预留，实现路径清晰
7. **Provider 抽象接口**：接口定义 + V1 单实现 → 后续多实现零改动
8. **子代理委派约束**：深度限制 + 工具白名单 + 独立迭代预算 → Agent 编排的核心安全模式
9. **确定性与 LLM 任务分流**：实体提取、索引重建走存储过程/脚本，不消耗 LLM 调用 → 参考 GBrain Minions 模式

### 11.3 不适合引入的方向

- 自动技能创建与自改进（审核/安全/稳定性风险）
- 多平台 Gateway（V1 明确只做 Web）
- Cron 调度、批量轨迹生成、RL 训练环境（MLOps 专用能力）
- Python 单体架构（与技术栈不匹配）
- 文件级 Checkpoint/Rollback（纯对话系统不需要）

### 11.4 与 GPT-5.5 版调研的差异

| 维度 | GPT-5.5 版 | 本版补充 |
|------|-----------|---------|
| 架构原则 | 未涉及 | 详细分析五条设计原则，提取三条直接适用的 |
| 记忆系统 | 仅提及 FTS5 检索 | 完整分析三层架构 + 数据模型演进路径 + GBrain 集成 |
| Provider 抽象 | 建议增加接口 | 给出具体 Java 接口草案 + V1 实现策略 |
| Agent Loop | 未涉及 | 分析核心流程 + 五个直接启示（迭代预算/压缩时机/fallback） |
| 安全设计 | 未涉及 | 双层注入扫描 + 命令审批 + 至多一个 memory provider |
| gstack 集成 | 未涉及 | 工具重写层 + GBrain 记忆桥接 + Minions 模式 |
| 上下文压缩 | 简述 | 详细分析压缩时机和算法 + summary_message_id 精确定义 |
| 子代理委派 | 未涉及 | 完整分析五个安全约束 |
| 落地建议 | 四个文档更新点 | 扩展为 11 条综合判断，区分 V1 引入和后续参考 |

## 12. 参考来源

- Hermes Agent 官网：https://hermes-agent.org/
- Hermes Agent GitHub：https://github.com/NousResearch/hermes-agent
- Hermes 架构文档：https://hermes-agent.nousresearch.com/docs/developer-guide/architecture
- Hermes Agent Loop 内部机制：https://hermes-agent.nousresearch.com/docs/developer-guide/agent-loop
- Hermes 记忆系统深度解析：https://hermes-agent.ai/blog/hermes-agent-memory-system
- Hermes 生产实践分析：https://saulius.io/blog/hermes-agent-self-improving-ai-architecture
- Hermes v0.10 技术评估：https://www.digitalapplied.com/blog/hermes-agent-v0-10-self-improving-open-source-guide
- gstack-Hermes 集成 PR：https://github.com/garrytan/gstack/commit/b805aa0113040fb78228068ce808772299caf244
- gstack-Hermes host 配置：https://raw.githubusercontent.com/garrytan/gstack/main/hosts/hermes.ts
- GBrain + GStack：https://hermesatlas.com/projects/garrytan/gbrain
- Gormes（Hermes Go 移植）：https://docs.gormes.ai/upstream-hermes/
- gstack Field Guide：https://agentairforce.com/osint/gstack/
- GPT-5.5 版同期调研：`docs/v1-通用大模型对话/调研/HermesAgent-v1-GPT5.5.md`
