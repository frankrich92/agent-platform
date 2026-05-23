# Agent 平台架构优化

## 1. 优化目标

当前工程已经按 `domain / api / biz / admin / infra / runtimes / repo / adapter / boot` 做了初步模块化，但随着平台定位从“应用迁移”转向“企业 Agent 平台”，模块边界应进一步贴合 Agent 平台能力域。

建议逐步从 `biz-*`、`admin-*`、`infra-*` 这种偏技术分层的模块命名，调整为面向平台能力的模块命名：

```text
Profile / Run / Runtime / Workflow / Capability / Worker / Governance
```

这样能更清晰表达平台职责，也更利于后续扩展通用底座、专家智能体 Profile、多运行时、多工具协议、多 Worker 和治理能力。

## 2. 企业 Agent 平台能力分层

目标能力分层如下：

```text
企业 Agent 平台
  ├─ Profile 管理：专家智能体定义、提示词、skills、MCP、权限、模型策略
  ├─ Session/Run 管理：会话、任务、状态、审计、回放、人工审批
  ├─ Agent Runtime：推理循环、工具调用、记忆、上下文压缩、失败恢复
  ├─ Workflow 编排：多步骤流程、多智能体协作、人工介入
  ├─ Tool/MCP 层：企业系统、数据库、代码库、知识库、审批流
  └─ Worker/Sandbox：代码执行、文件操作、构建测试、隔离环境
```

其中，管理端和用户端只是不同入口，不应决定核心模块边界。核心模块应围绕平台能力组织。

## 3. 推荐顶层模块

推荐目标模块如下：

```text
source/agent-platform/
  ├─ agent-domain
  ├─ agent-api
  ├─ agent-profile
  ├─ agent-run
  ├─ agent-runtime
  ├─ agent-workflow
  ├─ agent-capability
  ├─ agent-worker
  ├─ agent-governance
  ├─ agent-repo
  ├─ agent-adapter
  └─ agent-boot
```

## 4. 模块职责

### 4.1 agent-profile

`agent-profile` 负责专家智能体 Profile 管理。

包含能力：

- Agent 定义
- Profile 元数据
- 系统提示词绑定
- 模型配置与模型策略
- Skill 绑定
- MCP 绑定
- 工具绑定
- 知识库绑定
- 子 Agent 绑定
- Profile 版本、标签、启停状态

它对应“通用底座 + 专家智能体 Profile”中的 Profile 层。

### 4.2 agent-run

`agent-run` 负责 Session / Run 管理。

包含能力：

- 会话管理
- 消息管理
- Run 记录
- Step 记录
- ToolCall 记录
- 定时任务
- 任务状态
- 运行历史
- 回放
- 运行结果查询

它不应直接依赖具体 Runtime 实现，而应通过 `agent-runtime` 的 SPI 调用运行时。

### 4.3 agent-runtime

`agent-runtime` 负责 Agent Runtime 抽象与具体运行时适配。

包含能力：

- Runtime SPI
- AgentScope Runtime
- Hermes Runtime
- LangGraph Runtime
- 记忆装配
- 上下文压缩
- 推理循环适配
- 工具调用适配
- 失败恢复策略

当前已经开始引入平台侧 Runtime SPI，后续应继续避免业务模块直接依赖 AgentScope 类型。

### 4.4 agent-workflow

`agent-workflow` 负责工作流和多 Agent 编排。

包含能力：

- 工作流定义
- 节点编排
- 多步骤流程
- 多 Agent 协作
- Human-in-the-loop
- 长任务恢复
- 流程状态机
- 编排事件

该模块可以先以 Java 状态机实现，后续也可接入 LangGraph 作为独立编排 Worker。

### 4.5 agent-capability

`agent-capability` 负责 Tool / MCP / Skill / Knowledge 能力层。

包含能力：

- 工具目录
- 内置工具
- 动态工具
- MCP Server
- MCP Tool
- Skill 包
- Skill 导入
- 知识库
- RAG 配置
- 企业连接器

