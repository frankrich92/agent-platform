# AI 任务单模板

## 状态

Planned / In Progress / Review / Done / Blocked / Superseded

## 所属小工程

```text
工程迭代/<小工程>
```

## 背景

说明为什么需要该任务，以及它属于哪个小工程。

## 目标

- 目标 1。
- 目标 2。

## 非目标

- 不做事项 1。
- 不做事项 2。

## 必须阅读

```text
全局规则（精读 / 了解）：
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/08-小工程切片与验收门禁.md

本小工程（精读 / 了解）：
docs/v3-agent中台/工程迭代/<小工程>/README.md
docs/v3-agent中台/工程迭代/<小工程>/01-工程范围与验收目标.md
docs/v3-agent中台/工程迭代/<小工程>/02-工程契约切片.md
docs/v3-agent中台/工程迭代/<小工程>/03-工程技术设计.md
docs/v3-agent中台/工程迭代/<小工程>/04-工程测试策略与真实E2E.md
docs/v3-agent中台/工程迭代/<小工程>/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/README.md

本任务（精读）：
docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/<阶段>/TASK-V3-xxx-*/task.md
```

禁止把其他小工程文档加入默认阅读路径；跨工程任务必须在本任务单中单独说明补读原因。

阅读级别说明：

- `精读`：执行前必须理解并遵守。
- `了解`：只作为背景，不得把未冻结内容扩大为当前任务范围。

## owned modules

```text
待填写
```

## owned files

```text
待填写
```

## 禁止修改

```text
待填写
```

## 输入契约

待填写。

## 输出契约

待填写。

## 参考依据要求

涉及 API、SSE、RuntimeEvent、错误码、DTO、数据模型、表结构或状态机设计时，必须在本任务 `contract-notes.md` 中写明：

- 参考项目、版本 / commit / 文档版本。
- 源码路径、官方 endpoint、schema、table、entity、DTO、字段或状态枚举。
- 本任务采纳点、不采纳点和原因。

不得只写“参考某开源项目”。

## 并行开发计划

是否启用多子 Agent 并行：是 / 否。

若启用，必须填写：

| 子 Agent | 职责 | owned modules | owned files | 依赖输入 | 输出物 | 验证命令 |
| --- | --- | --- | --- | --- | --- | --- |
| 待填写 | 待填写 | 待填写 | 待填写 | 待填写 | 待填写 | 待填写 |

规则：

- 小工程按可验收业务闭环拆分，子 Agent 只在本小工程任务包内并行。
- 并行前必须冻结本小工程当前阶段 contract slice。
- owned files 尽量互斥；共享文件必须指定唯一 owner。
- 公共契约由 contract owner、主 Agent 或 Review Agent 汇总回写，其他子 Agent 的细化先写入 `contract-notes.md`。
- E2E / Review 子 Agent 默认只验证和复核，不顺手改业务实现。
- `pom.xml`、Flyway 迁移、公共 DTO、公共枚举、测试 fixture、AGENTS 规则等共享文件必须声明唯一 owner。
- 发现未授权文件需要修改时，先停止该部分修改并写入交接记录，不直接顺手改。

## 目标验收等级

L0 / L1 / L2 / L3 / L4 / L5

## 必须执行的验证命令

```bash
待填写
```

## 真实 E2E 要求

待填写。

## 风险

待填写。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
