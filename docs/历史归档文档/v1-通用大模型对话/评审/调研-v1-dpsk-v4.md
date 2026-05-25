# 调研综合分析评审报告

> 评审版本：v1
> 评审模型：DeepSeek-v4-pro
> 评审日期：2026-05-14
> 评审范围：docs/v1-通用大模型对话/调研/ 全部 6 篇调研报告
> 对照基线：docs/v1-通用大模型对话/01/02/03/04 项目文档 + docs/v1-通用大模型对话/评审/v1-dpsk-v4-pro.md + docs/v1-通用大模型对话/评审/v1-glm5.1.md

---

## 零、评审方法论

### 0.1 六篇调研概览

| # | 文件 | 对象 | 模型 | 定位 |
|---|------|------|------|------|
| 1 | HermesAgent-v1-GPT5.5.md | Hermes Agent | GPT-5.5 | 首轮调研，覆盖 Provider 抽象、上下文压缩、检索预留、中断、工具注册 |
| 2 | HermesAgent-v1-dpsk-v4-pro.md | Hermes Agent | DeepSeek-v4-pro | 补研，覆盖架构原则、三层记忆、Agent Loop、gstack 集成、安全、子代理 |
| 3 | HermesAgent-v1-glm.md | Hermes Agent v0.13.0 | GLM-5.1 | 补研，覆盖闭环学习、Periodic Nudge、技能自改进、v0.13 新增能力、技能市场 |
| 4 | openclaw-v1-gpt5.5.md | OpenClaw | GPT-5.5 | 首轮调研，覆盖 Gateway 控制平面、Tool/Skill/Plugin 体系、Context Engine、安全 |
| 5 | Openclaw-v1-dpsk-v4.md | OpenClaw | DeepSeek-v4-pro | 独立调研，覆盖四层架构、Lane Queue、Active Memory、子代理、Heartbeat |
| 6 | OpenClaw-v1-glm.md | OpenClaw v2026.5.x | GLM-5.1 | 补研，覆盖 Gateway 协议、会话模型、沙箱安全、Failover、上下文压缩、知识库 |

### 0.2 分析维度

本报告从四个维度交叉分析六篇调研：

1. **收敛点**：多篇调研独立得出相同结论的设计模式——可信度最高，应优先吸收
2. **互补点**：不同调研覆盖不同层次，组合后形成完整视角
3. **分歧点**：调研对象自身的架构分歧（Hermes vs OpenClaw）——反映两种路线选择
4. **遗漏点**：调研覆盖充分但均未深入的话题——可能是不重要的，也可能是共同盲区

---

## 一、跨调研收敛分析

以下设计模式在三篇或以上调研中被独立识别并推荐引用，构成高可信度的吸收清单。

### 1.1 强收敛：六篇一致推荐

#### 1.1.1 模型 Provider 抽象层

| 来源 | 建议强度 | 要点 |
|------|---------|------|
| Hermes-GPT5.5 | 高 | `LlmProviderAdapter` 接口，V1 单实现，后续多实现 |
| Hermes-dpsk | 高 | Java 接口草案，配置预留 provider/modelName/apiMode/baseUrl/apiKey |
| Hermes-glm | 高 | `LlmProviderAdapter` 接口优先于具体实现 |
| OpenClaw-GPT5.5 | 中 | 模型配置化，system prompt 分层 |
| OpenClaw-dpsk | 低 | Agent 运行时抽象（V1 单运行时，后续多运行时） |
| OpenClaw-glm | 中 | Model Failover 和 fail-closed 策略 |

**评审结论**：这是六篇调研公认的 V1 最高优先级吸收项。当前项目已有 `baseUrl`、`apiKey`、`modelName` 配置项，但缺少接口抽象层。建议 V1 实现 `OpenAiCompatibleAdapter`，通过接口预留后续扩展点。**此建议应写入 `02-技术方案` 和 `04-详细设计`**。

#### 1.1.2 核心层与入口层解耦