它解决“Agent 能使用哪些能力”的问题，不负责具体会话运行。

### 4.6 agent-worker

`agent-worker` 负责 Worker / Sandbox 能力。

包含能力：

- 代码执行
- 文件读写
- 工作区管理
- Shell 执行
- 构建测试
- 沙箱隔离
- CLI Coding Agent 适配
- 远程 Worker 调度

例如 `Codex CLI`、`Qwen Code`、`opencode` 这类 CLI Coding Agent，更适合放在 `agent-worker` 下作为可插拔 Worker，而不是直接作为主平台 Runtime。

### 4.7 agent-governance

`agent-governance` 负责治理能力。

包含能力：

- IAM
- 权限控制
- 审计
- 人工审批
- 安全策略
- 敏感词
- 操作确认
- 风险控制
- 评测规则
- Eval 样例集
- 可观测性

治理能力应该横向作用于 Profile、Run、Runtime、Capability、Worker 等模块。

## 5. 现有模块迁移映射

建议迁移关系如下：

```text
当前模块                                      建议归属

agent-biz/biz-agent                         -> agent-profile
agent-admin/admin-agent                      -> agent-profile
agent-admin/admin-provider                   -> agent-profile / agent-capability
agent-admin/admin-capability                 -> agent-capability + agent-governance
agent-biz/biz-session                        -> agent-run
agent-biz/biz-chat                           -> agent-run
agent-biz/biz-task                           -> agent-run
agent-admin/admin-audit                      -> agent-governance / agent-run
agent-biz/biz-memory                         -> agent-runtime
agent-biz/biz-execution                      -> agent-worker
agent-biz/biz-workflow                       -> agent-workflow
agent-biz/biz-knowledge                      -> agent-capability
agent-admin/admin-knowledge                  -> agent-capability
agent-runtimes/runtime-spi                   -> agent-runtime/runtime-spi
agent-runtimes/runtime-agentscope            -> agent-runtime/runtime-agentscope
agent-runtimes/runtime-hermes                -> agent-runtime/runtime-hermes
agent-runtimes/runtime-sandbox               -> agent-worker/worker-sandbox
agent-infra/infra-orchestration              -> agent-workflow 或 agent-runtime
agent-infra/infra-security                   -> agent-governance
agent-infra/infra-observability              -> agent-governance
agent-repo/*                                 -> 保留 agent-repo
agent-adapter/*                              -> 保留 agent-adapter
agent-boot                                   -> 保留 agent-boot
```

## 6. 推荐细化结构

如果进一步细化，可以采用以下结构：

```text
agent-profile
  ├─ profile-api
  ├─ profile-service
  └─ profile-admin

agent-run
  ├─ run-api
  ├─ run-session
  ├─ run-history
  └─ run-task

agent-runtime
  ├─ runtime-spi
  ├─ runtime-agentscope
  └─ runtime-hermes

agent-workflow
  ├─ workflow-api
  ├─ workflow-engine
  └─ workflow-human-task

agent-capability
  ├─ capability-tool
  ├─ capability-mcp
  ├─ capability-skill
  ├─ capability-knowledge
  └─ capability-connector

agent-worker
  ├─ worker-spi
  ├─ worker-sandbox
  ├─ worker-file
  ├─ worker-build
  └─ worker-coding-cli

agent-governance
  ├─ governance-iam
  ├─ governance-audit
  ├─ governance-approval
  ├─ governance-policy
  └─ governance-eval
```

## 7. 完全细化后的目标结构

以下是一版完全细化后的目标 Maven 模块结构，可作为最终态参考。实际落地时不建议一次性重命名到位，应按迁移顺序逐步推进。

