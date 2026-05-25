# TASK-V3-003 测试报告

## 状态

Review。

## 已执行命令

```bash
git diff --check
```

结果：通过，无输出。

```bash
find docs/v3-agent中台 AGENTS.md docs/AGENTS.md docs/README.md source/v3-agent-platform/AGENTS.md source/v3-agent-platform-ui/AGENTS.md -type f \( -name '*.md' -o -name 'AGENTS.md' \) -print0 | xargs -0 perl -ne 'if (/[ \t]$/) { print "$ARGV:$.: trailing whitespace\n" } if (/^(<<<<<<<|=======|>>>>>>>)($| )/) { print "$ARGV:$.: conflict marker\n" } close ARGV if eof'
```

结果：通过，无输出；新建和未跟踪 Markdown 文件未发现尾随空白或冲突标记。

```bash
rg -n --glob '!**/task.md' --glob '!**/test-report.md' "TODO|FIXME|TBD" docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/08-小工程切片与验收门禁.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构 || true
```

结果：通过，无输出，未发现待办占位。

```bash
rg -n --glob '!**/test-report.md' "04-API-SSE-运行时契约\\.md|05-数据模型与安全审计\\.md|06-测试策略与真实E2E\\.md|09-V3路线图|10-小工程切片|docs/v3-agent中台/ai-tasks|docs/v3-agent中台/adr/|poc-3|poc-4" docs AGENTS.md source/v3-agent-platform source/v3-agent-platform-ui || true
```

结果：通过，无输出；未发现旧文件名、旧顶层 `ai-tasks` / `adr` 路径或已移除的 `poc-3` / `poc-4` 引用。

```bash
rg -n --glob '!**/ai-tasks/**' "草稿|v3-架构重设" docs/README.md docs/v3-agent中台 AGENTS.md docs/AGENTS.md source/v3-agent-platform/AGENTS.md source/v3-agent-platform-ui/AGENTS.md || true
```

结果：通过，无输出；当前正式入口和 V3 文档不再引用草稿阶段或草稿目录。

```bash
rg -n "工程迭代/01-chat-minimal|01-chat-minimal" AGENTS.md docs/README.md docs/AGENTS.md source/v3-agent-platform/AGENTS.md source/v3-agent-platform-ui/AGENTS.md docs/v3-agent中台/templates/AI任务单-模板.md || true
```

结果：通过，无输出；通用阅读规则和任务模板不再默认绑定 `chat-minimal`。

```bash
rg -n --glob '!**/ai-tasks/**' "外部副作用工具|approvalRequired|业务门禁|runtime-sandbox / approval|sandbox 不能替代|沙箱不能替代" docs/v3-agent中台 || true
```

结果：通过，无输出；当前正式文档不再把真实业务副作用纳入 V3 sandbox / approval 门禁。

```bash
rg -n "系统级高风险|系统级动作必须进入|真实业务副作用动作不进入 V3 当前" docs/v3-agent中台/01-产品蓝图与体验目标.md docs/v3-agent中台/02-Agent中台技术架构方案.md docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构
```

结果：通过；确认 sandbox 口径已改为系统级执行风险判定，真实业务副作用动作不进入 V3 当前范围。

```bash
rg -n "^## |通用 Agent 与复杂 Agent" docs/v3-agent中台/01-产品蓝图与体验目标.md
```

结果：通过；产品蓝图中的 Agent 库、标签和类型分层已整合为 `## 3. Agent 库、标签与类型分层`，后续章节编号连续。

```bash
rg -n "维度化|tag_group|technical_capability|audience|标签只用于|不作为权限|不替代用户授权|标签过滤只影响|Agent 查询可以按|Agent 标签" docs/v3-agent中台/01-产品蓝图与体验目标.md docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构
```

结果：通过；Agent 标签模型已调整为维度化标签，并在产品、API、数据模型和任务记录中明确标签不替代权限、能力授权和运行时风险判定。

