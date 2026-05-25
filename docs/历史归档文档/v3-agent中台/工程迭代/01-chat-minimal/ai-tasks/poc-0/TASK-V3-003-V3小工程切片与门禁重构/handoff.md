# TASK-V3-003 交接记录

## 状态

Review。

## 交接内容

已完成：

- 新增 `08-小工程切片与验收门禁.md`。
- 新增 `templates/` 共享模板目录。
- 新增 `工程迭代/` 和 `01-chat-minimal/` 小工程目录。
- 将 V3 总契约定位为全局候选基线，小工程只冻结自身 contract slice。
- 定义 `EPIC-V3-A chat-minimal` 的工程范围、契约切片、技术设计、测试策略和 L2-L5 验收门禁。
- 调整 `04` / `05` 主契约顶部口径：全局基线只维护公共语义和小工程切片规则，`chat-minimal` 详细冻结清单保留在工程契约切片。
- 更新 RTK、路线图、AGENTS 和任务索引，给后续 POC-1/POC-2 提供更小的评审口径。
- 将 POC-1 已创建任务单收窄到 `chat-minimal`，避免提前要求 Task、Workflow、ExternalAgent、Admin 完整实现模块。
- 删除当前正式入口文档里的草稿阶段 / 草稿目录描述。
- 将通用默认阅读路径改为“全局入口 + 任务所属小工程”，不再默认读取 `chat-minimal`。
- 统一 sandbox 判定口径：按本次执行动作的系统级风险决定是否进入 `runtime-sandbox` 或禁用；真实业务副作用不进入 V3 当前范围，后续作为独立优化项。
- 整合 `01-产品蓝图与体验目标.md` 中 Agent 库、标签和类型分层章节，减少产品蓝图内的重复章节。
- 优化 Agent 标签模型为维度化标签，并明确标签只用于检索、筛选、推荐和展示，不替代权限、能力授权和运行时风险判定。
- 将 `02-Agent中台技术架构方案.md` 调整为全局技术架构候选基线，承载架构不变量、目标模块地图、后端模块职责、依赖方向、核心链路、候选架构和变更规则；具体小工程内容下沉到小工程目录。
- 删除独立 `03-后端模块与核心链路.md`，避免后端模块和核心链路出现双重权威。
- 全局基线补充 `GET /api/agents/tags` 公共语义，并在 `chat-minimal` 工程契约切片中冻结用户侧标签分组查询和单标签过滤。
- 明确小工程按可验收业务闭环拆分，小工程内部阶段和任务包按多 Agent 并行范式拆分；任务模板已新增并行开发计划。
- 将 `08-小工程切片与验收门禁.md` 收窄为小工程治理规则和已启动小工程索引；`chat-minimal` 的详细目标、验收命令、任务列表和暂缓范围只维护在 `工程迭代/01-chat-minimal/`。
- 为后续图片型输入预留 OCR / input-preprocessor 能力：新增 `platform-input` 候选模块、附件 / 派生上下文契约、`input.ocr.*` 候选事件、附件 / OCR 错误码和 `EPIC-V3-G input-ocr` / `TASK-V3-050` 追踪项。
- 明确图片型输入包括独立图片、扫描页和上传文件内嵌图片，派生上下文必须保留原文件、页码 / sheet / slide / 图片序号 / 区域等来源定位。
- 明确 `chat-minimal` 当前只处理文本消息，图片型输入、文件内嵌图片抽取、OCR 预处理和派生上下文展示不进入当前实现或人工验收。
- 在根目录和 V3 后端 `AGENTS.md` 中补充输入预处理治理规则，禁止绕过平台直接调用 OCR provider 或把图片原件 / 文件内嵌图片透传给非多模态主模型。
- 补充 Codex、Dify、Docling、Unstructured 的参考边界：Codex 只作为输入体验和隐私边界参考，Dify / Docling / Unstructured 作为文档内图片抽取、OCR 策略和来源定位参考。
- 对 V3 文档做整体复核，补齐 RTK 中已被引用但未登记的后续任务号 `TASK-V3-030`、`TASK-V3-031`、`TASK-V3-033`、`TASK-V3-034`、`TASK-V3-040`、`TASK-V3-041`、`TASK-V3-042`、`TASK-V3-043`。

修改范围：

```text
docs/v3-agent中台/08-小工程切片与验收门禁.md
docs/README.md
docs/AGENTS.md
AGENTS.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/01-产品蓝图与体验目标.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/03-后端模块与核心链路.md（删除，内容合并入 02）
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/templates/
docs/v3-agent中台/工程迭代/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/
docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/README.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/task.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/contract-notes.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/test-report.md
docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/handoff.md
source/v3-agent-platform/AGENTS.md
source/v3-agent-platform-ui/AGENTS.md
```

未完成：

- 需要人工评估 `chat-minimal` 切片是否足够小且可验收。
- 如果人工确认，应继续创建并收窄 POC-2 任务包到 `chat-minimal` 的 owned files 和验证命令。
- 后续若启动 `input-ocr`，需要单独创建小工程目录、contract slice、任务包、文件内嵌图片抽取规则、OCR provider 健康检查和 Playwright 验收路径。
- 后续 `admin-config`、`task-center`、`workflow-sequential`、`external-agent` 启动前仍需分别创建小工程目录和真实任务包；RTK 目前只补齐 Pending 追踪行。

风险：

- 当前只是门禁和切片重构，尚未实际重写 POC-1 / POC-2 的任务单细节。
- OCR 目前仅做全局预留，未验证真实 provider、文件存储、内嵌图片抽取、附件清理和 OCR 准确率。

是否触碰公共契约：是，改变后续契约评审和使用口径，并追加附件 / OCR 候选字段、事件、错误码和数据对象；`chat-minimal` 当前冻结切片未扩大。