```text
source/agent-platform/
  ├─ pom.xml
  │
  ├─ agent-domain
  │   ├─ domain-common
  │   ├─ domain-profile
  │   ├─ domain-run
  │   ├─ domain-capability
  │   ├─ domain-workflow
  │   ├─ domain-worker
  │   └─ domain-governance
  │
  ├─ agent-api
  │   ├─ api-common
  │   ├─ api-profile
  │   ├─ api-run
  │   ├─ api-capability
  │   ├─ api-workflow
  │   ├─ api-worker
  │   └─ api-governance
  │
  ├─ agent-profile
  │   ├─ profile-core
  │   ├─ profile-agent
  │   ├─ profile-prompt
  │   ├─ profile-model-policy
  │   ├─ profile-binding
  │   ├─ profile-version
  │   └─ profile-admin
  │
  ├─ agent-run
  │   ├─ run-core
  │   ├─ run-session
  │   ├─ run-message
  │   ├─ run-task
  │   ├─ run-step
  │   ├─ run-tool-call
  │   ├─ run-history
  │   ├─ run-replay
  │   └─ run-event
  │
  ├─ agent-runtime
  │   ├─ runtime-spi
  │   ├─ runtime-core
  │   ├─ runtime-context
  │   ├─ runtime-memory
  │   ├─ runtime-planning
  │   ├─ runtime-agentscope
  │   ├─ runtime-hermes
  │   ├─ runtime-langgraph
  │   └─ runtime-testkit
  │
  ├─ agent-workflow
  │   ├─ workflow-core
  │   ├─ workflow-definition
  │   ├─ workflow-engine
  │   ├─ workflow-node
  │   ├─ workflow-human-task
  │   ├─ workflow-event
  │   └─ workflow-runtime
  │
  ├─ agent-capability
  │   ├─ capability-core
  │   ├─ capability-tool
  │   ├─ capability-mcp
  │   ├─ capability-skill
  │   ├─ capability-knowledge
  │   ├─ capability-rag
  │   ├─ capability-provider
  │   ├─ capability-connector
  │   └─ capability-registry
  │
  ├─ agent-worker
  │   ├─ worker-spi
  │   ├─ worker-core
  │   ├─ worker-sandbox
  │   ├─ worker-workspace
  │   ├─ worker-file
  │   ├─ worker-shell
  │   ├─ worker-build
  │   ├─ worker-coding-cli
  │   ├─ worker-codex
  │   ├─ worker-qwen-code
  │   └─ worker-opencode
  │
  ├─ agent-governance
  │   ├─ governance-iam
  │   ├─ governance-auth
  │   ├─ governance-policy
  │   ├─ governance-approval
  │   ├─ governance-audit
  │   ├─ governance-risk
  │   ├─ governance-sensitive
  │   ├─ governance-observability
  │   └─ governance-eval
  │
  ├─ agent-repo
  │   ├─ repo-spi
  │   ├─ repo-mybatis
  │   ├─ repo-vector
  │   ├─ repo-cache
  │   └─ repo-migration
  │
  ├─ agent-adapter
  │   ├─ adapter-rest
  │   ├─ adapter-websocket
  │   ├─ adapter-agui
  │   ├─ adapter-openapi
  │   ├─ adapter-admin
  │   └─ adapter-internal
  │
  └─ agent-boot
      ├─ boot-app
      ├─ boot-autoconfigure
      └─ boot-starter
```

### 7.1 核心分工

```text
agent-profile
  管“Agent 是什么”
  定义专家智能体 Profile、提示词、模型策略、skills/MCP/工具/知识库绑定。

agent-run
  管“Agent 一次运行发生了什么”
  会话、消息、Run、Step、ToolCall、任务、历史、回放。

agent-runtime
  管“Agent 怎么推理和执行”
  Runtime SPI、AgentScope/Hermes/LangGraph 适配、记忆、规划、上下文。

agent-capability
  管“Agent 能用什么能力”
  Tool、MCP、Skill、Knowledge、RAG、Provider、Connector。

agent-worker
  管“外部执行环境”
  沙箱、工作区、文件、Shell、构建测试、Codex/Qwen Code/opencode CLI Worker。

agent-workflow
  管“复杂流程怎么编排”
  多步骤流程、多 Agent 协作、人工节点、长任务状态机。

agent-governance
  管“平台怎么治理”
  IAM、鉴权、策略、审批、审计、安全、敏感词、观测、评测。

agent-repo
  管“怎么存”
  仓储 SPI、MyBatis、向量库、缓存、迁移。

agent-adapter
  管“怎么暴露”
  REST、WebSocket、AGUI、OpenAPI、管理端入口、内部调用入口。

agent-boot
  管“怎么启动和装配”
  Spring Boot 启动、自动配置、Starter。
```

