# TASK-V3-903 CI 质量门禁

## 状态

Review

## 所属小工程

```text
工程迭代/01-chat-minimal
```

## 背景

`chat-minimal` 已完成后端 API/SSE、前端 Playwright 和真实 DB E2E 验证。为了让后续 PR 不依赖人工逐条执行命令，需要先接入稳定的默认 CI 门禁。

## 目标

- 新增 GitHub Actions 默认 CI workflow。
- PR / push 默认执行后端 Maven 测试、前端单测 / 类型检查 / production build、Playwright UI E2E。
- 默认 CI 不依赖 `.env`、真实 LLM key 或真实 PostgreSQL。
- 保留 L5 real E2E 为手动 / self-hosted 后续门禁，不阻塞普通 PR。

## 非目标

- 不接入真实 LLM / DB 的默认 PR CI。
- 不配置 Gitee、Jenkins 或 GitLab CI。
- 不实现 CI 自动数据库清理脚本。
- 不修改业务代码。

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
docs/v3-agent中台/工程迭代/01-chat-minimal/04-工程测试策略与真实E2E.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/README.md

本任务（精读）：
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-903-CI质量门禁/task.md
```

## owned modules

```text
.github/workflows
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-903-CI质量门禁
```

## owned files

```text
.github/workflows/chat-minimal-ci.yml
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-903-CI质量门禁/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-903-CI质量门禁/test-report.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/TASK-V3-903-CI质量门禁/handoff.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/cross-cutting/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/RTK.md
```

## 禁止修改

```text
source/v3-agent-platform/**/*
source/v3-agent-platform-ui/**/*
docs/v3-agent中台/工程迭代/其他小工程/**/*
```

## 输入契约

现有本地验证命令：

```bash
cd source/v3-agent-platform && mvn -pl agent-boot -am test
cd source/v3-agent-platform-ui && npm test -- --run
cd source/v3-agent-platform-ui && npm run lint
cd source/v3-agent-platform-ui && npm run build
cd source/v3-agent-platform-ui && npm run test:e2e
```

## 输出契约

GitHub Actions workflow：

```text
.github/workflows/chat-minimal-ci.yml
```

## 参考依据要求

本任务只接入 CI，不新增或修改 API、SSE、DTO、数据模型、表结构或状态机设计；不需要 `contract-notes.md`。

## 并行开发计划

是否启用多子 Agent 并行：否。

## 目标验收等级

L2-L4。

## 必须执行的验证命令

```bash
git diff --check
ruby -e 'require "yaml"; YAML.load_file(".github/workflows/chat-minimal-ci.yml"); puts "yaml ok"'
curl -fsSI https://archive.apache.org/dist/maven/maven-4/4.0.0-rc-5/binaries/apache-maven-4.0.0-rc-5-bin.tar.gz
npm run lint
npm run build
npm run test:e2e
mvn -pl agent-boot -am test
```

本地无法真正触发 GitHub hosted runner；最终以 GitHub Actions 运行结果为准。

## 真实 E2E 要求

- 默认 PR CI 使用 `HTAM_AGENT_FALLBACK_ONLY=true`，不消耗真实 LLM。
- L5 real DB E2E 仍由 `TASK-V3-901` 脚本手动执行，后续如接 self-hosted runner 再独立新增 workflow 或扩展本任务。

## 风险

- GitHub hosted runner 网络访问 Maven Central、npm registry、Playwright browser download 可能波动。
- Maven 4 RC 通过 Apache archive 下载；如果 archive URL 不可用，需要改为内部缓存或 self-hosted runner 预装。
- Playwright UI E2E 启动后端时仍会编译 Maven 模块，首次 CI 运行耗时较长。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
