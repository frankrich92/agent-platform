# TASK-V3-903 交接记录

## 状态

Review。

## 交接内容

已完成：

- 新增 `.github/workflows/chat-minimal-ci.yml`。
- CI 包含 `backend`、`frontend`、`ui-e2e` 三个 job。
- `backend` 使用 JDK 21 + Maven 4 RC 执行 `mvn -pl agent-boot -am test`。
- `frontend` 使用 Node 24 执行 `npm ci`、`npm test -- --run`、`npm run lint`、`npm run build`。
- `ui-e2e` 安装 Playwright Chromium，并执行 `npm run test:e2e`。
- 默认设置 `HTAM_AGENT_FALLBACK_ONLY=true`，避免 PR CI 依赖真实 LLM key 和 `.env`。

修改范围：

```text
.github/workflows/chat-minimal-ci.yml
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-903-CI质量门禁/
docs/v3-agent中台/RTK.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/README.md
```

未完成：

- 未接入 L5 real CI；真实 LLM / PostgreSQL / DB E2E 仍需手动或 self-hosted runner。
- 未接 Gitee CI。
- 未把 `TASK-V3-901` 的 DB E2E 脚本加入默认 PR CI。

风险：

- 首次 GitHub Actions 运行需要确认 Maven 4 RC archive 下载可用。
- Playwright browser install 依赖 GitHub hosted runner 网络。
- UI E2E 可能因 runner 性能导致首次启动超时，需要根据首轮 CI 结果调整 timeout。

已知问题 / workaround：

- 如果 Maven 4 下载不稳定，建议在 self-hosted runner 预装 Maven 4 RC，或改用内部制品缓存。
- 如果 Playwright E2E 耗时过长，可以把 `ui-e2e` 改为只在 PR label 或手动触发时运行，但当前先保持默认门禁。

是否触碰公共契约：否。

公共契约处理：

- 已合入主契约：不适用。
- 仍停留在任务内局部约定：不适用。
- 待人工确认：是否补充 L5 real self-hosted workflow。

未授权或共享文件变更：

- 是否发现未授权文件变更：否。
- 涉及文件：不适用。
- 处理建议：不适用。

验证结果：

```bash
git diff --check
ruby -e 'require "yaml"; YAML.load_file(".github/workflows/chat-minimal-ci.yml"); puts "yaml ok"'
curl -fsSI https://archive.apache.org/dist/maven/maven-4/4.0.0-rc-5/binaries/apache-maven-4.0.0-rc-5-bin.tar.gz
npm run lint
npm run build
```

结果：通过。workflow YAML 可解析，Maven 4 RC 下载地址可访问。本地后端 Maven、前端 Vitest 和 Playwright E2E 已在前序任务中验证通过；GitHub hosted runner 首轮结果待提交后观察。