### 7.2 依赖方向

建议固定核心依赖方向：

```text
agent-boot
  -> agent-adapter
  -> agent-profile / agent-run / agent-workflow / agent-governance
  -> agent-runtime / agent-capability / agent-worker
  -> agent-repo
  -> agent-api
  -> agent-domain
```

底层规则：

```text
domain-* 不依赖 Spring / MyBatis / AgentScope
api-* 只放 DTO / VO / facade contract
profile/run/workflow/capability/worker/governance 依赖 repo-spi，不直接依赖 repo-mybatis
业务模块依赖 runtime-spi，不直接依赖 runtime-agentscope
runtime-agentscope 可以依赖 AgentScope 类型
worker-codex / worker-qwen-code / worker-opencode 只作为 Worker 实现，不进入核心业务模块
```

### 7.3 首批落地模块

如果开始落地迁移，第一批建议优先建设：

```text
agent-runtime/runtime-spi
agent-run/run-session
agent-run/run-task
agent-profile/profile-agent
agent-profile/profile-binding
agent-capability/capability-mcp
agent-capability/capability-skill
```

这批模块最贴近当前代码，也最能把“通用底座 + 专家智能体 Profile”的边界立起来。

## 8. 迁移顺序建议

建议按以下顺序逐步迁移，避免一次性大范围重命名导致风险过高：

1. 建立新的能力域父模块，但先保留旧模块。
2. 完善 `agent-runtime/runtime-spi`，继续收口业务模块对 AgentScope 的直接依赖。
3. 将 `biz-agent` 和 `admin-agent` 合并迁移到 `agent-profile`。
4. 将 `biz-session`、`biz-chat`、`biz-task` 合并迁移到 `agent-run`。
5. 将 Tool、MCP、Skill、Knowledge 迁移到 `agent-capability`。
6. 将代码执行、工作区、沙箱和 CLI Agent 适配迁移到 `agent-worker`。
7. 将 IAM、审计、审批、敏感词、安全策略、评测迁移到 `agent-governance`。
8. 清理旧的 `biz-*`、`admin-*`、`infra-*` 命名残留。

## 9. 当前优先级

结合当前项目状态，优先级建议如下：

```text
第一优先级：
  agent-runtime/runtime-spi
  agent-run
  agent-profile

第二优先级：
  agent-capability
  agent-worker

第三优先级：
  agent-workflow
  agent-governance
```

原因：

- 当前平台已经有 `AgentDefinition`，它实质上就是 Profile。
- 当前平台已经开始引入 Runtime SPI，应继续将业务层与 AgentScope 解耦。
- Session、Chat、Task 是运行链路核心，应尽快统一到 Run 模型。
- Tool、MCP、Skill、Knowledge 已经具备较多实现，后续适合统一为 Capability 层。
- Worker/Sandbox 是代码类专家 Agent 的关键能力，但可以在 Runtime SPI 稳定后推进。
- Workflow 和 Governance 需要更完整的运行记录与权限模型支撑，可稍后增强。

## 10. 结论

不要再用 `biz-*` / `admin-*` 作为主业务模块边界，而应改成：

```text
profile / run / runtime / workflow / capability / worker / governance
```

更准确的目标定位是：

```text
企业 Agent 平台 = 专家智能体 Profile + 运行会话管理 + 可插拔 Runtime + 能力接入 + Worker/Sandbox + 治理体系
```

管理端和用户端只是不同访问入口，应通过 `agent-adapter` 或 facade 层暴露，不应反向决定核心模块的命名和职责边界。