| 来源 | 建议强度 | 要点 |
|------|---------|------|
| Hermes-dpsk | 高 | "平台无关核心"原则 — agent-core 不依赖 WebFlux |
| Hermes-glm | 高 | Core 模块与协议适配分离 |
| OpenClaw-GPT5.5 | 中 | Gateway 控制平面与 Agent 运行时解耦 |
| OpenClaw-dpsk | 高 | 四层架构 — Gateway / Execution / Integration / Intelligence |
| OpenClaw-glm | 中 | Gateway 作为独立守护进程，Agent 作为被嵌入组件 |

**评审结论**：当前 Maven 多模块结构（`agent-api` / `agent-core` / `agent-web`）已经在工程层面体现了此原则。建议在 `04-详细设计` 中显式约束：`agent-core` 不引入 `spring-boot-starter-webflux`，业务服务通过接口接收上下文，由 `agent-web` 的 Filter/Controller 负责协议适配。

#### 1.1.3 上下文压缩与 Session Lineage

| 来源 | 建议强度 | 要点 |
|------|---------|------|
| Hermes-GPT5.5 | 高 | `summary_message_id` + `parent_session_id` 语义明确化 |
| Hermes-dpsk | 高 | 压缩时机（preflight > 50% context）和算法（保留最后 N + 压缩中间） |
| Hermes-glm | 高 | SessionDB lineage、压缩触发条件（接近 context 50%） |
| OpenClaw-GPT5.5 | 中 | Context engine 生命周期，compaction 后生成 tool_call_id |
| OpenClaw-glm | 中 | Auto-triggered compaction，基于 token 估算 |

**评审结论**：当前数据表已预留 `parent_session_id`、`summary_message_id`、`prompt_tokens`、`completion_tokens` 字段。V1 不做压缩，但必须明确这些字段的语义，避免后续使用偏差。**关键决策**：压缩时机应为请求前（preflight），而非响应后。

#### 1.1.4 会话搜索与检索预留

| 来源 | 建议强度 | 要点 |
|------|---------|------|
| Hermes-GPT5.5 | 中高 | PostgreSQL full-text search + `pg_trgm`，V1 不实现 |
| Hermes-dpsk | 中 | SQLite FTS5 方案的延迟优势（10ms @10k docs），可考虑 PostgreSQL `tsvector` |
| Hermes-glm | 中 | FTS5 是关键词搜索（非语义搜索），复杂查询需 LLM 辅助；SQLite 零运维 |
| OpenClaw-dpsk | 中 | SQLite 嵌入式向量搜索（`sqlite-vec`），Active Memory 检索模式 |
| OpenClaw-glm | 中 | 知识库（KDB）独立存储 |

**评审结论**：V1 不做搜索，但检索演进有两条清晰路径：**简单路径**（PostgreSQL `tsvector` + `pg_trgm` → 关键词检索）和**复杂路径**（embedding 表 + pgvector → 语义检索）。建议在 `02-技术方案` 中以"后续演进"形式记录两条路径。

### 1.2 中收敛：4-5 篇推荐

#### 1.2.1 串行化会话执行

| 来源 | 建议强度 | 要点 |
|------|---------|------|
| Hermes-dpsk | 中 | Agent Loop 同步执行，concurrent tool execution 通过 ThreadPoolExecutor |
| OpenClaw-GPT5.5 | 中 | Side-effect 请求需要 idempotency key + 去重 |
| OpenClaw-dpsk | 高 | **Lane Queue** — per-session 串行队列 + 独立后台 lane |
| OpenClaw-glm | 中 | Gateway 命令队列，同 session 消息排队而非拒绝 |

**评审结论**：当前项目已设计会话锁（`Semaphore` + 409），与 Lane Queue 理念一致。但 Lane Queue 的排队而非拒绝策略在后台任务场景中更优雅。**建议**：V1 保持现有 409 策略（更简单），在 `02-技术方案` 中记录后续升级为 Lane Queue 的条件。

