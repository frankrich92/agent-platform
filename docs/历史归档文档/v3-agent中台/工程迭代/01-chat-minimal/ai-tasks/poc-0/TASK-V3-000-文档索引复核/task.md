# TASK-V3-000 文档索引复核

## 状态

Review

## 背景

V3 文档已成为当前主线，后续 AI agent 需要稳定入口，避免从 V1/V2 历史文档开始执行。

## 目标

- 复核 `docs/README.md`、`docs/v3-agent中台/README.md`、`docs/调研/README.md` 的阅读路径。
- 确认 V1/V2 关键文档顶部有历史提示。
- 确认 AGENTS 分层规则与 V3 文档一致。
- 输出文档链接、路径、旧口径残留检查结果。

## 非目标

- 不改业务代码。
- 不创建 Maven、Node 工程。
- 不重写 V1/V2 历史正文。

## 必须阅读

```text
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/调研/README.md
AGENTS.md
docs/AGENTS.md
```

## owned modules

```text
docs
```

## owned files

```text
docs/README.md
docs/v3-agent中台/README.md
docs/调研/README.md
AGENTS.md
docs/AGENTS.md
docs/v1-通用大模型对话/01-通用大模型对话需求.md 顶部历史提示
docs/v1-通用大模型对话/04-通用大模型对话详细设计.md 顶部历史提示
docs/v2-通用大模型对话/实施计划/v2-第一轮执行步骤.md 顶部历史提示
docs/v2-通用大模型对话/评审/agentscope源码分析.md 顶部历史提示
docs/v3-agent中台/RTK.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-000-文档索引复核/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-000-文档索引复核/test-report.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-000-文档索引复核/handoff.md
```

## 禁止修改

```text
source/
docs/v1-通用大模型对话/ 正文内容
docs/v2-通用大模型对话/ 正文内容
```

## 输入契约

当前 V3 文档结构和历史文档提示。

## 输出契约

文档索引检查结论；如修改索引，必须保持本地 Markdown 链接可用。

## 目标验收等级

L0

## 必须执行的验证命令

```bash
git diff --check
rg -n "docs/v3-agent-platform|agent-runtime-openai|agent-application|agent-services-business|agent-services-foundation" docs AGENTS.md || true
```

## 真实 E2E 要求

无。

## 风险

旧路径残留会误导后续 AI agent。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
