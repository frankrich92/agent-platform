# TASK-V3-900 测试报告

## 状态

Review。

## 已执行命令

```bash
cd source/v3-agent-platform-ui
npm run test:e2e
```

结果：通过。Playwright Chromium 主流程 1 个用例通过。

```bash
cd source/v3-agent-platform-ui
HTAM_AGENT_FALLBACK_ONLY=false npm run test:e2e
```

结果：通过。真实后端 + 真实 LLM 场景下主流程通过。

## 环境信息

- OS / shell：macOS / zsh。
- Node：24.15.0。
- 浏览器：Playwright Chromium。
- 后端：`http://127.0.0.1:18080`。
- 前端：Vite dev server。
- 是否使用 `.env`：真实 LLM 场景使用 `.env`；报告不记录密钥。

## 覆盖范围

- 打开 Chat 工作台。
- 标签筛选 Agent。
- 新建会话。
- 发送消息并等待 Assistant 内容非空。
- 停止生成。
- 完成一轮流式回答并确认 `run-status` 进入终态。
- 查看执行详情。
- 重新生成。
- 编辑用户消息并重发。
- 与真实 LLM 输出兼容，不依赖固定回答文本。

## 用例统计

- 通过：1 个 Chromium 主流程。
- 失败：0。
- 跳过：0。
- 重试后通过：0。

## 未覆盖范围

- 未覆盖移动端 viewport。
- 未覆盖多浏览器矩阵。
- 未覆盖截图视觉回归。
- 未覆盖 Admin、任务中心、工作流和 input-ocr。

## 测试数据与隔离

- `testRunId`：由前端 store / API client 传递到后端。
- 数据清理方式：当前 UI E2E 侧不直接清理数据库，真实 DB 清理由 `TASK-V3-901` 的 DB E2E 脚本覆盖。
- 未清理风险：页面 E2E 可能留下带 `testRunId` 的会话数据，后续可在 CI hardening 中增加清理 fixture。

## 失败与重试

- 失败命令：无。
- 失败原因：无。
- 是否重试：否。
- 剩余风险：真实 LLM 延迟波动仍可能导致 UI E2E 超时，需要在 CI 中按真实环境调整 timeout。

## 结论

`chat-minimal` Playwright UI E2E 主流程进入 Review，可作为 L4 页面体验验收证据。
