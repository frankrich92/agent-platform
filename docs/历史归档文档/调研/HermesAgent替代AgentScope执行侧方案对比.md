# Hermes Agent 替代 AgentScope 执行侧方案对比

## 结论

如果采用 Hermes Agent 作为底层，可以理解为用 Hermes 的 Agent runtime 能力替代当前规划中的 `agentscope-java + agentscope-runtime-java` 执行侧组合。

但这个替代不是完整平台替代。Hermes Agent 可以覆盖较多 Agent 执行、工具调用、MCP、Skills、记忆、运行事件和 API Server 能力；用户管理、Agent 授权、租户隔离、能力白名单、审计、消息主数据、Run / Span 主数据和前端控制面仍应由 `htam-agent-platform` 自己掌握。

更稳妥的定位是：

```text
Hermes Agent
= 可选的 AgentRuntime 后端

htam-agent-platform
= 平台控制面、用户权限、会话消息、审计观测和前端体验
```

## 替换关系

| 当前方案 | Hermes 方案中的对应 |
| --- | --- |
| `agentscope-java`：`ReActAgent`、Skills、MCP、Memory、Stream 事件 | Hermes `AIAgent`、Tools、Skills、MCP、Memory、Runs / API Server |
| `agentscope-runtime-java`：Agent-as-API、Sandbox、Session / Memory 服务化、A2A | Hermes API Server、Runs API、Profiles、Terminal backends、Docker / SSH / Modal / Daytona 等执行后端 |
| `htam-agent-platform`：用户、Agent 授权、会话消息、Run / Span、SSE、前端 | 仍然由平台保留，不建议交给 Hermes |

## 推荐架构

不建议前端直接连接 Hermes。推荐由平台后端封装一层 `HermesRuntime`：

```text
前端 UI
  -> htam-agent-platform API
     -> 用户认证 / Agent 授权 / 能力白名单 / 审计 / 会话消息落库
        -> AgentRuntime 抽象
           -> OpenAiChatRuntime
           -> AgentScopeReActRuntime
           -> HermesRuntime
              -> Hermes API Server / Runs API
```

平台职责：

- 校验 `userCode + agentId` 授权。
- 根据 Agent 定义裁剪 MCP / Skills 能力。
- 组装系统提示词、上下文、用户隔离记忆。
- 将请求转发给 Hermes API Server 或 Runs API。
- 将 Hermes tool progress / run events 映射为平台 SSE 事件。
- 将消息、parts、Run、Span、工具摘要和错误写入平台数据库。

Hermes 职责：

- 执行 Agent 循环。
- 调用模型。
- 调用工具、MCP server 和 Skills。
- 管理执行态上下文、工具进度和 runtime 内部状态。

## Hermes Agent 的优势

### 1. 能力完整，开箱即用强

Hermes 已经具备 API Server、Runs API、MCP、Skills、工具集、记忆、上下文压缩、Cron、子 Agent、浏览器、文件、终端等能力。对于 V2 的“Agent 对话 + MCP + Skills”目标，落地速度可能比围绕 AgentScope Java 自行拼装完整运行时更快。

### 2. 适合做图形化 Agent 后端

Hermes API Server 支持 OpenAI-compatible `/v1/chat/completions`、`/v1/responses`、Runs API 和 SSE 工具进度事件，天然可以接自研前端，也能接 Open WebUI、LobeChat 等通用前端。

### 3. Skills 生态更活跃

Hermes 的 Skills 是核心卖点，支持渐进式披露、Skills Hub、自动技能创建和技能修补。若平台目标是“可成长 Agent”，Hermes 的产品心智更贴近。

### 4. MCP 能力成熟

Hermes 支持 stdio / HTTP MCP server、工具 include / exclude 过滤、动态工具刷新和 `mcp-<server>` toolset。作为工具生态底座比较完整。

### 5. 执行环境选择多

Hermes 支持 local、Docker、SSH、Singularity、Modal、Daytona、Vercel Sandbox 等终端后端。对工具执行、长任务和隔离运行有吸引力。

## Hermes Agent 的劣势

### 1. 技术栈不一致

当前平台是 Java / Spring Boot，Hermes 是 Python 为主。引入后会变成“Java 平台 + Python Agent 服务”的双运行时架构，部署、监控、日志、版本管理和故障排查都会复杂一些。

### 2. 平台级权限不是 Hermes 的核心强项

Hermes 有 profiles、Bearer token、工具过滤、命令审批、平台 allowlist，但这不是完整企业用户 / Agent 授权模型。

以下能力仍应由平台实现：

- 用户 A 是否可以使用 Agent B。
- Agent B 是否可以调用 MCP C。
- 当前用户是否允许使用某个高风险工具。
- 工具参数和结果如何按 `userCode + agentId` 审计。
- 会话、消息、记忆和 Run / Span 如何做用户隔离。

### 3. 默认能力强，安全面大

