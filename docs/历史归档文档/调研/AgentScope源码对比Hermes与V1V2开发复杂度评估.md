# AgentScope 源码对比 Hermes 与 V1/V2 开发复杂度评估

## 结论

基于 `source/fork_source/agentscope-java` 和 `source/fork_source/agentscope-runtime-java` 源码对比，AgentScope Java + AgentScope Runtime Java 已经覆盖 V1 / V2 所需的核心执行底座。当前项目的大部分工作不是从零实现 Agent runtime，而是把 AgentScope 能力适配到 `htam-agent-platform` 的用户、Agent、权限、会话、消息、Run / Span 和 SSE 协议中。

真正复杂的是补齐 Hermes Agent 已经产品化较多的高阶能力，例如 Skills Hub、自动技能创建 / 自修复、完整工具治理 UI、跨会话检索、生产级沙箱策略和可观测报表。

总体判断：

```text
V1 / V2 必需功能
= AgentScope 路线大多是简单到中等适配

Hermes 亮点能力
= AgentScope 有底座，但需要中等到复杂的平台产品化开发
```

## 源码观察

### AgentScope Java 已具备的关键能力

`agentscope-java` 的 `agentscope-core` 已包含：

- `ReActAgent`：ReAct reasoning / acting 循环、流式、Hook、HITL、结构化输出。
- `Toolkit`：工具注册、工具分组、MCP client 管理、tool schema、执行器。
- `SkillBox`：技能注册、技能提示词、动态加载、技能工具组启停。
- `McpClientBuilder` / `McpClientWrapper`：支持 stdio、SSE、streamable HTTP MCP transport。
- `Memory` / `Session`：Agent 内部记忆和会话能力。
- `RuntimeContext` / `StreamOptions` / `Event`：运行上下文、流式事件和事件过滤。
- Hook / trace：支持执行过程拦截和记录。

当前平台也已经开始接入这些能力：

- `AgentScopeReActRuntime` 已基于 `ReActAgent`、`Toolkit`、`SkillBox` 和 `OpenAIChatModel` 实现运行路径。
- `AgentScopeCapabilityAssembler` 已能把平台能力装配为 MCP、Skill 或内置工具。

### AgentScope Runtime Java 已具备的关键能力

`agentscope-runtime-java` 已包含：

- `AgentApp`：Agent-as-API / Web 应用部署入口。
- `AgentScopeAgentHandler`：AgentScope Java 适配器基类。
- `MemoryService` / `SessionHistoryService` / `StateService`：运行时状态、会话历史和长期记忆服务接口。
- `SandboxService`：沙箱生命周期、容器创建、远程 sandbox、TTL 清理。
- `BrowserSandbox`、`RunShellCommandTool`、`RunPythonTool` 等沙箱工具。
- A2A / runtime starter / examples：用于把 Agent 以服务方式部署和暴露。

这说明 AgentScope Runtime Java 更适合在后续需要 Agent-as-API、沙箱工具执行和独立运行时服务时引入；V2 当前内嵌 `AgentRuntime` 路径已能满足受控 Agent 对话验证。

## 简单开发：主要是适配

| 功能 | 评估 | 说明 |
| --- | --- | --- |
| V1 普通对话 | 简单 | 当前已有 `OpenAiChatRuntime` / `AgentScopeChatRuntime` 路径，V1 主要是 SSE、消息落库、上下文组装。 |
| `AgentRuntime` 抽象 | 简单 | V2 文档已要求统一普通模型和 ReAct 路径，当前代码也已有 `AgentRuntime` 框架。 |
| ReAct Agent 对话 | 简单到中等 | `ReActAgent` 已内置 reasoning / acting 循环、流式、Hook、HITL 等能力；平台已有 `AgentScopeReActRuntime`。 |
| MCP 接入 | 简单到中等 | AgentScope 已支持 stdio、SSE、streamable HTTP；平台侧已有 `AgentScopeCapabilityAssembler` 装配逻辑。 |
| Skills 加载 | 简单到中等 | `SkillBox` 已支持技能注册、动态加载和技能工具组启停；平台只需把 DB 中的 skill config 转为 `AgentSkill`。 |
| 工具白名单 / 工具分组 | 中等偏简单 | `Toolkit` 已有 tool group、MCP client、schema 和执行器；平台重点是权限裁剪和配置管理。 |
| 基础 Run 事件映射 | 中等偏简单 | AgentScope 有 `Event` / `StreamOptions`；当前平台已有 event mapper，只需补齐 tool call、span、error 映射。 |
| 用户管理、Agent 授权 | 简单到中等 | 这是平台业务，不依赖 AgentScope。V2 已明确 `AgentAccessService`、`AgentDefinitionService` 等服务。 |

## 中等开发：有底座但需要平台工程

| 功能 | 复杂原因 |
| --- | --- |
| 用户隔离记忆 | AgentScope 有 Memory，Runtime 有 MemoryService，但 V2 要按 `userCode + agentId` 隔离，DB 是权威上下文，需要平台自己的 `AgentMemoryService` 与注入策略。 |
| Run / Span 可观测 | AgentScope 有 Hook / Jsonl trace，Runtime 可观测能力还在发展中；V2 需要产品可查询的 `agent_run` / `agent_run_span`，要平台自己建模落库。 |
| MCP / Skills 安全策略 | AgentScope 能挂载能力，但“Agent 白名单 + 用户授权 + 风险等级 + 环境门控 + 脱敏审计”要平台实现。 |
| 沙箱工具执行 | Runtime Java 有 `SandboxService`、Docker / 远程容器、BrowserSandbox、RunShell / RunPython 工具；但接入平台权限、目录隔离、资源限制、审计是中等工程。 |
| Agent-as-API / 独立 runtime 服务 | Runtime Java 有 `AgentApp` 和 adapter 形态；但当前平台内嵌 runtime 已够用，是否拆服务取决于部署目标。 |
| 多 Provider / 模型选择 | AgentScope 有不同模型实现和 OpenAI-compatible 适配，但平台仍要做 provider registry、模型列表、会话级选择和错误处理。 |
| 上下文预算保护 | AgentScope 提供消息和运行时能力，但 V2 要按平台 DB 历史、thinking 排除、token 估算、裁剪记录实现。 |