#### 1.2.2 子代理委派安全约束

| 来源 | 建议强度 | 要点 |
|------|---------|------|
| Hermes-dpsk | 中 | 深度限制（≤2）、工具白名单、独立迭代预算、父代理阻塞等待 |
| Hermes-glm | 中 | 子代理最大 depth 2，execute_code 不消耗 budget |
| OpenClaw-dpsk | 高 | maxSpawnDepth、工具策略分层叠加、Lane 隔离 |
| OpenClaw-glm | 中 | Main vs Untrusted 会话的沙箱隔离 |

**评审结论**：V1 无子代理需求。但三篇调研独立的子代理安全约束分析高度一致，可作为后续 Agent 编排的安全基线直接引用。**核心约束**：深度 ≤ 2、默认不可 spawn、工具策略继承 + 限制、独立执行 Lane。

### 1.3 弱收敛：3 篇推荐

#### 1.3.1 可中断执行

| 来源 | 要点 |
|------|------|
| Hermes-GPT5.5 | V1 内部 abort，后续 UI "停止生成"按钮 |
| Hermes-dpsk | AbortController + WebFlux doOnCancel |
| Hermes-glm | Interrupt 机制，工具执行可中途取消 |

**评审结论**：当前详细设计已有 `AbortController` + `doOnCancel()`。建议在 `01-需求` 非首版范围明确增加"停止生成按钮"。

#### 1.3.2 Prompt 稳定性（冻结快照）

| 来源 | 要点 |
|------|------|
| Hermes-dpsk | "System prompt doesn't change mid-conversation" |
| Hermes-glm | 冻结快照模式保护 Anthropic prompt cache prefix |
| OpenClaw-GPT5.5 | Workspace 文件注入到 system prompt，分层管理 |

**评审结论**：V1 的 system prompt 简单（配置中的一段文本），此原则不直接适用。但应在 `02-技术方案` 中记录此约束："会话启动后的 system prompt 在会话过程中不变更"。原因：保护 prompt caching 收益（后续对接 Anthropic 时关键）。

#### 1.3.3 工具注册与运行时

| 来源 | 要点 |
|------|------|
| Hermes-GPT5.5 | 注册表、toolset、dispatch、hook 四层结构 |
| Hermes-glm | 工具注册表（registry.py），70+ 工具自注册 |
| OpenClaw-GPT5.5 | Tool/Skill/Plugin 三层能力体系，fail-closed 策略 |

**评审结论**：V1 不做工具调用。但建议在 `02-技术方案` 中记录三层能力体系（Tool 执行层 / Skill 指导层 / Plugin 扩展层）作为后续 Agent 能力框架的设计基线。

---

## 二、跨调研互补分析

多篇调研覆盖同一主题的不同层次，组合后形成完整视角。

### 2.1 记忆架构：从 Hermes 三层到 OpenClaw Active Memory

| 层次 | Hermes 方案 | OpenClaw 方案 | 互补价值 |
|------|-----------|-------------|---------|
| 持久记忆存储 | MEMORY.md（Markdown 文件，2,200 字符上限） | SQLite（`memory-core` 插件，无硬限制） | Hermes 简单透明，OpenClaw 结构化可查询 |
| 会话历史 | SQLite FTS5 全文检索 + LLM 摘要 | SQLite 向量搜索（`sqlite-vec`） | Hermes 关键词快，OpenClaw 语义召回强 |
| 记忆注入时机 | 会话启动时写入 system prompt（冻结快照） | Active Memory：主回复前阻塞式检索 | Hermes 模式节省 token；OpenClaw 模式相关度更高 |
| 记忆编码触发 | Periodic Nudge（周期性自我提示） | 被动（对话中写入） | Hermes 主动性更强 |
| 用户画像 | USER.md + Honcho 方言建模 | USER.md（偏好文件） | Hermes 跨会话建模更深 |

### 2.2 安全架构：五层 vs 分工

