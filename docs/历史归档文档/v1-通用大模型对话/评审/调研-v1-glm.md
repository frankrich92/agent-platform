# 调研综合评审：Hermes Agent 与 OpenClaw

> 评审版本：v1
> 评审模型：GLM-5.1
> 评审日期：2026-05-14
> 评审范围：6 篇调研报告（Hermes Agent × 3 + OpenClaw × 3）+ 通用大模型对话 01/02/03/04 文档
> 调研来源：
> - Hermes Agent：`docs/v1-通用大模型对话/调研/HermesAgent-v1-GPT5.5.md`、`docs/v1-通用大模型对话/调研/HermesAgent-v1-dpsk-v4-pro.md`、`docs/v1-通用大模型对话/调研/HermesAgent-v1-glm.md`
> - OpenClaw：`docs/v1-通用大模型对话/调研/openclaw-v1-gpt5.5.md`、`docs/v1-通用大模型对话/调研/Openclaw-v1-dpsk-v4.md`、`docs/v1-通用大模型对话/调研/OpenClaw-v1-glm.md`

---

## 一、调研全景

### 1.1 调研对象与规模

| 调研对象 | 版本 | Stars | 语言栈 | 核心定位 |
|---------|------|-------|--------|---------|
| Hermes Agent | v0.13.0 | 148k | Python | 闭环学习的自主 Agent runtime |
| OpenClaw | v2026.5.x | 372k | TypeScript | 本地优先的个人 AI 助手平台 |

两者同源但走向不同：Hermes 从 OpenClaw fork 而来，专注 Agent 自我进化能力；OpenClaw 专注平台编排完整性。两者互补而非竞争。

### 1.2 六篇调研的覆盖矩阵

| 主题 | Hermes GPT-5.5 | Hermes DeepSeek-v4 | Hermes GLM-5.1 | OpenClaw GPT-5.5 | OpenClaw DeepSeek-v4 | OpenClaw GLM-5.1 |
|------|:-:|:-:|:-:|:-:|:-:|:-:|
| Provider 抽象 | ● | ● | ● | ○ | ○ | ○ |
| 上下文压缩 | ● | ● | ● | ● | ● | ● |
| 会话 Lineage | ● | ● | ○ | ○ | ○ | ○ |
| 跨会话检索 | ● | ● | ● | ● | ● | ● |
| 可中断执行 | ● | ● | ● | ○ | ○ | ○ |
| 工具注册表 | ● | ○ | ● | ● | ● | ● |
| 三层记忆架构 | ○ | ● | ● | ○ | ● | ● |
| 记忆内部实现 | ○ | ○ | ● | ○ | ○ | ● |
| 闭环学习 | ○ | ○ | ● | ○ | ○ | ○ |
| 技能系统 | ○ | ○ | ● | ○ | ● | ● |
| Gateway 架构 | ○ | ○ | ○ | ● | ● | ● |
| Lane Queue | ○ | ○ | ○ | ○ | ● | ○ |
| Active Memory | ○ | ○ | ○ | ○ | ● | ○ |
| 子代理委派 | ○ | ● | ● | ○ | ● | ○ |
| 模型 Failover | ○ | ○ | ○ | ○ | ○ | ● |
| 沙箱安全 | ○ | ● | ● | ● | ● | ● |
| 消息转向/Steering | ○ | ○ | ○ | ● | ○ | ● |
| 心跳/主动性 | ○ | ○ | ○ | ○ | ● | ○ |

●=深入分析  ○=涉及但不深入  空=未涉及

### 1.3 三模型共识与分歧

**全票共识（6/6）**：

1. V1 不引入闭环学习、技能自创建、多平台 Gateway、Cron 调度、子代理委派
2. V1 应预留 Provider 抽象接口、上下文压缩字段语义、可中断执行
3. 后续演进方向：分层记忆、工具注册表、跨会话检索、安全扫描
4. `agent-core` 与 `agent-web` 必须解耦

**多数共识（4-5/6）**：

5. 上下文压缩必须 preflight（请求前检查），而非 post-response
6. 辅助任务（摘要/记忆刷新）应支持独立小模型
7. 压缩前必须先执行记忆持久化
8. 安全应分层设计，不依赖单一机制