Hermes 包含 terminal、file、browser、cron、delegation、MCP 等高风险能力。一旦作为底层接入生产平台，必须默认关闭工具，再按 Agent、用户、环境和安全策略逐项开启。

### 4. 状态容易双写

Hermes 有自己的 session storage、response storage 和 memory；平台也有 `agent_session`、`agent_message`、`agent_run_span` 等表。如果边界不清，会出现状态分裂。

建议平台数据库作为主数据源，Hermes 状态只作为执行态或 runtime 内部状态。

### 5. Java 生态集成优势减弱

`agentscope-java + agentscope-runtime-java` 与 Spring Boot、Project Reactor、Maven、Java 类型系统更贴近。当前平台已经有 `AgentRuntime`、`AgentScopeReActRuntime`、`CapabilityRegistry` 等方向，继续走 Java 路线维护成本更低。

## AgentScope 组合的优势

- 与当前 Java 平台同栈，部署和调试链路更简单。
- 更容易把权限校验、SSE 映射、Run / Span 记录和数据库事务放在一个应用内。
- `agentscope-java` 负责 Agent 执行，`agentscope-runtime-java` 负责 Agent-as-API、沙箱、Session / Memory 服务化，职责边界清晰。
- `agentscope-runtime-java` 官方定位为 Java Agent 部署与工具沙箱，可集成 AgentScope Java、Spring AI Alibaba、LangChain4j 等 Java 框架。
- 对企业级、多用户、强治理平台而言，架构边界更规整。

## AgentScope 组合的劣势

- 需要平台自行补更多产品化能力，例如图形化事件体验、Skills 管理、MCP 配置 UI、记忆治理、工具进度展示和技能生态。
- 相比 Hermes，开箱工具和自动成长能力弱一些。
- 如果目标是快速做一个能力很强的个人或团队 Agent 助手，AgentScope 组合可能需要更多工程投入。

## 适用场景判断

| 目标 | 更适合的路线 |
| --- | --- |
| 企业级、多用户、强权限、强审计 Agent 平台 | `agentscope-java + agentscope-runtime-java` 主线更稳 |
| 快速做有 MCP、Skills、工具调用和长任务能力的 Agent 产品原型 | Hermes Agent 更省事 |
| 想保留技术路线弹性 | 保留 `AgentRuntime` 抽象，同时支持 AgentScope 和 Hermes |
| 想做“可成长 Agent”、自动技能沉淀、自我改进 | Hermes Agent 更贴近 |
| 想减少跨语言运行时和部署复杂度 | AgentScope Java 组合更贴近当前平台 |

## 建议路线

短期：

1. V1 不引入 Hermes，继续保持普通对话链路轻量。
2. V2 优先完成当前 `AgentRuntime` 抽象和 `AgentScopeReActRuntime`。
3. 不要把前端直接接到 Hermes，避免绕过平台权限和审计。

中期：

1. 新增 `HermesRuntime` 做 PoC。
2. 用真实场景验证 MCP / Skills 调用、流式事件、工具进度、停止生成、错误处理和审计落库。
3. 验证平台能力白名单如何映射到 Hermes toolsets、MCP include / exclude 和 profiles。

长期：

1. 平台继续掌握用户、Agent、权限、审计、会话、消息和 Run / Span 主数据。
2. Hermes 作为可插拔底层 runtime，而不是平台控制面的替代品。
3. 根据真实验证结果决定是否将 Hermes Runtime 升为主 runtime，或只作为高阶工具型 Agent runtime。

## 最终判断

Hermes Agent 可以替代 `agentscope-java + agentscope-runtime-java` 的较大一部分执行侧能力，尤其是 Agent 对话、MCP、Skills、工具调用、运行事件和 API Server。

但从当前项目定位看，不建议直接把 Hermes 作为唯一底层并绕过平台控制面。更合理的方案是保留 `AgentRuntime` 抽象：

```text
AgentRuntime
  -> OpenAiChatRuntime
  -> AgentScopeReActRuntime
  -> HermesRuntime
```

这样既能保持当前 Java 平台路线稳定，也能用 Hermes 快速验证高阶 Agent 能力，不把项目过早绑定到单一 runtime。

## 参考

- Hermes Agent 文档：<https://hermes-agent.nousresearch.com/docs/>
- Hermes API Server：<https://hermes-agent.nousresearch.com/docs/user-guide/features/api-server>
- Hermes MCP Integration：<https://hermes-agent.nousresearch.com/docs/user-guide/features/mcp>
- Hermes Security：<https://hermes-agent.nousresearch.com/docs/user-guide/security>
- Hermes Architecture：<https://hermes-agent.nousresearch.com/docs/developer-guide/architecture>
- AgentScope Java：<https://github.com/agentscope-ai/agentscope-java>
- AgentScope Runtime Java：<https://github.com/agentscope-ai/agentscope-runtime-java>
- AgentScope Runtime 文档：<https://runtime.agentscope.io/en/intro.html>