| 安全维度 | Hermes 方案 | OpenClaw 方案 |
|---------|-----------|-------------|
| Prompt 注入扫描 | 双层扫描（context files + memory writes） | 设备身份加密握手 + allowlists |
| 命令审批 | 危险命令检测 + approval 机制 | Tool policy profiles（coding/full/messaging） |
| 沙箱隔离 | 6 种终端后端（local/Docker/SSH/Daytona/Singularity/Modal） | Docker per-session（main on host, untrusted in container） |
| 工具策略 | tooset 级别 allow/deny | 分层 tool-policy pipeline（继承 + 限制） |
| Memory 安全 | 至多一个外部 memory provider（防 schema 膨胀） | 记忆子代理工具白名单（仅 memory_search/memory_get） |

**综合启示**：两者安全思路互补——Hermes 侧重"输入安全"（prompt 注入、memory 污染），OpenClaw 侧重"执行安全"（沙箱、工具策略分层、profiles）。完整的 Agent 安全体系应覆盖两者。

### 2.3 闭环学习 vs 编排控制——两条路线的完整图景

| 维度 | Hermes（认知深度） | OpenClaw（编排广度） |
|------|-------------------|---------------------|
| Agent 如何变强 | 从使用经验中自改进（闭环学习） | 社区贡献新插件/配置优化 |
| 能力来源 | 自动生成的 Skills | ClawHub 社区市场 |
| 记忆模型 | 三层记忆 + 自管理 | SQLite 结构化存储 + Active Memory |
| 多 Agent | 子任务委派（单 Agent 深） | 原生多 Agent 编排（广） |
| 平台策略 | 少而精（6 核心通道） | 多而广（25+ 通道） |
| 安全哲学 | 输入安全优先 | 执行安全优先 |
| 运维模式 | Serverless（Modal/Daytona/$5 VPS） | 持久运行守护进程 |

**这对项目定位的启示**：如果 `htam-agent-platform` 长期目标是"企业级 Agent 平台"，则需要两者结合——以 OpenClaw 式的 Gateway 编排层作为平台骨架，以 Hermes 式的自改进能力作为 Agent 的差异化价值。

---

## 三、分歧与风险分析

### 3.1 调研对象本身的设计分歧

#### 3.1.1 主动学习 vs 被动执行

Hermes 的 Periodic Nudge 和自动 Skill 创建代表了主动学习路线——Agent 自行决定"什么值得记住"、"如何改进"。OpenClaw 的被动执行路线——Agent 的行为完全由配置文件和社区插件决定，不产生自主变更。

**对项目的风险**：
- Hermes 路线引入可预测性和审计性问题——Agent 自动修改自身行为，谁对此负责？
- OpenClaw 路线引入静态性问题——Agent 不会从使用中变得更好，需要 operator 持续介入。

**建议**：当前项目在 V1-V2 阶段应采用 OpenClaw 式的被动路线（可预测 > 可学习），在 V3+ 阶段引入可控的自改进能力（经过人工审核的技能可用性优化、记忆自动整理等非破坏性改进）。

#### 3.1.2 记忆存储：Markdown vs SQLite

Hermes 选择 Markdown 文件（人工可读可编辑，auditability），OpenClaw 选择 SQLite（结构化可查询，performance）。两种方案各有侧重。

**建议**：当前项目使用 PostgreSQL 已有结构化优势。后续长期记忆存储建议保持 SQL 方案（已有基础设施），同时参考 Hermes 的"人工可读副本"思想——定期将关键记忆导出为可读的摘要视图。

#### 3.1.3 Gateway 单点 vs Agent Loop 中心

OpenClaw 的 Gateway 是单点控制平面，Hermes 的 AIAgent 是多入口共享核心。两者本质上都是"单点"——只是单点的位置不同。

**建议**：当前项目的 Maven 多模块设计更接近 OpenClaw 模式（`agent-web` 是入口层，`agent-core` 是共享核心）。保持此分离，不将 HTTP 协议逻辑混入 `agent-core`。