**模型间分歧**：

| 分歧点 | GPT-5.5 | DeepSeek-v4-pro | GLM-5.1 |
|--------|---------|-----------------|----------|
| V1 是否需要 ContextAssembler 抽象 | 是，立即可做 | 在详细设计中预留包职责 | 作为后续演进，V1 仅记录原则 |
| 会话锁升级为 Lane Queue 的时机 | 未明确 | 引入后台任务/工具时 | 引入后台任务/工具时 |
| `source_type` 字段预留 | 未提及 | 未提及 | 建议预留（WEB/API/MOBILE） |
| 安全声明细化程度 | 明确 thinking/日志脱敏 | 记录安全设计点 | 记录安全设计点 |

---

## 二、与当前项目的架构映射

### 2.1 Maven 模块 ↔ 开源项目架构映射

| 当前项目模块 | OpenClaw 对应 | Hermes 对应 | 设计原则 |
|------------|-------------|------------|---------|
| `agent-api`（DTO/VO/枚举/契约） | Wire Protocol 类型定义 | 消息格式归一化层 | 不依赖任何运行时 |
| `agent-core`（领域/DB/LLM） | Pi Agent Core + Session Store | AIAgent 核心循环 + Memory + Skills | 不依赖 Spring WebFlux |
| `agent-web`（Controller/Filter/配置） | Gateway Server + Channel Adapter | 多平台 Gateway + CLI | 负责协议适配和鉴权 |

**关键约束**：`agent-core` 不引入 `spring-boot-starter-webflux` 依赖，领域服务通过接口接收上下文，由 `agent-web` 负责从 HTTP 请求中提取并传入。这一约束在所有六篇调研中都被确认。

### 2.2 数据模型 ↔ 开源方案映射

| 当前项目数据模型 | Hermes 方案 | OpenClaw 方案 | 综合建议 |
|----------------|------------|-------------|---------|
| `agent_session` | session + FTS5 state.db | sessions.json + JSONL 转录 | V1 足够，后续增加 `source_type` 预留 |
| `agent_message` | Msg 事件流 | JSONL 行 | V1 足够 |
| `agent_message_part` | ThinkingBlock/TextBlock | tool_result + text | V1 足够 |
| `prompt_tokens`/`completion_tokens` | ContextCompressor 预算 | Context Engine 统计 | V1 足够，为压缩估算服务 |
| `parent_session_id` | Session Lineage | 转录轮换 | 语义需明确（压缩后新会话指向旧会话） |
| `summary_message_id` | 压缩边界标记 | Compaction summary entry | 语义需明确（摘要消息 ID，该 ID 之前的消息已被摘要替代） |

---

## 三、核心设计原则（从调研中提炼）

以下原则从六篇调研的共识中提炼，建议写入 `02-技术方案` 作为显式设计约束：

### 3.1 Prompt 稳定性约束

> **规则**：会话启动后，系统级 prompt（包括 memory、用户偏好、项目上下文等）在会话过程中不发生变更。需要更新时仅在下一轮会话中生效。

**来源**：Hermes 三版调研一致推荐。Hermes 生产实践表明，"quietly defeats prompt caching"是一个常见且昂贵的错误。

**Why**：保护 Anthropic 等支持 prompt caching 的 provider 的缓存前缀，避免每次对话支付全量输入费用。

**How to apply**：V1 的 system prompt 结构简单，此规则暂时不产生实际约束。但在技术方案中预先写入，确保后续实现记忆/上下文注入时不会违反此原则。

### 3.2 Core 层与 Web 层解耦

> **规则**：`agent-core` 不依赖 `spring-boot-starter-webflux`，领域服务通过接口接收上下文，由 `agent-web` 的 Filter/Controller 负责从请求中提取并传入。

**来源**：六篇调研一致推荐。OpenClaw 的 Gateway-first 架构和 Hermes 的平台无关核心（AIAgent）都体现了此原则。

**Why**：核心逻辑与入口协议分离，后续新增入口（消息网关、CLI、API）只需新增模块依赖 `agent-core`，核心逻辑不变。

**How to apply**：在 `04-详细设计` 中明确 `agent-core` 的 Maven 依赖不包含 `spring-boot-starter-webflux`。

