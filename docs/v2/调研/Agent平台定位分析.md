# Agent 平台定位分析

## 1. 核心判断

`opencode`、`Qwen Code`、`Codex CLI`、`Workbuddy` 这类工具更接近“通用智能体”或“通用工作型智能体”。它们的核心能力是理解任务、读写代码或文件、调用工具、执行命令、持续迭代。

进入企业内部场景后，通常不建议只依赖一个裸的通用智能体，而应在通用底座之上形成“专家智能体 Profile”：

```text
Agent Runtime
  + system prompt
  + skills
  + MCP tools
  + knowledge sources
  + permissions
  + memory/session policy
  + evaluation rules
= Expert Agent
```

因此，企业内更推荐采用：

```text
通用底座 + 专家智能体 Profile
```

而不是每个业务场景都重新实现一套完全独立的智能体系统。

## 2. 通用底座与专家智能体的关系

通用底座负责通用能力：

- 对话理解
- 任务规划
- 工具调用
- 文件和代码操作
- 上下文管理
- 记忆管理
- 执行状态管理
- 人工确认
- 失败恢复
- 审计与追踪

专家智能体 Profile 负责场景差异：

- 专用系统提示词
- 专有 skills
- 专有 MCP 工具
- 业务知识库
- 权限范围
- 输出格式
- 审批流程
- 评测集
- 风险控制规则

例如：

- 需求分析智能体
- 架构设计智能体
- 后端迁移智能体
- SQL 审查智能体
- 测试用例生成智能体
- 运维排障智能体
- 合同审查智能体

通用智能体加上不同 skills、MCP 和提示词，确实可以在不同会话中承担不同角色。但在企业内部，若要做到可治理、可审计、可复用、可评测，最好将这些组合固化为明确的 Agent Profile，而不是完全依赖每次会话中的临时提示。

## 3. 企业 Agent 平台的分层

企业 Agent 平台中的“通用底座”不应只等同于某一个 CLI 或某一个编排框架，而应拆成多层能力：

```text
企业 Agent 平台
  ├─ Profile 管理：专家智能体定义、提示词、skills、MCP、权限、模型策略
  ├─ Session/Run 管理：会话、任务、状态、审计、回放、人工审批
  ├─ Agent Runtime：推理循环、工具调用、记忆、上下文压缩、失败恢复
  ├─ Workflow 编排：多步骤流程、多智能体协作、人工介入
  ├─ Tool/MCP 层：企业系统、数据库、代码库、知识库、审批流
  └─ Worker/Sandbox：代码执行、文件操作、构建测试、隔离环境
```

从这个角度看，`opencode`、`Qwen Code`、`Codex CLI`、`AgentScope Java`、`LangGraph`、`LangChain` 并不处在同一层。

## 4. 技术选型分类

### 4.1 CLI 型 Coding Agent

代表：

- `opencode`
- `Qwen Code`
- `Codex CLI`
- 类似 Workbuddy 的开发者工作台型工具

这类工具适合做“开发者工作台”或“代码任务执行器”。

适合场景：

- 代码迁移
- 修复 bug
- 修改仓库文件
- 运行测试
- 生成技术文档
- 作为平台调度的外部 worker

不适合作为唯一企业底座的原因：

- 本质偏人机协作工具，不是完整企业运行时。
- 权限、租户、审计、任务状态、审批、评测、Profile 生命周期通常还需要平台侧补齐。
- CLI 行为会受版本升级影响。
- 企业业务 Agent 不一定都是代码类任务。

结论：

```text
CLI 型 Coding Agent 可以纳入企业平台，作为某类专家 Agent 的执行后端；
但不建议把整个企业 Agent 平台绑定死在 CLI 上。
```

### 4.2 JVM/Java Agent Runtime

代表：

- `AgentScope Java`
- `Spring AI`
- `Semantic Kernel Java`

如果企业平台后端主栈是 Java/Spring，这类框架更适合作为企业 Agent 平台底座的一部分。

AgentScope Java 的定位更接近 JVM 上的 Agent Runtime，支持 ReAct 推理、Harness 工程化能力、多智能体编排、MCP/A2A 等能力，适合与企业后端的用户、权限、租户、任务、审计体系集成。

适合场景：

- 企业内部 Agent 平台
- 专家 Agent Profile 管理
- 多租户会话
- 权限控制
- MCP 客户端/服务端集成
- Spring Boot 服务化部署
- 任务状态持久化

结论：

```text
如果平台主工程采用 Java/Spring，优先考虑 Java 原生 Runtime，
例如 AgentScope Java / Spring AI 组合。
```

### 4.3 Workflow/编排型框架

代表：

- `LangGraph`
- `LangChain`

LangGraph 更像有状态 Agent/Workflow 编排引擎，重点能力包括：

- durable execution
- human-in-the-loop
- memory
- long-running workflow
- 多节点流程编排
- 多智能体协作

适合流程：

```text
需求分析 -> 架构设计 -> 代码生成 -> 测试 -> 审批
```

或：