### 3.2 调研覆盖充分但存在盲区的领域

以下话题在所有六篇调研中均未深入分析：

1. **多租户数据隔离**：Hermes 和 OpenClaw 都是单用户设计，未覆盖多租户场景。当前项目的 `userCode` 隔离是正确起点，但在后续引入团队协作时需要系统性设计。
2. **对话质量评估**：无调研讨论如何评估 Agent 回复质量（人工标注？自动评分？A/B 对比？）。
3. **成本控制与预实估算**：仅 Hermes-glm 简要提到 token 消耗对比（Hermes 比 OpenClaw 节省 36%），但无系统性的成本管理设计。
4. **国际化/多语言**：无覆盖。当前项目面向中文用户，`pg_trgm` 对中文 fuzzy search 的支持应提前验证。
5. **消息队列/事件总线的可靠性**：OpenClaw-glm 提到幂等键和去重缓存，但无深入的可靠消息传递设计。

**建议**：以上盲区在当前 V1 阶段非阻塞项，但应在后续版本规划中逐项覆盖。

---

## 四、V1 即时吸收清单

以下为六篇调研中识别出的、应立即写入当前项目文档的设计决策和约束。

### 4.1 应写入 `01-通用大模型对话需求.md` 的补充

| 序号 | 补充内容 | 来源 | 优先级 |
|------|---------|------|--------|
| N1.1 | **非首版范围补充**：停止生成按钮、跨会话记忆检索、定时自主任务、多入口接入、Agent 工具策略与权限 | Hermes-GPT5.5, Hermes-dpsk, OpenClaw-dpsk | 高 |
| N1.2 | **排队消息持久化边界**：明确"刷新/关闭后丢失是预期行为" | 已有评审识别（v1-dpsk-v4-pro），本次重新确认 | 高 |

### 4.2 应写入 `02-通用大模型对话技术方案.md` 的补充

| 序号 | 补充内容 | 来源 | 优先级 |
|------|---------|------|--------|
| N2.1 | **设计原则小节（新增）**：① 会话内 Prompt 不变性；② Core 层与 Web 层解耦（agent-core 不依赖 WebFlux）；③ 可选模块 register + availability check 门控 | Hermes-dpsk, Hermes-glm, OpenClaw-dpsk | **最高** |
| N2.2 | **LlmProviderAdapter 抽象**：Java 接口草案，V1 单实现（OpenAiCompatible），配置预留 provider/modelName/apiMode/baseUrl/apiKey | 六篇一致推荐 | **最高** |
| N2.3 | **`summary_message_id` 字段语义明确化**：压缩边界标记——此消息之前的历史被摘要替代 | Hermes-GPT5.5, Hermes-dpsk, Hermes-glm | 高 |
| N2.4 | **演进路线补充**：Lane Queue 升级条件、Active Memory 记忆检索蓝图、Manifest-first 插件注册约束、子代理深度限制安全基线 | OpenClaw-dpsk, OpenClaw-glm, Hermes-dpsk | 高 |
| N2.5 | **安全设计小节（新增）**：V1 安全边界 + 后续五层防御体系（访问控制/工具策略/沙箱隔离/命令审批/审计日志） | Hermes-dpsk, OpenClaw-dpsk, OpenClaw-glm | 中 |

### 4.3 应写入 `04-通用大模型对话详细设计.md` 的补充

| 序号 | 补充内容 | 来源 | 优先级 |
|------|---------|------|--------|
| N3.1 | **agent-core 依赖约束**：不引入 `spring-boot-starter-webflux`，领域服务通过接口接收上下文 | Hermes-dpsk, Hermes-glm | **最高** |
| N3.2 | **agent-core/llm 包新增 LlmProviderAdapter 接口**：V1 实现 OpenAiCompatibleAdapter | Hermes-dpsk, Hermes-GPT5.5 | 高 |
| N3.3 | **消息表预留字段**：`source_type` 字段（WEB/API/MOBILE，V1 全部 WEB） | OpenClaw-dpsk | 中 |
| N3.4 | **会话锁语义补充**：明确与 Lane Queue 的关系，记录升级条件 | OpenClaw-dpsk | 中 |