### 3.3 可选模块 Check-fn 门控

> **规则**：后续新增可选模块时采用 register + availability check 模式，避免硬耦合影响核心对话链路。

**来源**：Hermes 三版调研推荐。OpenClaw 的插件系统（manifest-first + registry pattern）也体现了此原则。

**Why**：MCP、Honcho、ACP 等可选子系统不应影响核心对话功能。即使某个模块未加载，核心对话仍可正常运行。

**How to apply**：在 `02-技术方案` 的非首版范围后补充说明。

### 3.4 幂等副作用设计

> **规则**：所有产生副作用的操作（发送消息、工具执行）应支持幂等键，服务端维护短期去重缓存。

**来源**：OpenClaw 三版调研一致推荐。OpenClaw Gateway 的 `send` 和 `agent` 方法强制幂等键。

**Why**：防止消息重试导致重复发送或重复执行。后续引入工具执行时，idempotency key 是避免副作用的标配。

**How to apply**：V1 的前端排队机制已部分覆盖（同一会话串行发送），但服务端也应有兜底。后续如果支持消息重试，应在 API 层引入幂等键。

### 3.5 上下文组装独立性

> **规则**：上下文组装应成为独立职责，而不是散落在发送消息接口里。

**来源**：OpenClaw GPT-5.5 版明确提出 `ContextAssembler` 抽象。Hermes 三版调研的压缩策略隐含了此原则。

**Why**：为后续 compaction、检索增强、摘要边界、token 预算估算做准备。

**How to apply**：V1 可在 `04-详细设计` 中将上下文组装从"发送消息接口步骤"提升为 `agent-core/llm/context` 包下的独立职责，V1 行为为 pass-through（取 completed 消息 + 排除 thinking + 用户消息在最后）。

---

## 四、后续演进路线（按优先级排序）

### 4.1 第一优先级：V1 后立即需要

| 编号 | 能力 | 参考来源 | 对应数据/代码改动 |
|------|------|---------|-----------------|
| E1 | Provider 抽象接口 | Hermes 三版一致 | `agent-core/llm/LlmProviderAdapter` 接口，V1 单实现 `OpenAiCompatibleAdapter` |
| E2 | 上下文压缩（preflight） | Hermes 三版一致 | `summary_message_id` 语义明确化 + `ContextAssembler` 抽象 |
| E3 | 停止生成按钮 | Hermes GPT-5.5 | 消息状态增加 `cancelled`/`interrupted`，前端显式按钮 |
| E4 | 会话生命周期管理 | OpenClaw GLM-5.1 | 长期不活跃会话的上下文刷新提示 |
| E5 | 安全声明与日志脱敏 | OpenClaw GPT-5.5 | thinking/trace/error 不暴露给其他用户；日志脱敏 token/apiKey |

### 4.2 第二优先级：Agent 平台化时需要

| 编号 | 能力 | 参考来源 | 关键设计模式 |
|------|------|---------|------------|
| E6 | 分层记忆 | Hermes 三版 + OpenClaw 三版 | L1 用户上下文表 → L2 能力模板 → L3 消息全文检索（PostgreSQL FTS + pg_trgm） |
| E7 | 模型 Failover 链 | OpenClaw GLM-5.1 | 有序 fallback + Provider 内 auth 轮换 + 用户选择严格模式 vs 自动 Failover 静默模式 |
| E8 | Lane Queue | OpenClaw DeepSeek-v4 | per-session 串行队列 + 独立后台 lane + 背压控制 |
| E9 | 工具注册表 | Hermes 三版 + OpenClaw 三版 | Tool / Skill / Plugin 三层 + Manifest-first + Registry pattern |
| E10 | 子代理委派 | Hermes 三版 + OpenClaw DeepSeek-v4 | 深度限制 ≤ 2 + 工具白名单 + 独立迭代预算 |
| E11 | 上下文压缩实现 | Hermes 三版 + OpenClaw 三版 | 压缩前记忆刷新 + 工具调用配对保留 + 可插拔 Provider |
| E12 | 安全扫描层 | Hermes 三版 | 所有注入 prompt 的用户可控内容注入前扫描 |

### 4.3 第三优先级：远期平台能力

