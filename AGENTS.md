# AGENTS.md

## 适用范围

本文件定义 `/opt/project/agent-platform` 仓库内 Agent 的执行规则。

本仓库是企业内部 Agent 平台的新工程工作区。`.apboa/` 目录存放上游 Apboa 源码，只能作为分析和迁移参考材料。技术架构、模块职责和迁移知识见 `RTK.md`。

## 硬性约束

- 任何情况下都不能修改、创建、删除、移动、格式化或生成 `.apboa/` 目录下的任何文件。
- 始终将 `.apboa/` 视为只读参考源码，只允许读取和分析。
- 如果用户请求看起来需要修改 `.apboa/`，不要修改它。应说明 `.apboa/` 是不可变参考目录，并将变更实现到新的项目结构中。
- 不要运行任何会写入 `.apboa/` 的命令，包括格式化、构建产物生成、依赖更新、代码生成、清理脚本或批量重写。
- 不要暂存或提交 `.apboa/` 目录下的任何变更。
- 不要为了完成任务回滚、覆盖或清理用户已有的无关改动。

## 代码与文档落点

- 可以读取 `.apboa/` 来理解现有行为，然后在 `.apboa/` 之外实现等价或改进后的能力。
- 后端/Maven 多模块新代码统一落在 `source/agent-platform/`。
- 前端代码从后端 Maven 工程中抽离，独立落在 `source/agent-platform-ui/`。
- Java 包名前缀和 Maven `groupId` 统一使用 `com.htam.agent`。
- 第一阶段数据库表结构保持不变，只迁移代码。
- 所有新平台代码和文档都应放在 `.apboa/` 之外。
- 除非用户明确指定其他位置，文档默认放在 `docs/` 下。

## 工作规则

- 搜索代码时优先使用 `rg` 或 `rg --files`。
- 手动编辑文件时使用 `apply_patch`。
- 修改前先理解现有结构和约定，避免盲目搬运 Apboa 包结构。
- 迁移 Apboa 行为时，先提取职责、契约、数据模型和运行链路，再在新模块中实现。
- 涉及架构边界、模块职责或迁移原则时，以 `RTK.md` 为准。
- 保留已有用户改动；如果用户改动影响当前任务，应在其基础上继续，不要擅自回滚。

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

## 参考资料

优先阅读以下资料理解背景：

- `RTK.md`
- `docs/apboa/01-系统详细设计报告.md`
- `docs/v1/01-需求简述.md`
- `docs/v1/02-模块迁移方案.md`