```bash
rg -n "全局技术架构候选基线|小工程架构使用方式|小工程技术设计归属|\\[Core\\]|\\[Ext\\]|\\[Off\\]|不承载具体小工程的实现切片" docs/v3-agent中台/02-Agent中台技术架构方案.md docs/v3-agent中台/README.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构
```

结果：通过；`02-Agent中台技术架构方案.md` 已调整为全局技术架构候选基线，只保留架构不变量、模块地图、依赖方向和变更规则，具体小工程内容下沉到小工程目录。

```bash
rg -n "chat-minimal|EPIC-V3-A|01-chat|\\[A\\]|当前架构切片|当前主链路|当前不进入" docs/v3-agent中台/02-Agent中台技术架构方案.md
```

结果：通过，无输出；`02-Agent中台技术架构方案.md` 不再出现具体小工程名称、首个工程编号或旧的当前激活标记。

```bash
rg -n "后端模块职责|核心链路基线|Capability Plan 链路|Runtime 路由链路|Stream 持久化链路|Run / Span / Audit 链路" docs/v3-agent中台/02-Agent中台技术架构方案.md docs/v3-agent中台/README.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构
```

结果：通过；后端模块职责和核心链路已合并入 `02-Agent中台技术架构方案.md`。

```bash
rg -n "03-后端模块与核心链路|后端模块与核心链路全局基线|\\[A\\]|\\[C\\]|\\[D\\]|chat-minimal 当前核心链路" AGENTS.md docs/README.md docs/AGENTS.md docs/v3-agent中台/README.md docs/v3-agent中台/RTK.md docs/v3-agent中台/02-Agent中台技术架构方案.md source/v3-agent-platform/AGENTS.md source/v3-agent-platform-ui/AGENTS.md
```

结果：通过，无输出；正式入口和全局技术架构基线不再引用已删除的 `03-后端模块与核心链路.md`，也不再使用旧的当前激活标记。

```bash
test ! -e docs/v3-agent中台/03-后端模块与核心链路.md
```

结果：通过；独立 `03-后端模块与核心链路.md` 已删除。

```bash
rg -n "GET  /api/agents/tags|tagGroup \\+ tagCode|标签后台管理|多标签复杂组合查询|标签分组查询" docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/工程迭代/01-chat-minimal docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构
```

结果：通过；`03-API-SSE-运行时契约-全局基线.md` 已维护 `GET /api/agents/tags` 的公共语义，`chat-minimal` 工程契约切片已冻结用户侧标签分组查询和单标签过滤，不包含标签后台管理或复杂组合查询。

```bash
rg -n "小工程 contract slice 维护规则|小工程 SSE event 维护规则|小工程错误码维护规则|小工程数据切片维护规则" docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
```

结果：通过；全局 `04` / `05` 不再维护 `chat-minimal` 详细冻结 API/SSE/错误码/表清单，详细冻结范围保留在 `工程迭代/01-chat-minimal/02-工程契约切片.md`。

```bash
find docs/v3-agent中台 -maxdepth 4 -type d | sort
```

结果：目录结构已收敛为顶层全局基线、`templates/`、`工程迭代/01-chat-minimal/`，其中 `ai-tasks` 只保留 `poc-0`、`poc-1`、`poc-2`、`cross-cutting`。

```bash
rg -n "V3 大工程|小工程：按业务可验收闭环拆分|并行开发就绪门禁|不建议把小工程拆成|任务包可以是横向技术切片" docs/v3-agent中台/08-小工程切片与验收门禁.md
```

结果：通过；`08-小工程切片与验收门禁.md` 已明确小工程按可验收业务闭环拆分，任务包才按多 Agent 并行边界拆分。

```bash
rg -n "## 5\\. EPIC-V3-A|### 5\\.1|## 6\\. 暂缓范围|## 7\\. 任务映射调整|TASK-V3-010|TASK-V3-024|L2：|L3：|L4：|L5：" docs/v3-agent中台/08-小工程切片与验收门禁.md || true
```