| 编号 | 能力 | 参考来源 | 说明 |
|------|------|---------|------|
| E13 | Gateway 控制平面 | OpenClaw 三版 | Agent 核心抽离为独立服务，各入口作为接入层 |
| E14 | 多 Agent 路由 | OpenClaw GLM-5.1 + DeepSeek-v4 | Agent 路由 + 技能白名单 + 独立工作空间 |
| E15 | 沙箱安全 | OpenClaw 三版 + Hermes 三版 | 六层安全模型（工具策略→沙箱→访问控制→绑定验证→网络隔离→提权执行） |
| E16 | 消息转向/Steering | OpenClaw GPT-5.5 + GLM-5.1 | steer/followup/collect/interrupt 四种队列模式 |
| E17 | 主动性/心跳 | OpenClaw DeepSeek-v4 | 定时唤醒 + 检查清单 + 无事静默 |
| E18 | 多模型表面 | OpenClaw GLM-5.1 | 主对话/图像/摘要/生成各自独立配置模型 + fallbacks |
| E19 | 技能门控与白名单 | OpenClaw GLM-5.1 + DeepSeek-v4 | 运行时环境检测自动决定技能可见性 |
| E20 | Active Memory 检索 | OpenClaw DeepSeek-v4 | 阻塞式子代理 + graceful degradation + 工具白名单 |

---

## 五、V1 设计的具体建议更新

### 5.1 `01-通用大模型对话需求.md`

**补充非首版范围**：

- 停止生成
- 历史会话检索
- 工具调用与 Agent 编排
- 跨会话长期记忆
- 上下文压缩与自动摘要
- 真实账号与生产鉴权
- 定时自主任务（Heartbeat 式定期检查）
- 多入口接入（API / mobile / WebSocket）

### 5.2 `02-通用大模型对话技术方案.md`

**新增"架构设计原则"小节**，记录从调研中提炼的五条约束：

1. 会话内 Prompt 不变性：会话启动后系统级 prompt 不修改，变更新会话生效
2. Core 层与 Web 层解耦：`agent-core` 不依赖 Spring WebFlux
3. 可选模块 check-fn 门控：后续引入新模块时采用 register + availability check
4. 幂等副作用设计：后续副作用操作支持幂等键
5. 上下文组装独立性：上下文组装为独立职责

**新增"后续演进参考"小节**，按优先级记录 E1-E20 的演进路线。

**补充安全设计说明**：

- V1 的 `X-Token` 非空校验不是生产鉴权
- thinking、trace、errorMessage 不应暴露给其他用户或公共渠道
- 日志默认脱敏 token、apiKey、请求头和模型原始异常

### 5.3 `04-通用大模型对话详细设计.md`

**补充 `agent-core/llm` 包设计**：

- `LlmProviderAdapter` 接口草案（V1 单实现 `OpenAiCompatibleAdapter`）
- `ContextAssembler` 包职责（V1 行为：取 completed 消息 + 排除 thinking + 用户消息在最后）
- `ContextBudgetEstimator` 预留（token 估算，V1 可不实现）

**补充消息表字段语义**：

- `summary_message_id`：指向一条"摘要消息"，该消息之前的历史消息在上下文组装时已被摘要替代
- `parent_session_id`：压缩后新会话指向旧会话的 lineage 关系

**补充会话锁语义**：

- 当前 `Semaphore` `tryAcquire` 策略对应"单 lane 串行"模式
- 记录后续升级为 Lane Queue 的条件（引入后台任务、Agent 工具调用时）

**补充排队边界**：

- 排队上限 `cap`（建议 3-5 条）
- 溢出策略：拒绝新消息并提示
- 排队等待超过阈值记录日志

### 5.4 `03-原型设计/chat-prototype.html`

当前不建议更新。停止生成按钮、搜索入口、工具面板都不是 V1 必做，原型保持当前 V1 范围更稳。

---

## 六、不适合引入的方向

以下能力在六篇调研中一致认为不适合当前项目 V1 或后续近期引入：

