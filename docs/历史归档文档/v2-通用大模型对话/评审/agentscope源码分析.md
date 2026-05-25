# AgentScope Java 源码分析与平台调整建议

> 注意：本文档为历史阶段文档，仅作为需求、调研和演进参考。
> 当前 Agent 中台架构与后续开发以 `docs/v3-agent中台/` 为准。
> AI agent 执行开发任务前必须先阅读 `docs/README.md` 和 V3 架构文档。

本文基于 `/mnt/e/project/1_source_analysis/agentscope/docs` 下文档整理：

- `/mnt/e/project/1_source_analysis/agentscope/docs/agentscope-java-static-architecture.md`
- `/mnt/e/project/1_source_analysis/agentscope/docs/agentscope-java-examples-scenarios.md`
- `/mnt/e/project/1_source_analysis/agentscope/docs/agentscope-java-platform-architecture.md`
- `/mnt/e/project/1_source_analysis/agentscope/docs/agentscope-java-agent-lifecycle.md`

结论：当前平台已经有 Agent 定义、授权、记忆、Run/Span、SSE、消息持久化这些平台控制面，但 Agent 执行面还接得比较浅。很多能力不应该在平台侧自研，应该继续交给 `agentscope-java`，平台只做配置装配、权限裁剪、事件映射、持久化和审计。

## 可以继续交给 AgentScope Java 的能力

### 1. ReAct 推理与工具执行循环

AgentScope 已有 `ReActAgent` 的 reasoning、acting、`maxIters`、summary 主循环。平台不应自研工具选择、循环调度、工具结果回写逻辑。

当前 `AgentScopeReActRuntime` 已经调用 `agent.stream(...)`，但仍是空 `Toolkit`、空 `SkillBox`、`InMemoryMemory`，还没有真正把平台能力装进去。

### 2. 工具、MCP、Skill 执行

AgentScope 已覆盖 `Toolkit`、`@Tool`、工具组、MCP tool、`SkillBox`、`AgentSkill`。平台侧已有 `CapabilityRegistry` 做授权能力过滤，但它目前只返回能力摘要，没有把 MCP/Skill 实例注册到 AgentScope `Toolkit/SkillBox`。

下一步应做能力装配层，而不是平台侧直接执行工具。

### 3. Agent 流式事件

AgentScope 已有 `StreamOptions`、`EventType.REASONING`、`TOOL_RESULT`、`SUMMARY` 等事件。当前 `AgentScopeEventMapper` 已经做了基本映射，可以继续扩展到 tool use、acting chunk、summary、stop/interruption 等事件，而不是另起一套执行事件协议。

### 4. Session / Memory 的运行态承载

数据库仍应是权威历史和长期记忆，但单次运行内可以用 AgentScope `Memory` 承载上下文。

当前 `ContextAssembler` 是手工拼 `LlmMessage`，记忆也是直接注入系统提示词。更合理的是：平台从 DB 读取授权范围内的历史和记忆，转换成 AgentScope `Memory` 或 `Msg`，交给 `ReActAgent` 运行；运行结束后平台再落库。

### 5. RAG、PlanNotebook、结构化输出、HITL、中断恢复

AgentScope 示例已覆盖 RAG、PlanNotebook、structured output、Human-in-the-loop、interruption。平台后续如果做知识库检索、计划管理、工具审批、暂停/恢复，优先接 AgentScope 的 Hook / Tool / PlanNotebook / Interruption，不要在平台侧重新实现 Agent 编排。

### 6. 多 Agent、路由、Supervisor、Subagent、A2A

AgentScope 的 `multiagent-patterns`、A2A、`boba-tea-shop` 示例已经覆盖 routing、supervisor、subagent、workflow、A2A/Nacos。

平台后续做 Agent 授权分发、多 Agent 协作时，应把 AgentScope 作为执行内核，平台只管理 Agent 定义、用户授权、能力白名单、调用审计和资源预算。

## 当前 Agent 侧最值得优化的功能

### 1. 把 `AgentRuntimeOptions` 升级为真正的 `AgentRunRequest`

当前 `AgentRuntimeOptions` 只有模型配置和 system prompt，无法传入 `agentId`、`userCode`、capabilities、memory、`runId`、trace context、安全策略。

建议按 V2 设计改为运行请求对象，让 `AgentScopeReActRuntime` 能拿到完整装配上下文。

### 2. 在 `ChatService` 中接入能力裁剪结果

当前 `ChatService` 只读取历史和记忆，然后直接调用 runtime。缺少 `CapabilityRegistry.enabledCapabilities(...)` 结果传递。

建议在进入 runtime 前完成：

- Agent 白名单
- 用户授权
- 环境可用性
- 风险等级
- 工具脱敏策略裁剪

### 3. 做 `AgentScopeCapabilityAssembler`

新增装配层，把平台 `agent_capability` 转成 AgentScope 对象：

