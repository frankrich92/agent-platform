# Hermes Agent 设计调研

> 调研版本：v1
> 调研模型：GPT-5.5
> 调研日期：2026-05-14
> 调研对象：NousResearch Hermes Agent
> 对照范围：通用大模型对话 01/02/03/04 文档

## 1. 背景与定位

Hermes Agent 是 NousResearch 的长期运行自主 Agent runtime，重点能力包括多模型 provider、长期记忆、会话存储、工具运行时、上下文压缩、检查点回滚、多入口接入等。

当前项目第一版定位是 Web 端通用大模型对话，目标是稳定完成基础会话、流式回复、thinking 展示、历史续聊和用户隔离。因此 Hermes Agent 的设计不能直接照搬，但其中部分架构思路可以作为后续演进参考。

## 2. 可借鉴设计

### 2.1 模型 Provider 抽象

Hermes 支持多 provider，并通过运行时解析 provider 和 model 来避免供应商锁定。

当前项目已有 `baseUrl`、`apiKey`、`modelName`、`thinkingBudget` 等配置，但模型适配仍偏单一。建议后续在 `02-技术方案` 和 `04-详细设计` 中补充 `LlmProviderAdapter` 抽象：

- 第一版只实现 OpenAI-compatible 调用。
- 配置中预留 `provider`、`modelName`、`apiMode`、`baseUrl`、`apiKey`。
- 后续可以扩展不同厂商、不同 thinking/reasoning 字段格式和不同流式事件结构。

适用程度：高。该设计不会显著扩大 V1 范围，但能减少后续替换模型供应商时的返工。

### 2.2 上下文压缩与 Session Lineage

Hermes 在上下文过大时压缩中间消息，保留最近消息，并维护 session lineage。

当前项目数据表中已经预留：

- `parent_session_id`
- `summary_message_id`
- `prompt_tokens`
- `completion_tokens`

建议在后续演进中明确：

- 当上下文超过阈值时，生成摘要消息或摘要边界。
- 新会话可以通过 `parent_session_id` 追踪来源。
- `summary_message_id` 记录当前上下文压缩边界。
- 上下文组装时使用摘要 + 最近 N 轮完整消息。

适用程度：高。当前字段已经具备承载能力，应把演进规则写清楚，避免字段长期语义不明。

### 2.3 跨会话检索预留

Hermes 使用 SQLite FTS5 支持历史会话检索，并针对 CJK/substring 等场景做检索增强。

当前项目第一版明确不做会话搜索，原型中搜索框也已经调整为禁用占位。但历史会话检索是对话产品的高频后续能力，建议作为后续演进写入：

- PostgreSQL full-text search 用于基础关键词检索。
- `pg_trgm` 可用于中文、模糊匹配和 substring 场景。
- 如后续需要语义召回，可增加 embedding 表或外部向量库。
- V1 不实现搜索接口，避免扩大首版范围。

适用程度：中高。适合进入技术方案的后续演进，不适合进入 V1 必做项。

### 2.4 可中断生成

Hermes 支持 interrupt/cancellation。

当前详细设计已经补充：

- 前端 `AbortController`
- 切换会话时 abort 当前流
- 后端 WebFlux `doOnCancel()`
- 客户端断开时取消模型调用、更新消息状态并释放会话锁

建议进一步把“停止生成”作为明确后续能力：

- V1 可以只支持内部 abort 和切换会话清理。
- 后续版本可以在 UI 上提供“停止生成”按钮。
- 服务端需要能标记消息为 `failed`、`cancelled` 或 `interrupted`。

适用程度：高。中断能力与流式对话稳定性直接相关，但 UI 按钮可以放到后续版本。

### 2.5 工具与能力注册模型

Hermes 的 tools runtime 采用注册表、toolset、dispatch、hook 等结构。

当前项目 V1 不包含工具调用和 Agent 编排，但长期看平台级 Agent 能力会需要类似机制。建议作为后续方向预留：