### 4.4 应写入 `03-原型设计` 的补充

| 序号 | 补充内容 | 来源 | 优先级 |
|------|---------|------|--------|
| N4.1 | 无新增交互要求。现有原型已覆盖 V1 范围。停止生成按钮在后续版本增加。 | — | — |

---

## 五、版本演进路线图建议

基于六篇调研的综合分析，建议将识别出的能力按版本分期吸收：

### 5.1 V1（当前交付）：基础对话

**立即吸收**（写文档，不改代码）：
- 三大设计原则 → `02-技术方案`
- LlmProviderAdapter 接口草案 → `04-详细设计`
- 字段语义明确化 → `02-技术方案` / `04-详细设计`
- 安全设计基线 → `02-技术方案`
- 非首版范围补充 → `01-需求`

### 5.2 V2（Agent 工具引入）：对话 + 工具调用

**引入能力**：
1. **Lane Queue 升级**：会话锁 → per-session serial lane
2. **工具注册表**：ToolDescriptor + ToolPolicy + ToolExecutionService（参考 Hermes registry + OpenClaw tool-policy pipeline）
3. **fail-closed 工具策略**：无工具时上下文不出现工具能力暗示
4. **Provider 多实现**：LlmProviderAdapter 增加 Anthropic 等 adapter
5. **停止生成按钮**：UI interrupt + 后端 cancel status 标记

### 5.3 V3（Agent 编排）：多 Agent + 长期记忆

**引入能力**：
1. **子代理委派**：深度限制（≤2）+ 工具策略分层 + Lane 隔离
2. **Active Memory 检索**：阻塞式记忆子代理 + graceful degradation
3. **上下文压缩**：preflight 检查 + 保留最后 N + 中间摘要
4. **PostgreSQL 全文检索**：`tsvector` + `pg_trgm` 会话搜索
5. **多入口适配器**：REST + WebSocket + API 多协议支持

### 5.4 V4+（平台化）：技能系统 + 主动 Agent

**引入能力**：
1. **Manifest-first 插件系统**：参考 OpenClaw 四类插件注册 + Hermes 热加载
2. **Heartbeat 定时任务**：Spring `@Scheduled` + 检查清单模式
3. **自改进能力（可选）**：人工审核的 Skill 优化 + 记忆自动整理
4. **语义检索**：embedding 表 + pgvector
5. **多模型 Failover**：主模型失败 → fallback 链（参考 Hermes fallback + OpenClaw fail-closed）

---

## 六、与已有评审的交叉验证

### 6.1 与 `v1-dpsk-v4-pro.md` 的对齐

| v1-dpsk-v4-pro 评审发现 | 调研支撑 | 状态 |
|------------------------|---------|------|
| 排队消息持久化边界不明确 | 未直接涉及，但 Lane Queue 模式暗示排队应有明确语义 | 一致 |
| 会话删除语义需澄清 | OpenClaw 会话模型（main/dm/group + 存档策略）| 补充了软删除参考 |
| thinking 显示策略不完整 | 未涉及 | 调研未覆盖 UI 交互细节 |
| 自动标题生成规则模糊 | 未涉及 | 调研未覆盖 |
| Spring Boot 版本不一致 | 未涉及（非调研范围） | — |
| agentscope 流式事件映射 | 未涉及（非调研范围） | — |
| parts 化存储 ID 类型不一致 | 未涉及 | 调研未覆盖数据库设计细节 |

### 6.2 与 `v1-glm5.1.md` 的对齐