## 复杂开发：接近 Hermes 产品能力

| 功能 | 复杂原因 |
| --- | --- |
| Hermes 式 Skills Hub | AgentScope 有 `SkillBox` 和 repository 扩展，但没有完整技能市场、安装 UI、评分、审核、版本治理体验。 |
| 自动技能创建 / 自修复 | AgentScope 可加载 skill，但 Hermes 的闭环学习、自动沉淀经验、自动修补技能，需要安全审核、版本回滚、提示词注入扫描、用户确认流程。 |
| 长期跨会话检索 / session search | AgentScope 有 Memory / RAG 扩展，但 Hermes 式跨 session recall、全文检索、摘要压缩和用户画像，需要平台自行产品化。 |
| 完整工具治理中心 | 包括工具目录、风险分级、审批流、参数脱敏、执行回放、失败统计和权限报表。这不是 AgentScope 框架本身职责。 |
| 多 Agent 委派治理 | AgentScope 有 sub-agent / tool 相关底座，但委派深度、预算、工具继承、子任务审计需要复杂平台治理。 |
| 生产级沙箱策略 | Runtime Java 有沙箱底座，但企业生产级还需要镜像治理、网络隔离、文件系统策略、资源配额、生命周期、审计和故障恢复。 |
| Hermes 式 Cron / 长任务体验 | AgentScope Runtime 有部署和服务化底座，但定时任务、后台运行、恢复、通知和前端任务中心需要平台自己做。 |

## 与 Hermes Agent 的功能差距

Hermes 更像一个已经产品化较多的个人 / 团队 Agent runtime：

- MCP、Skills、工具集、记忆、Cron、子 Agent、API Server、Profiles、工具进度事件更开箱。
- Skills Hub、自动技能创建、自动技能修补和闭环学习是 Hermes 的明显差异化。
- 对快速构建强能力 Agent 助手，Hermes 的短期产品效率更高。

AgentScope 路线更像 Java 生态里的执行内核 + runtime 基础设施：

- 核心执行能力完整。
- Java / Spring Boot 集成更自然。
- 更适合当前项目的企业级平台控制面：用户、Agent 授权、用户隔离、Run / Span、审计和数据库主数据。
- 高阶产品体验需要平台自己补齐。

## 对 V1 / V2 的建议

### V1

V1 不建议引入 AgentScope Runtime Java 或 Hermes。继续保持普通对话链路轻量，重点是：

- 真正流式输出。
- 消息落库。
- thinking / text parts 展示。
- 会话锁和取消。
- 错误状态。

### V2

V2 建议继续走 `AgentRuntime + AgentScopeReActRuntime` 主线：

1. 先跑通受控 Agent 对话。
2. 再跑通 MCP / Skills 的最小闭环。
3. Run / Span 从第一条工具链路开始建模。
4. MCP / Skills 默认关闭，由 Agent 白名单、用户授权和风险策略逐项开启。
5. 沙箱工具先作为高风险能力单独 PoC，不和基础 Agent 对话一起扩大范围。

### 暂缓能力

以下能力建议暂缓，不进入 V2 主线：

- 自动技能创建。
- 自动技能自修复。
- Skills Hub。
- 多 Agent 深度委派。
- Cron / 长任务中心。
- 完整工具治理后台。
- 企业生产级沙箱治理。

这些能力不是 AgentScope 不支持，而是需要较完整的平台治理、安全、审核和运营体系。

## 最终判断

对当前 `htam-agent-platform` 来说：

- V1 / V2 必需功能：AgentScope 路线基本是简单 / 中等适配。
- Hermes 亮点功能：AgentScope 有部分底座，但大多是中等到复杂开发。
- 平台治理能力：无论使用 Hermes 还是 AgentScope，都必须由平台自己掌握。

因此，建议 V2 优先把 `AgentRuntime`、`AgentScopeReActRuntime`、`CapabilityRegistry`、`RunSpanRecorder`、`AgentAccessService` 和 `AgentMemoryService` 做扎实。不要过早追 Hermes 的自动成长能力，否则范围会从“通用 Agent 对话平台”快速膨胀为完整 Agent 产品。

## 参考源码

- `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/ReActAgent.java`
- `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/tool/Toolkit.java`
- `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/skill/SkillBox.java`
- `source/fork_source/agentscope-java/agentscope-core/src/main/java/io/agentscope/core/tool/mcp/McpClientBuilder.java`
- `source/fork_source/agentscope-runtime-java/web/src/main/java/io/agentscope/runtime/app/AgentApp.java`
- `source/fork_source/agentscope-runtime-java/engine-core/src/main/java/io/agentscope/runtime/adapters/agentscope/AgentScopeAgentHandler.java`
- `source/fork_source/agentscope-runtime-java/sandbox-core/src/main/java/io/agentscope/runtime/sandbox/manager/SandboxService.java`
- `source/htam-agent-platform/agent-core/src/main/java/com/htam/agent/core/runtime/AgentScopeReActRuntime.java`
- `source/htam-agent-platform/agent-core/src/main/java/com/htam/agent/core/runtime/AgentScopeCapabilityAssembler.java`
