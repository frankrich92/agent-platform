# TASK-V3-003 V3 小工程切片与门禁重构

## 状态

Review

## 背景

V3 是完整 Agent 中台大工程。当前 POC 按完整产品形态冻结契约，人工评审范围过大，难以判断每一步是否正确。需要在保留 V3 总蓝图的前提下，拆出可独立交付的小工程，并先聚焦 `chat-minimal`。

## 目标

- 新增 V3 小工程切片和验收门禁文档。
- 将顶层 V3 文档结构调整为全局基线、共享模板和小工程迭代目录。
- 明确全局契约是候选基线，小工程只冻结自身 contract slice。
- 定义首个小工程 `EPIC-V3-A chat-minimal` 的范围、工程契约切片、技术设计、测试策略和验收门禁。
- 调整 RTK、路线图、任务索引，避免 POC-1/POC-2 被完整 V3 范围绑死。
- 删除当前正式文档中关于草稿阶段或草稿目录的描述。
- 重新梳理新任务的必须阅读规则，确保不同小工程不互相干扰。
- 调整 sandbox 判定口径：按本次执行动作的系统级风险判定，真实业务副作用不进入 V3 当前范围。
- 优化 Agent 标签模型：采用维度化标签，并明确标签不替代权限、能力授权和风险判定。
- 将 `02-Agent中台技术架构方案.md` 调整为全局技术架构候选基线，明确具体小工程内容只写入小工程技术设计、测试策略和任务包。
- 将后端模块职责和核心链路合并入 `02-Agent中台技术架构方案.md`，下线独立 `03-后端模块与核心链路.md`，避免全局技术架构双重权威。
- 明确 `chat-minimal` 纳入用户侧 Agent 标签分组查询和单标签过滤，但不实现标签后台管理或复杂组合查询。
- 明确小工程按可验收业务闭环拆分，小工程内部阶段和任务包按多 Agent 并行范式执行。
- 为后续图片型输入预留 OCR / input-preprocessor 能力，使非多模态主模型可通过派生上下文理解独立图片、扫描页和上传文件内嵌图片信息，但不纳入 `chat-minimal` 当前实现范围。

## 非目标

- 不实现代码。
- 不创建 Maven / Node 工程。
- 不废弃 V3 总蓝图。
- 不重写已冻结的 `04` / `05` 主契约，只改变其后续使用口径。
- 不实现图片型输入上传、文件内嵌图片抽取、OCR provider 适配或派生上下文持久化代码。

## 必须阅读

```text
docs/v3-agent中台/RTK.md
docs/v3-agent中台/README.md
docs/v3-agent中台/01-产品蓝图与体验目标.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/08-小工程切片与验收门禁.md
docs/v3-agent中台/工程迭代/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/README.md
```

## owned modules

```text
docs
AGENTS
```

## owned files

```text
docs/v3-agent中台/08-小工程切片与验收门禁.md
docs/README.md
docs/AGENTS.md
AGENTS.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/01-产品蓝图与体验目标.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/templates/**
docs/v3-agent中台/工程迭代/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/**
source/v3-agent-platform/AGENTS.md
source/v3-agent-platform-ui/AGENTS.md
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/**
```

## 禁止修改

```text
source/ 下除 source/v3-agent-platform/AGENTS.md 和 source/v3-agent-platform-ui/AGENTS.md 外的文件
docs/v1-通用大模型对话/
docs/v2-通用大模型对话/
```

## 输入契约

现有 V3 总蓝图、API/SSE/Runtime 候选基线、数据模型 / 安全 / 审计候选基线。

## 输出契约

小工程切片规则、共享模板结构、`chat-minimal` contract slice、系统级高风险执行的 sandbox 判定口径、Agent 维度化标签口径、`chat-minimal` 标签接口边界、全局架构基线和小工程技术设计的归属边界、小工程内多 Agent 并行开发门禁、后端模块 / 核心链路全局基线，以及后续 `input-ocr` 小工程对独立图片、扫描页和文件内嵌图片的全局预留边界。

## 目标验收等级

L0

## 必须执行的验证命令

```bash
git diff --check
rg -n --glob '!**/task.md' --glob '!**/test-report.md' "TODO|FIXME|TBD" docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/08-小工程切片与验收门禁.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构 || true
rg -n --glob '!**/ai-tasks/**' "草稿|v3-架构重设" docs/README.md docs/v3-agent中台 AGENTS.md docs/AGENTS.md source/v3-agent-platform/AGENTS.md source/v3-agent-platform-ui/AGENTS.md || true
rg -n "工程迭代/01-chat-minimal|01-chat-minimal" AGENTS.md docs/README.md docs/AGENTS.md source/v3-agent-platform/AGENTS.md source/v3-agent-platform-ui/AGENTS.md docs/v3-agent中台/templates/AI任务单-模板.md || true
rg -n --glob '!**/ai-tasks/**' "外部副作用工具|approvalRequired|业务门禁|runtime-sandbox / approval|sandbox 不能替代|沙箱不能替代" docs/v3-agent中台 || true
rg -n "OCR|input-preprocessor|platform-input|derivedContexts|input\\.ocr|agent_message_attachment|agent_input_derived_context|EPIC-V3-G|TASK-V3-050" docs/v3-agent中台/01-产品蓝图与体验目标.md docs/v3-agent中台/02-Agent中台技术架构方案.md docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/08-小工程切片与验收门禁.md docs/v3-agent中台/RTK.md docs/v3-agent中台/工程迭代/01-chat-minimal || true
```

## 真实 E2E 要求

无。

## 风险

如果切片过粗，人工评审仍然难以把控；如果切片过细，会导致跨任务契约反复变更。当前先以 `chat-minimal` 作为首个可运行闭环。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