- 工具注册表：定义工具元数据、入参 schema、权限范围。
- toolset：按 Agent、应用或用户组合可用工具。
- dispatch：统一执行工具调用，隔离模型输出和真实执行。
- hook：记录工具调用前后日志、审计、权限校验、错误转换。

适用程度：中。适合写入非首版范围或平台演进，不应进入通用对话 V1 实现。

## 3. 暂不建议纳入 V1 的能力

### 3.1 自动创建或自改进 Skills

Hermes 支持 skills 形态的能力沉淀。该方向适合自主 Agent，但会引入审核、版本、安全、可解释性问题。

当前项目第一版只做通用对话，不建议引入自动 skill 创建或自动改写能力。

### 3.2 多平台 Gateway

Hermes 支持 CLI、Telegram、Slack 等入口。当前项目第一版只面向 Web 端。

建议只保留前后端分离和 API 契约清晰，不在 V1 增加多入口设计。

### 3.3 文件 Checkpoint / Rollback

Hermes 的检查点与回滚适合代码 Agent 或会修改文件的工具型 Agent。

当前项目是纯对话系统，消息已经持久化保存，不需要文件级 checkpoint。后续如果引入工具执行或代码修改类 Agent，再考虑该能力。

### 3.4 Cron、子 Agent 并行、工具执行沙箱

这些能力属于更完整的 Agent 平台能力，会显著扩大系统边界。

当前 V1 不建议纳入，只适合放入平台级 Agent 编排的远期路线。

## 4. 建议更新到现有文档的位置

### 4.1 `01-通用大模型对话需求.md`

建议只补后续方向，不进入 V1 必做：

- 停止生成。
- 历史会话检索。
- 工具调用。
- 长期记忆。
- 上下文压缩。

### 4.2 `02-通用大模型对话技术方案.md`

建议新增“Hermes Agent 可借鉴设计”或“后续演进参考”小节：

- `LlmProviderAdapter` 模型 provider 抽象。
- 上下文压缩与 session lineage。
- PostgreSQL full-text / `pg_trgm` / 向量检索演进。
- 工具注册表、toolset、dispatch、hook 作为 Agent 平台后续方向。

### 4.3 `04-通用大模型对话详细设计.md`

建议补充：

- `LlmProviderAdapter` 包职责和接口草案。
- 上下文压缩字段的未来使用规则。
- 停止生成接口或前端按钮的后续预留。
- 工具运行时作为非首版模块，不进入当前工程任务拆分。

### 4.4 `03-原型设计/chat-prototype.html`

当前不建议更新。停止生成按钮、搜索入口、工具面板都不是 V1 必做，原型保持当前 V1 范围更稳。

## 5. 综合判断

Hermes Agent 的价值不在于具体 UI 或数据表，而在于几个长期演进方向：

- 模型 provider 解耦。
- 会话 lineage 与上下文压缩。
- 可检索的长期记忆。
- 可中断、可恢复、可审计的运行时。
- 工具注册与统一 dispatch。

对当前项目而言，最适合优先吸收的是模型 provider 抽象、上下文压缩语义、停止生成预留和检索演进说明。工具 runtime、skills、自主任务、多入口接入应保留为平台后续能力，避免干扰第一版通用对话交付。

## 6. 参考来源

- Hermes Agent GitHub: https://github.com/NousResearch/hermes-agent
- Hermes features overview: https://hermes-agent.nousresearch.com/docs/user-guide/features/overview/
- Hermes memory docs: https://hermes-agent.nousresearch.com/docs/user-guide/features/memory/
- Hermes agent loop: https://hermes-agent.nousresearch.com/docs/developer-guide/agent-loop/
- Hermes session storage: https://hermes-agent.nousresearch.com/docs/developer-guide/session-storage/
- Hermes tools runtime: https://hermes-agent.nousresearch.com/docs/developer-guide/tools-runtime
- Hermes checkpoints and rollback: https://hermes-agent.nousresearch.com/docs/user-guide/checkpoints-and-rollback/