```text
用户任务 -> 规划 Agent -> 多个执行 Agent -> 汇总 Agent -> 人工确认
```

LangChain 更偏模型、工具、RAG、链式调用的组件库。复杂 Agent 编排场景下，通常更适合使用 LangGraph，而不是只使用 LangChain。

适合场景：

- Python/JS 技术栈
- 复杂工作流
- 多 Agent 协作
- 快速实验
- RAG/工具集成丰富的场景

结论：

```text
LangGraph 适合作为工作流引擎；
LangChain 适合作为集成组件库；
如果主平台是 Java，可以将 LangGraph 作为独立 worker 服务，而不是强行嵌入 Java 主服务。
```

### 4.4 MCP 工具协议层

MCP 不是完整 Agent Runtime，而是工具与数据接入协议层。

它解决的问题是：

```text
Agent 如何标准化连接外部工具、数据源和业务系统
```

推荐结构：

```text
Agent Runtime
  -> MCP Client
      -> 企业 MCP Server：代码库、需求系统、数据库、知识库、CI/CD、审批系统
```

MCP 在企业平台中的价值：

- 标准化工具接入
- 降低不同 Agent 与不同系统之间的集成成本
- 复用企业内部工具能力
- 将业务系统能力封装成受控工具
- 便于权限控制、审计和治理

但 MCP 本身不能替代：

- Agent Runtime
- 会话管理
- 权限治理
- 任务编排
- 评测体系
- 人工审批流程

## 5. 推荐架构组合

针对企业内部 Agent 平台，推荐组合如下：

```text
主平台：
Java / Spring Boot
  + AgentScope Java 或 Spring AI 作为 Runtime 基础
  + 自研 Profile / Session / Run / Permission / Audit / Eval 模块
  + MCP Gateway / MCP Client / 企业 MCP Server

代码类专家 Agent：
  + 集成 Codex CLI / Qwen Code / opencode 作为 sandbox worker

复杂流程：
  + Java 内部状态机
  或
  + LangGraph 作为独立编排服务
```

更具体地，可以形成以下平台关系：

```text
Agent Platform
  ├─ Agent Profile Registry
  │   ├─ 需求分析 Agent
  │   ├─ 架构设计 Agent
  │   ├─ 后端迁移 Agent
  │   ├─ SQL 审查 Agent
  │   └─ 测试生成 Agent
  │
  ├─ Agent Runtime
  │   ├─ Java Native Runtime
  │   ├─ LangGraph Worker
  │   └─ CLI Coding Worker
  │
  ├─ Tool Layer
  │   ├─ MCP Client
  │   ├─ MCP Gateway
  │   └─ Enterprise MCP Servers
  │
  ├─ Governance
  │   ├─ Permission
  │   ├─ Audit
  │   ├─ Approval
  │   └─ Evaluation
  │
  └─ Workspace/Sandbox
      ├─ Code Workspace
      ├─ File Workspace
      └─ Build/Test Runtime
```

## 6. 对当前平台建设的建议

对于本项目这类企业内部 Agent 平台，建议将核心能力建设在平台侧，而不是直接围绕某一个 CLI 工具展开。

优先建设：

- Agent Profile 元数据模型
- Agent Runtime 抽象
- Session / Run / Step / ToolCall 运行记录
- MCP 工具注册与调用治理
- 用户、角色、权限与租户模型
- 人工审批与高危操作确认
- 运行日志、审计与回放
- 评测与样例集管理
- Worker/Sandbox 适配层

CLI 型 Coding Agent 可作为 Worker 插件引入：

```text
平台负责任务治理、权限、状态和审计；
CLI Agent 负责具体代码工作区中的执行能力。
```

这样可以避免平台被某个 CLI 工具的交互模式、配置格式、版本变化绑定。

## 7. 一句话结论

企业 Agent 平台中的“通用底座”不应该简单等于 `opencode`、`Qwen Code`、`Codex CLI` 这类 CLI，也不应该简单等于 `LangChain`。

更合理的定位是：

```text
通用底座 = Profile 管理 + 会话运行 + 权限审计 + 工具/MCP + 任务编排 + 评测治理 + Worker/Sandbox
```

`opencode`、`Qwen Code`、`Codex CLI`、`AgentScope Java`、`LangGraph`、`LangChain` 都可以成为这个底座中的某一层技术选型，但不应混为一谈。

推荐方向：

```text
Java/Spring 主平台
  + Java Agent Runtime
  + MCP 工具层
  + 可插拔 CLI Coding Worker
  + 必要时引入 LangGraph 独立编排服务
```

## 8. 参考资料

- [OpenCode Docs](https://dev.opencode.ai/docs)
- [Qwen Code GitHub](https://github.com/QwenLM/qwen-code)
- [AgentScope Java Docs](https://java.agentscope.io/zh/intro.html)
- [LangGraph Docs](https://docs.langchain.com/oss/python/langgraph/overview)
- [Model Context Protocol Docs](https://modelcontextprotocol.io/docs/getting-started/intro)
- [OpenAI Codex CLI Help](https://help.openai.com/en/articles/11096431)