| 方向 | 不适合的原因 |
|------|------------|
| 自动技能创建与自改进 | 审核/安全/稳定性风险，Hermes 的闭环学习需要成熟的安全基础设施 |
| 多平台 Gateway（22+ 消息渠道） | V1 明确只做 Web，OpenClaw 的多渠道编排能力过早 |
| Cron 调度、RL 训练环境 | MLOps 专用能力，与对话产品距离太远 |
| Canvas / A2UI 可视化工作空间 | Agent 高级能力，V1 不涉及 |
| 梦境系统 Dreaming | 后台整合机制，V1 阶段过早 |
| Voice Wake / Talk Mode 语音交互 | V1 明确不做 |
| 本地守护进程 + launchd/systemd | 与 Spring Boot 部署模型不匹配 |
| 多个外部记忆 Provider 并存 | 工具 schema 膨胀导致模型混淆 |
| Python/TypeScript 技术栈 | 与 Java 技术栈不匹配 |

---

## 七、两个项目的哲学分野与项目定位启示

### 7.1 核心分野

> "OpenClaw treats an agent as a system to be orchestrated. Hermes treats an agent as a mind to be developed." — DeepSeek-v4-pro 版调研

| 决策维度 | OpenClaw（编排哲学） | Hermes（认知哲学） |
|---------|-------------------|-------------------|
| Agent 如何变得更好？ | 社区贡献新 Skills / 配置优化 | 自动从经验中创建和改进 Skills |
| 能力广度 vs 深度 | 广度优先（22+ 通道，ClawHub 市场） | 深度优先（6 核心通道，自改进回路） |
| 可预测性 | 高（行为由配置文件显式决定） | 中—高（自动改进引入变化） |
| Operator 介入 | 高（配置文件显式编辑） | 低—中（Agent 自动管理 memory/skills） |
| 多 Agent 编排 | 原生支持（Gateway 级路由） | 子任务委派（单 Agent 深度） |
| 适用场景 | 多通道团队协作、企业治理 | 个人深度助手、长期工作流自动化 |

### 7.2 对项目定位的启示

当前项目 V1 是 Web 端通用大模型对话——既不需要 OpenClaw 的 22+ 通道编排，也不需要 Hermes 的自改进学习回路。但项目名称（`htam-agent-platform`）暗示其长期愿景是一个 Agent 平台。

**平台化路径建议**：

- **如果走 OpenClaw 路线**：Gateway-first，聚焦多 Agent 编排、通道集成、工具策略管理、团队权限。适合企业级/多租户场景。
- **如果走 Hermes 路线**：Agent-first，聚焦单个 Agent 的深度能力、自改进技能、长期记忆、上下文压缩。适合个人/单用户深度助手场景。
- **混合路径**：以 OpenClaw 式的 Gateway 编排层作为平台骨架，以 Hermes 式的自改进能力作为 Agent 差异化能力。DeepSeek-v4-pro 版调研提到 Gormes 项目（Hermes 的 Go 移植）正在尝试这种混合。

**建议**：在技术方案中明确当前项目定位。当前 V1 的 Web 端通用对话是两者的共同起点，V1 阶段不需要做此选择，但应在演进路线中保留两种可能性。

---

## 八、调研质量评估与差异

### 8.1 各版调研的独特贡献

| 调研版本 | 独特贡献（其他版本未覆盖或深度不足的） |
|---------|-------------------------------------|
| Hermes GPT-5.5 | 首次提出 `LlmProviderAdapter` 抽象、`summary_message_id` 字段语义、会话 Lineage 概念 |
| Hermes DeepSeek-v4 | 最深入分析五条设计原则、三层记忆 + 数据模型演进路径 + GBrain 集成、Java 接口草案、子代理五个安全约束 |
| Hermes GLM-5.1 | v0.13.0 最新版覆盖、闭环学习范式详细分析、记忆双文件存储模型 + 容量管理 + 安全扫描、SKILL.md 格式规范 + 渐进式披露 + 条件激活 |
| OpenClaw GPT-5.5 | ContextAssembler 抽象、Context Engine 生命周期（ingest/assemble/compact/afterTurn）、Retry Policy、Queue cap/overflow、安全声明细化 |
| OpenClaw DeepSeek-v4 | Gateway-first 架构深度分析、四层架构模型、Lane Queue 详细设计、Active Memory 检索模式、子代理工具策略 Pipeline、Heartbeat 系统、OpenClaw vs Hermes 哲学分野 |
| OpenClaw GLM-5.1 | Gateway 线协议 + 核心不变量、会话生命周期与重置、模型 Failover 链 + 多表面 + 白名单、分层沙箱六层安全模型、可插拔压缩 Provider + 转录轮换、Dreaming 梦境系统 |