- `type=mcp` -> MCP tool 注册到 `Toolkit`
- `type=skill` -> `AgentSkill` 注册到 `SkillBox`
- `type=builtin` -> 受控 `@Tool` 或 `AgentTool`
- 高风险能力默认不装配

### 4. 普通聊天路径收敛

当前有 `OpenAiCompatibleAdapter` 手写 SSE，也有 `AgentScopeChatRuntime` 使用 `OpenAIChatModel`。如果 AgentScope 模型流式能力稳定，建议普通对话也优先走 `AgentScopeChatRuntime`，减少平台侧手写 OpenAI SSE parser 的维护面。

`LlmProviderAdapter` 保留为底层模型协议扩展点，不承载 Agent 语义。

### 5. 强化工具观测

当前工具事件只记录简单 `tool_call` span。建议补齐：

- tool name
- capability id
- MCP server / skill id
- 脱敏后的输入参数
- 输出摘要
- duration / error / timeout
- 是否经过 HITL 审批

### 6. 引入 Hook 作为平台治理点

AgentScope Hook 可以承载平台策略：运行前注入用户记忆、工具调用前做权限二次校验、工具结果脱敏、异常归一化、Run/Span 记录。这样比在 `ChatService` 里堆执行分支更清晰。

### 7. 上下文压缩优先看 AgentScope `AutoContextMemory`

当前 `ContextAssembler` 用字符数裁剪，够用但粗糙。后续长会话建议优先评估 AgentScope 的自动上下文压缩能力；平台仍保留 DB 原始消息和摘要边界，只把压缩后的运行态上下文交给 AgentScope。

### 8. 引入 Agent 生命周期服务

AgentScope 平台蓝图建议不要长期复用同一个 `ReActAgent` 实例。推荐模型是每次请求按 Agent 配置创建新实例，按 `sessionId` load 状态，执行 `call()` 或 `stream()`，结束、取消、异常或 HITL 暂停时 save 状态。

当前平台还没有 `RunningAgentRegistry`、`LifecycleService` 或自定义 AgentScope `Session`。后续支持中断、工具确认、优雅停机和恢复时，应优先按 AgentScope 生命周期模型实现，而不是让 `ChatService` 直接持有运行中 Agent 状态。

### 9. 使用 `RuntimeContext` 传递平台上下文

AgentScope 的 `RuntimeContext` 适合传递 `userId`、`sessionId`、`traceId`、权限对象和业务对象，并能被工具和 Hook 使用。平台运行请求应把 `userCode`、`agentId`、`runId`、能力数量、记忆数量等传入 RuntimeContext，后续工具执行和审计 Hook 从同一上下文读取。

平台已将 AgentScope Core 依赖升级到 `1.1.0-SNAPSHOT`，并在 `AgentScopeReActRuntime` 中通过 `agent.stream(..., RuntimeContext)` 传递运行上下文。当前平台只使用 AgentScope Core API，暂不依赖 `agentscope-spring-boot-starter`，避免为了本地快照拉起 all-in-one 和扩展模块编译。

## 本轮已落地调整

- 平台依赖已升级到 `io.agentscope:agentscope-core:1.1.0-SNAPSHOT`。
- `AgentRuntime` 已支持 `AgentRunRequest`，运行请求中包含 `userCode`、`agentId`、`runId`、消息、记忆、能力列表和模型运行参数。
- `ChatService` 已把 `CapabilityRegistry.enabledCapabilities(...)` 的结果传入 runtime，并在 `model_call` span 记录 `capabilityCount`。
- `AgentScopeReActRuntime` 已通过 `RuntimeContext` 传入 `AgentRunRequest`、`agentId`、`runId`、能力数量和记忆数量，为后续 Hook / Tool 注入统一上下文。
- 已新增 `AgentScopeCapabilityAssembler`，当前支持把低风险内置 `cap_builtin_datetime` 装配为 AgentScope `Toolkit` 中的 `get_current_datetime` 工具；MCP/Skill 因平台尚无配置结构，暂只保留授权但未挂载的清晰边界。
- 能力默认自动装配增加风险门禁：高风险能力不会自动挂载到 AgentScope 执行面。
- 工具调用 span 已补充 `capabilityId`、`capabilityType`、`riskLevel` 和输出摘要，便于后续审计与观测。
- 已补充 `AgentScopeCapabilityAssemblerTest`、`CapabilityRegistryTest` 和 `ChatServiceTest` 覆盖能力裁剪、工具挂载和运行请求传递。

## 总体建议

下一阶段不要扩写平台侧 Agent 执行引擎，重点做 `CapabilityRegistry -> Toolkit/SkillBox/MCP/Memory/Hook` 的装配层。

平台负责“谁能用、能用什么、怎么审计、怎么落库”，AgentScope 负责“怎么推理、怎么调用工具、怎么组织 Agent 流程”。
