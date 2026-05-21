# AGENTS.md

## 项目范围

本仓库是企业内部 Agent 平台的新工程工作区。`.apboa/` 目录存放上游 Apboa 源码，只作为分析和迁移参考材料。

## 硬性约束

- 任何情况下都不能修改、创建、删除、移动、格式化或生成 `.apboa/` 目录下的任何文件。
- 始终将 `.apboa/` 视为只读参考源码。
- 如果用户请求看起来需要修改 `.apboa/`，不要修改它。应说明 `.apboa/` 是不可变参考目录，并将变更实现到新的项目结构中。
- 不要运行任何会写入 `.apboa/` 的命令，包括格式化、构建产物生成、依赖更新、代码生成、清理脚本或批量重写。
- 不要暂存或提交 `.apboa/` 目录下的任何变更。

## 工作规则

- 可以读取 `.apboa/` 来理解现有行为，然后在 `.apboa/` 之外实现等价或改进后的能力。
- 后端/Maven 多模块新代码统一落在 `source/agent-platform/`。
- 前端代码从后端 Maven 工程中抽离，独立落在 `source/agent-platform-ui/`。
- Java 包名前缀和 Maven `groupId` 统一使用 `com.htam.agent`。
- 第一阶段数据库表结构保持不变，只迁移代码。
- 所有新平台代码和文档都应放在 `.apboa/` 之外。
- 保留已有用户改动，不要回滚无关文件。
- 搜索代码时优先使用 `rg` 或 `rg --files`。
- 手动编辑文件时使用 `apply_patch`。
- 除非用户明确指定其他位置，文档默认放在 `docs/` 下。

## 固定执行环境

涉及 Java、Maven、Node、Python、前端包管理器或构建验证时，先在 zsh 中应用以下环境，再执行命令：

```sh
export JAVA_HOME=/opt/jdk-21.0.7
export MAVEN_HOME=/opt/apache-maven-3.9.12
export NODE_HOME=/root/.nvm/versions/node/v24.15.0
export PYTHON_HOME=/app/miniforge3/envs/agent
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$NODE_HOME/bin:$PATH"
conda activate agent
```

## 目标方向

目标架构应将 Apboa 概念迁移为更清晰的 Maven 多模块平台，围绕以下模块组织：

- `agent-domain`
- `agent-api`
- `agent-biz`
- `agent-admin`
- `agent-platform`
- `agent-runtimes`
- `agent-repo`
- `agent-adapter`
- `agent-boot`

前端独立工程为 `source/agent-platform-ui/`，不作为后端 Maven reactor 子模块。

参考文档：

- `docs/apboa/01-系统详细设计报告.md`
- `docs/v1/01-需求简述.md`
- `docs/v1/02-模块迁移方案.md`
