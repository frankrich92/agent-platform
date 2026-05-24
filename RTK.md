# RTK.md

## 定位

本文件记录 `/opt/project/agent-platform` 的技术知识、目标架构和迁移原则。Agent 执行规则、命令环境和文件落点约束见 `AGENTS.md`。

本仓库正在初始化为新的企业内部 Agent 平台。当前上游 Apboa 代码位于 `.apboa/` 目录，只能作为分析和迁移参考。

## 不可变参考源码

`.apboa/` 严格只读。任何 Agent、脚本、命令、格式化工具、代码生成器、迁移工具、构建流程或清理任务，在任何情况下都不能修改 `.apboa/` 内的任何内容。

所有新的实现工作都必须发生在 `.apboa/` 之外：

- 后端/Maven 多模块工程：`source/agent-platform/`
- 前端独立工程：`source/agent-platform-ui/`
- 文档默认目录：`docs/`
- Java 包名前缀和 Maven `groupId`：`com.htam.agent`
- 第一阶段数据库策略：表结构保持不变，只迁移代码

如果需要复用上游 Apboa 行为：

1. 读取 `.apboa/` 下的相关文件。
2. 提取其设计、契约、数据模型或行为。
3. 在 `.apboa/` 之外的新项目模块中重新实现或适配。
4. 记录任何有意引入的差异。

## 目标模块化

新平台应围绕以下边界组织：

- `agent-domain`：纯领域模型、枚举、值对象、领域事件。
- `agent-api`：DTO、VO、请求/响应契约、门面接口。
- `agent-biz`：面向用户侧的业务模块，例如 Agent、会话、聊天、记忆、执行、知识库、任务、工作流。
- `agent-admin`：管理侧模块，例如 IAM、Agent 管理、能力管理、Provider 管理、知识库管理、任务管理、系统设置、审计。
- `agent-infra`：平台基础设施能力，例如通用工具、安全、流式事件、能力抽象、Provider、文件、可观测性、编排。
- `agent-runtimes`：运行时 SPI 和具体运行时适配，例如 AgentScope 和沙箱运行时。
- `agent-repo`：仓储 SPI 和持久化实现，例如 MyBatis 和向量存储。
- `agent-adapter`：REST、WebSocket、AGUI 适配器。
- `agent-boot`：Spring Boot 启动和模块装配。

前端应用作为独立工程位于 `source/agent-platform-ui/`，不作为后端 Maven 子模块。

## 依赖方向

保持依赖单向：

```text
agent-boot
  -> agent-adapter/*
  -> agent-admin/*
  -> agent-biz/*
  -> agent-runtimes/*
  -> agent-infra/*
  -> agent-repo/*

agent-adapter/*
  -> agent-api
  -> agent-biz/*
  -> agent-admin/*

agent-admin/*
  -> agent-biz/*
  -> agent-api
  -> agent-domain

agent-biz/*
  -> agent-domain
  -> agent-api
  -> repo-spi
  -> runtime-spi
  -> platform-*

runtime-agentscope
  -> runtime-spi
  -> infra-capability
  -> infra-provider
  -> infra-stream
  -> infra-files
  -> agent-domain

repo-mybatis
  -> repo-spi
  -> agent-domain

agent-domain
  -> no Spring / no MyBatis / no AgentScope
```

`source/agent-platform-ui/` 不参与上述后端依赖图。

## 迁移原则

不要从 Apboa 进行盲目的包迁移，应按职责迁移：

- Apboa 的 `common` 应拆分为领域、API、平台通用、安全、MyBatis、流式事件相关模块。
- Apboa 的 `core` 应拆分为运行时、Provider、能力抽象、文件、流式事件、RAG/知识库相关职责。
- Apboa 的 `biz/agent` 应拆分为 Agent、会话、聊天、执行等业务模块。
- Controller 应迁移到 adapter 模块。
- AgentScope 专属类型应限制在 `runtime-agentscope` 和 adapter 模块中，不要泄漏到业务模块。

## 迁移检查点

迁移或新增能力时，至少确认以下事项：

- 是否保持第一阶段数据库表结构不变。
- 是否避免将 Spring、MyBatis、AgentScope 类型泄漏到 `agent-domain`。
- 是否避免将 AgentScope 专属类型泄漏到业务模块。
- 是否按用户侧业务、管理侧业务、平台能力、运行时、仓储、适配器拆分职责。
- 是否在 `.apboa/` 之外实现变更，并记录有意引入的行为差异。

## 当前知识库

已有分析和规划文档：

- `docs/apboa/01-系统详细设计报告.md`：当前 Apboa 架构、模块、运行链路、数据模型和版本基线的静态分析。
- `docs/v1/01-需求简述.md`：初始需求简述。
- `docs/v1/02-模块迁移方案.md`：Maven 多模块迁移方案。
