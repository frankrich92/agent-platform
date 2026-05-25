# chat-minimal AI 任务包索引

本目录保存 `EPIC-V3-A chat-minimal` 的 AI Native 任务包。任务包用于约束多 Agent 并行开发，不替代正式架构文档；任务完成后的长期结论应回写到本工程文档或 V3 全局基线。

任务包状态的全局追踪见：

```text
docs/v3-agent中台/RTK.md
```

本工程范围见：

```text
docs/v3-agent中台/工程迭代/01-chat-minimal/01-工程范围与验收目标.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
```

## 任务包规则

目录按阶段拆分：

```text
ai-tasks/
├── README.md
├── poc-0/
├── poc-1/
├── poc-2/
└── cross-cutting/
```

阶段目录用途：

- `poc-0`：文档、公共契约、数据模型、安全审计冻结。
- `poc-1`：后端模块骨架、公共 Java 契约、architecture test。
- `poc-2`：Chat 主链路、AgentScope runtime、SSE、前端最小闭环。
- `cross-cutting`：Playwright、e2e-real、AGENTS、CI 等横切任务。

Admin 完整治理、任务中心、工作流和外部复杂 Agent 不在本工程任务包内；这些内容启动时应在 `工程迭代/` 下新建对应小工程。

每个任务目录至少包含：

```text
task.md
test-report.md
handoff.md
```

涉及 API、SSE、RuntimeEvent、错误码或公共 Java SPI 的任务，还必须包含：

```text
contract-notes.md
```

涉及 DTO、数据模型、表结构或状态机的任务同样必须包含 `contract-notes.md`。文件中必须写清：

- 本任务涉及的 HTTP API、SSE event、RuntimeEvent、错误码、DTO、数据模型、表结构和状态机。
- 参考项目、版本/commit、源码路径或官方 endpoint。
- 实际参考的 API、类、方法、接口签名、事件、字段、表结构或状态枚举。
- 本任务采纳点、不采纳点和原因。

不得使用“参考 AgentScope”“参考历史实现”“待执行时确认”替代具体 API / 模型依据；缺少依据的 API、DTO、模型和表结构设计不得进入 Review。

共享模板：

```text
docs/v3-agent中台/templates/AI任务单-模板.md
docs/v3-agent中台/templates/子Agent报告-模板.md
docs/v3-agent中台/templates/Review-检查清单-模板.md
docs/v3-agent中台/templates/测试报告-模板.md
docs/v3-agent中台/templates/交接记录-模板.md
```

## 状态说明

- `Planned`：任务单已创建，尚未开始实现。
- `In Progress`：已有 Agent 开始执行。
- `Blocked`：等待人工决策、外部依赖或前置任务。
- `Review`：实现完成，等待独立评审。
- `Done`：已完成验证和交接。
- `Superseded`：已被新任务或新决策替代。

## 当前任务

POC-0：

```text
poc-0/TASK-V3-000-文档索引复核
poc-0/TASK-V3-001-API-SSE-Runtime契约冻结
poc-0/TASK-V3-002-数据模型与安全审计冻结
poc-0/TASK-V3-003-V3小工程切片与门禁重构
```

POC-1：

```text
poc-1/TASK-V3-010-Maven多模块骨架
poc-1/TASK-V3-011-agent-domain契约
poc-1/TASK-V3-012-runtime-spi契约
poc-1/TASK-V3-013-repo-spi契约
poc-1/TASK-V3-014-architecture-test
poc-1/TASK-V3-015-agent-api契约
```

POC-2：

```text
poc-2/TASK-V3-020-runtime-agentscope最小适配
poc-2/TASK-V3-021-platform-capability最小gate
poc-2/TASK-V3-022-biz-chat主流程
poc-2/TASK-V3-023-stream-persistence
poc-2/TASK-V3-024-Chat前端最小闭环
```

Cross-cutting：

```text
cross-cutting/TASK-V3-902-AGENTS分层规则
```

POC-0 未评审通过前，不进入真实代码开发。公共契约未冻结前，不启动多个子 Agent 并行写代码。

V3 后续按小工程切片推进。`03-API-SSE-运行时契约-全局基线.md` 和 `04-数据模型与安全审计-全局基线.md` 是全局候选基线；`02-工程契约切片.md` 是 `EPIC-V3-A chat-minimal` 的当前评审和实现范围。
