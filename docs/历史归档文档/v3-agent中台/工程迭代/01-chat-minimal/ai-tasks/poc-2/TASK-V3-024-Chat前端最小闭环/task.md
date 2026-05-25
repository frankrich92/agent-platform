# TASK-V3-024 Chat 前端最小闭环

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

`chat-minimal` 必须让业务老师可以在真实页面完成 Agent 选择、会话、流式对话、停止、重新生成、编辑重发和执行详情查看。

## 目标

- 创建 `source/v3-agent-platform-ui` 前端工程。
- 实现 Chat 工作台首屏，而不是 landing page。
- 实现 API client、SSE client、Pinia 状态和 Playwright UI E2E。

## 非目标

- 不实现 Admin 后台、任务中心、工作流和 input-ocr 页面。

## owned modules

```text
source/v3-agent-platform-ui
```

## 输入契约

`agent-api` HTTP DTO、SSE envelope、错误响应、`Authorization`、`X-Test-Run-Id`。

## 输出契约

可通过 `npm run test:e2e` 操作的 Chat 工作台。

## 目标验收等级

L4-L5；当前达到 L4，真实 L5 依赖后端 `.env` 和真实持久化。

## 必须执行的验证命令

```bash
cd source/v3-agent-platform-ui
npm test
npm run build
npm run test:e2e
```

## 真实 E2E 要求

Playwright 必须启动后端和前端，并通过 UI 完成主流程。
