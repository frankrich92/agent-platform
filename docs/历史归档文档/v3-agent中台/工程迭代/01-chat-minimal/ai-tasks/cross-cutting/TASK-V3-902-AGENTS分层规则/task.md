# TASK-V3-902 AGENTS 分层规则

## 状态

Review

## 背景

V3 后续将采用 AI Native 方式推进，仓库需要把 V3 主线、任务包、代码根目录、AgentScope 复用边界和验证规则写入 AI agent 默认会读取的指令文件中。

## 目标

- 更新根 `AGENTS.md`，从 V1/早期目录口径切换到 V3 Agent 中台口径。
- 新增 `docs/AGENTS.md`，约束文档维护、RTK、任务包和开源源码引用方式。
- 新增 `source/v3-agent-platform/AGENTS.md`，约束 V3 后端模块、依赖方向、运行时和持久化边界。
- 新增 `source/v3-agent-platform-ui/AGENTS.md`，约束 V3 前端体验、API/SSE 和 Playwright E2E。
- 确保 AGENTS 规则与 `docs/README.md`、`docs/v3-agent中台/README.md`、`RTK.md` 一致。

## 非目标

- 不创建 Maven 工程。
- 不创建前端工程。
- 不实现业务代码。
- 不修改旧 V1/V2 历史正文。

## 必须阅读

```text
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/07-开发规范与本地环境.md
```

## owned modules

```text
AGENTS
docs
source/v3-agent-platform
source/v3-agent-platform-ui
```

## owned files

```text
AGENTS.md
docs/AGENTS.md
source/v3-agent-platform/AGENTS.md
source/v3-agent-platform-ui/AGENTS.md
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/**
```

## 禁止修改

```text
source/v3-agent-platform/**/pom.xml
source/v3-agent-platform/**/src/**
source/v3-agent-platform-ui/package.json
source/v3-agent-platform-ui/src/**
docs/v1-通用大模型对话/
docs/v2-通用大模型对话/
```

## 输入契约

V3 文档主线、AI Native 交付规约、任务包分层规则。

## 输出契约

分层 AGENTS 指令、RTK 追踪入口和 POC 分层任务包结构。

## 目标验收等级

L0

## 必须执行的验证命令

```bash
git diff --check
```

建议额外验证：

```bash
rg -n "docs/v3-agent-platform|ai-tasks/TASK-V3|docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/TASK-V3" AGENTS.md docs/AGENTS.md docs/README.md docs/v3-agent中台 source/v3-agent-platform source/v3-agent-platform-ui || true
```

## 真实 E2E 要求

无。

## 风险

AGENTS 指令如果与正式文档不一致，后续 AI agent 会按错误入口或旧目录执行。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