结果：通过，无输出；`08-小工程切片与验收门禁.md` 不再维护 `chat-minimal` 的详细验收门禁、任务映射或暂缓范围。

```bash
rg -n "已启动小工程索引|文档维护边界|具体小工程的范围|当前权威文档" docs/v3-agent中台/08-小工程切片与验收门禁.md
```

结果：通过；`08-小工程切片与验收门禁.md` 只保留小工程治理规则和已启动小工程索引，具体小工程的范围、契约、测试、路线图和暂缓范围下沉到 `工程迭代/<小工程>/`。

```bash
rg -n "子 Agent 并行开发必须发生|不允许为了并行开发|并行就绪条件|并行分工表" docs/v3-agent中台/06-AI-Native交付规约.md
```

结果：通过；`06-AI-Native交付规约.md` 已将多 Agent 并行限定在小工程内部的任务包中，并要求任务包记录并行分工表。

```bash
rg -n "并行开发计划|是否启用多子 Agent 并行|共享文件必须指定唯一 owner|POC-2 可以按多 Agent 并行范式" docs/v3-agent中台/templates/AI任务单-模板.md docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
```

结果：通过；任务模板已新增并行开发计划，`chat-minimal` POC-2 路线图已补充多 Agent 并行角色建议。

```bash
rg -n "OCR|input-preprocessor|platform-input|derivedContexts|input\\.ocr|agent_message_attachment|agent_input_derived_context|EPIC-V3-G|TASK-V3-050" docs/v3-agent中台/01-产品蓝图与体验目标.md docs/v3-agent中台/02-Agent中台技术架构方案.md docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/08-小工程切片与验收门禁.md docs/v3-agent中台/RTK.md docs/v3-agent中台/工程迭代/01-chat-minimal || true
```

结果：通过；全局产品、技术、API/SSE/Runtime、数据安全、小工程门禁和 RTK 已预留图片型输入 OCR / input-preprocessor 能力，`chat-minimal` 工程文档明确当前不实现该能力。

```bash
rg -n "文件内嵌图片|内嵌图片|sourceLocator|source_locator|parent_attachment|scanned_page|rendered_page" docs/v3-agent中台/01-产品蓝图与体验目标.md docs/v3-agent中台/02-Agent中台技术架构方案.md docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/04-数据模型与安全审计-全局基线.md docs/v3-agent中台/08-小工程切片与验收门禁.md docs/v3-agent中台/RTK.md docs/v3-agent中台/工程迭代/01-chat-minimal AGENTS.md source/v3-agent-platform/AGENTS.md || true
```

结果：通过；OCR 预留已覆盖独立图片、扫描页和上传文件内嵌图片，并在契约 / 数据模型中保留来源文件和定位信息。

```bash
rg -n "Codex|Dify|Docling|Unstructured|Knowledge Pipeline|partition_image" docs/v3-agent中台/02-Agent中台技术架构方案.md docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md docs/v3-agent中台/RTK.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构/contract-notes.md
```

结果：通过；已补充 Codex、Dify、Docling、Unstructured 的参考边界，明确 Codex 只作为输入体验和隐私边界参考，Dify / Docling / Unstructured 作为文档内图片抽取、OCR 策略和来源定位参考。

```bash
rg -n --glob '!**/ai-tasks/**' "03-后端模块与核心链路|04-API-SSE-运行时契约\\.md|05-数据模型与安全审计\\.md|06-测试策略与真实E2E\\.md|09-V3路线图|10-小工程|docs/v3-agent中台/ai-tasks|docs/v3-agent中台/adr/|poc-3|poc-4|草稿|v3-架构重设|Agent 目录" docs/v3-agent中台 docs/README.md docs/AGENTS.md AGENTS.md source/v3-agent-platform/AGENTS.md source/v3-agent-platform-ui/AGENTS.md || true
```