### 8.2 调研盲区

以下主题在六篇调研中均未深入覆盖，值得后续补研：

| 盲区 | 说明 |
|------|------|
| 前端状态管理 | 两者的前端（Hermes WebUI / OpenClaw WebChat）的组件化、状态流、离线处理均未深入 |
| 多租户隔离 | OpenClaw 明确是单用户设计，Hermes 的多用户隔离依赖 DM 配对。企业场景的多租户隔离需另行研究 |
| 可观测性 | 分布式追踪、指标采集、告警策略等生产运维能力均未覆盖 |
| 测试策略 | 两者均有测试套件，但测试分层、集成测试策略、LLM 输出的确定性测试均未深入 |
| 国际化 | OpenClaw 有 `locales/` 目录，但 i18n 策略未详细覆盖 |

---

## 九、综合判断

### 9.1 一句话总结

> 调研覆盖了 Agent 平台设计的完整维度——Hermes 贡献了"Agent 如何深度学习和进化"的蓝图，OpenClaw 贡献了"Agent 如何被编排和治理"的蓝图。当前 V1 是两者的共同起点，后续平台化时应按优先级逐步吸收两者精华。

### 9.2 对 V1 的核心价值

六篇调研对 V1 的直接指导不是具体实现细节，而是以下**结构性设计约束**：

1. **Prompt 稳定性**——写入技术方案，即使 V1 的 system prompt 结构简单
2. **Core 与 Web 解耦**——在详细设计中明确 Maven 依赖方向
3. **幂等副作用**——后续 API 设计的标配
4. **上下文组装独立性**——为后续压缩/检索预留架构空间
5. **安全声明**——V1 已有数据（thinking、对话），需明确防护边界

### 9.3 对后续版本的核心价值

Hermes + OpenClaw 的组合覆盖了 Agent 平台的完整维度：

```
          自改进深度
              ▲
              │  Hermes 擅长
              │  (闭环学习, 三层记忆, 技能自创建,
              │   上下文压缩, Provider 抽象)
              │
              │    当前 V1
              │    (Web 对话)
              │
              │                  OpenClaw 擅长
              │                  (Gateway 编排, Lane Queue,
              │                   多 Agent 路由, 沙箱安全,
              │                   模型 Failover, 技能门控)
              └─────────────────────────────────────► 编排广度
```

后续平台化时，按 E1-E20 优先级逐步引入，V1 保持交付边界不变。

---

## 十、参考来源

### 调研报告

- `docs/v1-通用大模型对话/调研/HermesAgent-v1-GPT5.5.md` — Hermes Agent 设计调研（GPT-5.5）
- `docs/v1-通用大模型对话/调研/HermesAgent-v1-dpsk-v4-pro.md` — Hermes Agent 设计调研（DeepSeek-v4-pro）
- `docs/v1-通用大模型对话/调研/HermesAgent-v1-glm.md` — Hermes Agent 设计调研（GLM-5.1）
- `docs/v1-通用大模型对话/调研/openclaw-v1-gpt5.5.md` — OpenClaw 设计调研（GPT-5.5）
- `docs/v1-通用大模型对话/调研/Openclaw-v1-dpsk-v4.md` — OpenClaw 设计调研（DeepSeek-v4）
- `docs/v1-通用大模型对话/调研/OpenClaw-v1-glm.md` — OpenClaw 设计调研（GLM-5.1）

### 开源项目

- Hermes Agent：https://github.com/NousResearch/hermes-agent
- OpenClaw：https://github.com/openclaw/openclaw

### 设计文档

- `docs/v1-通用大模型对话/01-通用大模型对话需求.md`
- `docs/v1-通用大模型对话/02-通用大模型对话技术方案.md`
- `docs/v1-通用大模型对话/03-原型设计/chat-prototype.html`
- `docs/v1-通用大模型对话/04-通用大模型对话详细设计.md`
