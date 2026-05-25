# V3 Agent 中台正式文档索引

> 本目录是 V3 正式文档。AI agent 执行真实开发任务时，应先确认任务所属小工程，再从本索引选择阅读路径。

## 1. 文档清单

```text
docs/v3-agent中台/
├── README.md
├── RTK.md
├── 01-产品蓝图与体验目标.md
├── 02-Agent中台技术架构方案.md
├── 03-API-SSE-运行时契约-全局基线.md
├── 04-数据模型与安全审计-全局基线.md
├── 05-测试策略与验收等级-全局基线.md
├── 06-AI-Native交付规约.md
├── 07-开发规范与本地环境.md
├── 08-小工程切片与验收门禁.md
├── templates/
│   ├── ADR-模板.md
│   ├── AI任务单-模板.md
│   ├── Review-检查清单-模板.md
│   ├── 子Agent报告-模板.md
│   ├── 测试报告-模板.md
│   └── 交接记录-模板.md
└── 工程迭代/
    ├── README.md
    └── 01-chat-minimal/
        ├── README.md
        ├── 01-工程范围与验收目标.md
        ├── 02-工程契约切片.md
        ├── 03-工程技术设计.md
        ├── 04-工程测试策略与真实E2E.md
        ├── 05-工程路线图与任务.md
        ├── adr/
        │   ├── README.md
        │   ├── ADR-0001-AgentScope普通Chat默认执行底座.md
        │   ├── ADR-0002-MyBatis-Plus与R2DBC持久化边界.md
        │   └── ADR-0003-Spring-Boot-4.0.6与Maven-4-RC工具链选型.md
        └── ai-tasks/
            ├── README.md
            ├── poc-0/
            ├── poc-1/
            ├── poc-2/
            └── cross-cutting/
```

## 2. 阅读路径

所有新任务先读全局入口：

```text
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/01-产品蓝图与体验目标.md
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/templates/
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/08-小工程切片与验收门禁.md
docs/v3-agent中台/工程迭代/README.md
```

任务单必须声明所属小工程。确认小工程后，只补读该小工程内的文档：

```text
docs/v3-agent中台/工程迭代/<小工程>/README.md
docs/v3-agent中台/工程迭代/<小工程>/01-工程范围与验收目标.md
docs/v3-agent中台/工程迭代/<小工程>/02-工程契约切片.md
docs/v3-agent中台/工程迭代/<小工程>/03-工程技术设计.md
docs/v3-agent中台/工程迭代/<小工程>/04-工程测试策略与真实E2E.md
docs/v3-agent中台/工程迭代/<小工程>/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/README.md
```

不同小工程的文档不互相作为默认阅读材料。只有任务单明确声明跨工程影响时，才允许补读其他小工程文档，并必须说明原因。

说明：

- `02-Agent中台技术架构方案.md` 是全局技术架构候选基线，用于维护架构不变量、目标模块地图、后端模块职责、依赖方向、核心链路和后续候选链路，不维护具体小工程实现切片。
- 具体实现范围以任务所属小工程的 `03-工程技术设计.md` 为准，未进入小工程 contract slice 的模块不阻塞当前评审。

## 3. 按任务类型阅读

产品和前端体验：

```text
01-产品蓝图与体验目标.md
03-API-SSE-运行时契约-全局基线.md
05-测试策略与验收等级-全局基线.md
```

后端模块开发：

```text
02-Agent中台技术架构方案.md
03-API-SSE-运行时契约-全局基线.md
04-数据模型与安全审计-全局基线.md
```

运行时、AgentScope、MCP、Skills：

```text
02-Agent中台技术架构方案.md
03-API-SSE-运行时契约-全局基线.md
docs/调研/README.md
```

数据库、安全和审计：

```text
04-数据模型与安全审计-全局基线.md
05-测试策略与验收等级-全局基线.md
```

AI Native 开发：

```text
RTK.md
06-AI-Native交付规约.md
templates/
08-小工程切片与验收门禁.md
工程迭代/<小工程>/README.md
工程迭代/<小工程>/02-工程契约切片.md
工程迭代/<小工程>/05-工程路线图与任务.md
工程迭代/<小工程>/ai-tasks/README.md
07-开发规范与本地环境.md
```

## 4. 维护规则

- 约束只保留在一个权威位置，避免多份文档反复复制。
- 全局技术架构、后端模块职责、核心链路和依赖方向改 `02-Agent中台技术架构方案.md`。
- 全局候选接口契约改 `03-API-SSE-运行时契约-全局基线.md`。
- 全局候选数据、安全、审计规则改 `04-数据模型与安全审计-全局基线.md`。
- 全局测试等级、E2E、Playwright 规则改 `05-测试策略与验收等级-全局基线.md`。
- 新增 AI 任务单、并行开发、验收规约改 `06-AI-Native交付规约.md`。
- 新增小工程拆分、阶段门禁和跨工程边界改 `08-小工程切片与验收门禁.md`。
- 当前小工程的真实评审范围改 `工程迭代/<小工程>/02-工程契约切片.md`。
- 新增需求、任务、知识、契约和测试的总追踪关系改 `RTK.md`。
- 新增任务执行记录、局部契约草案、测试报告和交接证据放入 `工程迭代/<小工程>/ai-tasks/`。
- 共享模板放入 `templates/`；具体任务和具体 ADR 必须复制到对应小工程目录后再填写。
- 重大架构决策必须在对应小工程 `adr/` 下新增 ADR，不直接覆盖已确认结论。