结果：通过；仅命中 `回复草稿` 作为业务交付物标签，不属于草稿阶段或草稿目录残留。

```bash
comm -23 <(perl -ne 'while(/TASK-V3-\d+/g){print "$&\n"}' docs/v3-agent中台/RTK.md | sort -u) <(perl -ne 'if(/^\| (TASK-V3-\d+) /){print "$1\n"}' docs/v3-agent中台/RTK.md | sort -u)
```

结果：通过，无输出；RTK 中被引用的任务号都已在任务追踪表登记。复核中发现并补齐了 `TASK-V3-030`、`TASK-V3-031`、`TASK-V3-033`、`TASK-V3-034`、`TASK-V3-040`、`TASK-V3-041`、`TASK-V3-042`、`TASK-V3-043` 的 Pending 行。

```bash
rg -n "图片型输入|文件内嵌图片|attachments.*为空|input\\.ocr|OCR 预处理|派生上下文" docs/v3-agent中台/工程迭代/01-chat-minimal/01-工程范围与验收目标.md docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md
```

结果：通过；`chat-minimal` 明确只接收文本消息，非空 `attachments` 可返回 `VALIDATION_FAILED`，不冻结 `input.ocr.*` 事件、附件专用错误码或 OCR 数据表。

```bash
rg -n "输入预处理|OCR provider|非多模态主模型|派生上下文" AGENTS.md source/v3-agent-platform/AGENTS.md
```

结果：通过；根目录和 V3 后端开发规则已补充输入预处理治理边界，禁止绕过平台直接调用 OCR provider 或把图片原件透传给非多模态主模型。

```bash
rg -n --glob '!**/task.md' --glob '!**/test-report.md' "TODO|FIXME|TBD" docs/v3-agent中台/06-AI-Native交付规约.md docs/v3-agent中台/08-小工程切片与验收门禁.md docs/v3-agent中台/templates/AI任务单-模板.md docs/v3-agent中台/工程迭代/01-chat-minimal/05-工程路线图与任务.md docs/v3-agent中台/工程迭代/01-chat-minimal/ai-tasks/poc-0/TASK-V3-003-V3小工程切片与门禁重构 || true
```

结果：通过，无输出；新增并行开发规则未引入待办占位。

## 结论

L0 文档检查通过。V3 已拆为顶层全局基线 + 共享模板 + 小工程迭代结构；`chat-minimal` 已拥有独立范围、契约切片、技术设计、测试策略、路线图、ADR 目录和任务包目录。POC-1 已创建任务单也已收窄到 `chat-minimal` 口径。通用入口和新任务模板已改为“全局入口 + 任务所属小工程”的阅读方式。sandbox 判定已收敛为系统级执行风险，真实业务副作用动作不进入 V3 当前范围。`GET /api/agents/tags` 的公共语义保留在全局基线，`chat-minimal` 的冻结范围保留在工程契约切片。`02-Agent中台技术架构方案.md` 已调整为不承载具体小工程内容的全局技术架构候选基线，并合并后端模块职责和核心链路；独立 `03-后端模块与核心链路.md` 已删除，避免双重权威。`08-小工程切片与验收门禁.md` 已收窄为治理规则和小工程索引，不再重复维护 `chat-minimal` 详细验收门禁。小工程技术设计是当前实现权威。小工程拆分已明确为纵向业务闭环，任务包和子 Agent 才按多 Agent 并行边界拆分。图片型输入 OCR 已作为后续 `input-ocr` 小工程预留，支持非多模态主模型通过派生上下文理解独立图片、扫描页和上传文件内嵌图片，但不扩大 `chat-minimal` 当前验收范围。整体复核中补齐了 RTK 后续任务号的 Pending 追踪行；未发现新的阻塞性文档问题。本任务触碰公共契约使用口径，当前进入 Review。
