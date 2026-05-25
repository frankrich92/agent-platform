# TASK-V3-024 contract notes

## 涉及契约

- API client：`/api/agents`、`/api/agents/tags`、`/api/sessions`、`/api/chat/send`、`/api/chat/{runId}/stream`、`/api/chat/{runId}/cancel`、`/api/executions/{runId}`。
- SSE parser：按 `id:`、`event:`、`data:` 解析平台 SSE envelope。
- UI E2E：稳定 `data-testid`。

## 参考依据

| 来源 | 版本 / commit | 路径或 API | 采纳点 | 不采纳点 | 原因 |
| --- | --- | --- | --- | --- | --- |
| 本地 V2 前端 | 当前工作区 | `source/htam-agent-platform-ui/src/api.ts` | 采纳 `fetch` + `ReadableStream` + `AbortController` 的 SSE 客户端方式 | 不沿用旧页面结构和旧路径 | V3 需要 chat-minimal 独立工作台和 send / stream 两步模型 |
| Vue / Pinia / Element Plus | 当前 npm lockfile | `vue@3`、`pinia`、`element-plus`、`@element-plus/icons-vue` | 采纳组合式状态、常规表单/按钮/标签组件、图标按钮 | 不做营销页或装饰性 hero | 本工程目标是高频工作台 |
| Playwright | 当前 npm lockfile | `@playwright/test`、`page.getByTestId(...)` | 采纳真实浏览器 UI E2E 和稳定 test id | 不用 API E2E 替代 UI E2E | L4 要求真实操作页面 |

## 当前取舍

- 前端 token 和 `testRunId` 暂存在 localStorage，仅用于 POC。
- SSE client 使用 `fetch` 读取流，便于携带 Authorization 和 Last-Event-ID。
