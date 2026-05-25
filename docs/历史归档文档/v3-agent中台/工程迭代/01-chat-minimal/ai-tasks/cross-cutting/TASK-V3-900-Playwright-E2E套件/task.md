# TASK-V3-900 Playwright E2E 套件

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

`chat-minimal` 的页面体验必须通过真实浏览器操作验证。API/SSE E2E 可以证明接口可用，但不能替代用户从页面选择 Agent、新建会话、发送消息、停止、查看执行详情、重新生成和编辑重发的体验验收。

## 目标

- 在 `source/v3-agent-platform-ui/tests` 下维护 Playwright 主流程测试。
- 覆盖 Chat 工作台的主要交互路径。
- 使用真实前端页面和后端 API，不用 API E2E 替代 UI E2E。
- 与真实 LLM 输出兼容，不绑定 deterministic fallback 的固定文案。

## 非目标

- 不覆盖 Admin、任务中心、工作流和 input-ocr。
- 不做全量视觉回归。
- 不替代 `TASK-V3-901` 的数据库持久化 E2E。

## 必须阅读

```text
全局规则（精读）：
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/08-小工程切片与验收门禁.md

本小工程（精读）：
docs/v3-agent中台/工程迭代/01-chat-minimal/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/01-工程范围与验收目标.md
docs/v3-agent中台/工程迭代/01-chat-minimal/04-工程测试策略与真实E2E.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/README.md

本任务（精读）：
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-900-Playwright-E2E套件/task.md
```

## owned modules

```text
source/v3-agent-platform-ui/tests
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-900-Playwright-E2E套件
```

## owned files

```text
source/v3-agent-platform-ui/tests/chat-minimal.spec.ts
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-900-Playwright-E2E套件/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-900-Playwright-E2E套件/test-report.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-900-Playwright-E2E套件/handoff.md
```

## 禁止修改

```text
source/v3-agent-platform/**/*
docs/v3-agent中台/工程迭代/其他小工程/**/*
```

## 输入契约

`chat-minimal` 前端页面、API client、SSE client、测试账号 header、`testRunId` 隔离规则。

## 输出契约

`npm run test:e2e` 可在 Chromium 中完成 Chat 主流程验收。

## 参考依据要求

本任务只维护 UI E2E，不新增或修改 API、SSE、DTO、数据模型、表结构或状态机设计；不需要 `contract-notes.md`。

## 并行开发计划

是否启用多子 Agent 并行：否。

## 目标验收等级

L4；具备真实后端和真实 LLM 时可作为 L5 UI 证据之一。

## 必须执行的验证命令

```bash
cd source/v3-agent-platform-ui
npm run test:e2e
```

## 真实 E2E 要求

- 必须真实打开页面并通过 Playwright 操作 UI。
- 不允许只用 API 请求替代页面交互。
- 数据必须带 `testRunId`，避免污染人工数据。

## 风险

- 真实 LLM 输出时长和内容不稳定，断言必须围绕协议和 UI 状态，不绑定具体回答文本。
- 当前仅覆盖桌面 Chromium 主流程，未覆盖移动端和多浏览器矩阵。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