| v1-glm5.1 评审发现 | 调研支撑 | 状态 |
|-------------------|---------|------|
| Spring Boot 4.0.5 vs 4.0.6 不一致 | 未涉及 | — |
| 原型数据模型 ID 类型不一致 | 未涉及 | — |
| SSE 流程页路径不一致 | 未涉及 | — |
| agentscope ThinkingBlock/TextBlock 混合事件 | Hermes-dpsk Provider 抽象可覆盖此场景 | 间接支撑 |
| 排队消息上限建议 | Lane Queue 模式提供参考 | 补充了模式参考 |

### 6.3 调研评审的新增发现

以下为调研综合分析独立发现、不为已有评审所覆盖的问题：

| 新增发现 | 严重程度 | 说明 |
|---------|---------|------|
| **缺少明确的 platform-agnostic core 约束** | 中等 | `agent-core` 依赖边界未在文档中显式约束，可能导致 Web 层逻辑泄露到核心层 |
| **summary_message_id 语义未定义** | 中等 | 字段已预留但语义未写，后续可能产生理解偏差 |
| **缺少 Provider 抽象层设计** | 中等 | 六篇调研一致推荐，但当前文档未提及 |
| **没有版本演进路线图** | 低 | 建议以本文第五章为基础，在技术方案中增加演进路线说明 |
| **缺少安全设计基线** | 低 | V1 安全需求简单但应有基线描述，指导后续扩展 |

---

## 七、执行建议

### 7.1 立即执行的文档更新（无代码改动）

以下更新应在 V1 开发启动前完成，仅涉及文档修订：

1. **`02-技术方案`**：新增"第三节 设计原则"（Prompt 稳定性、Core 解耦、松散耦合）+ "第十节 后续演进参考"（Lane Queue、Active Memory、子代理安全、Provider 抽象、Manifest-first 插件）
2. **`04-详细设计`**：新增 `agent-core/llm` 包的 `LlmProviderAdapter` 接口草案；明确 `agent-core` 不依赖 WebFlux 的约束；补充 `summary_message_id` 语义
3. **`01-需求`**：非首版范围补充 5 项（停止生成、记忆检索、定时任务、多入口、工具策略）

### 7.2 后续版本规划

参考第五章的 V2/V3/V4 分期建议，将调研识别的能力按优先级和依赖关系排入后续版本。

### 7.3 调研质量自评

| 维度 | 评价 | 说明 |
|------|------|------|
| 覆盖全面性 | ★★★★☆ | 两个主流 Agent 平台（Hermes + OpenClaw），六篇交叉验证，覆盖架构/记忆/安全/插件/编排 |
| 重复度 | 可控 | 核心模式（Provider 抽象、层解耦）存在合理重复（交叉验证），细节发现（GLM 的 v0.13.0、dpsk 的 gstack 集成）形成互补 |
| 与项目关联度 | ★★★★☆ | 对 V1 的直接指导较少（Hermes/OpenClaw 远超当前阶段），但对 V2+ 的演进方向给出了清晰路线 |
| 盲区 | 见 3.2 节 | 多租户、质量评估、成本控制、国际化 |

---

## 八、参考来源

- `docs/v1-通用大模型对话/调研/HermesAgent-v1-GPT5.5.md`
- `docs/v1-通用大模型对话/调研/HermesAgent-v1-dpsk-v4-pro.md`
- `docs/v1-通用大模型对话/调研/HermesAgent-v1-glm.md`
- `docs/v1-通用大模型对话/调研/openclaw-v1-gpt5.5.md`
- `docs/v1-通用大模型对话/调研/Openclaw-v1-dpsk-v4.md`
- `docs/v1-通用大模型对话/调研/OpenClaw-v1-glm.md`
- `docs/v1-通用大模型对话/01-通用大模型对话需求.md`
- `docs/v1-通用大模型对话/02-通用大模型对话技术方案.md`
- `docs/v1-通用大模型对话/03-原型设计/`
- `docs/v1-通用大模型对话/04-通用大模型对话详细设计.md`
- `docs/v1-通用大模型对话/评审/v1-dpsk-v4-pro.md`
- `docs/v1-通用大模型对话/评审/v1-glm5.1.md`
