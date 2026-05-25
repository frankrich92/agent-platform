# HTAM Agent Platform 文档入口

> 本文件是项目文档入口。后续架构设计、代码开发、测试验收和 AI Native 协作，以 V3 Agent 中台正式文档为主。

## 1. 当前优先级

阅读优先级：

```text
1. docs/README.md
2. docs/v3-agent中台/README.md
3. docs/v3-agent中台/RTK.md
4. docs/v3-agent中台/01-产品蓝图与体验目标.md
5. docs/v3-agent中台/02-Agent中台技术架构方案.md
6. docs/v3-agent中台/06-AI-Native交付规约.md
7. docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
8. docs/v3-agent中台/08-小工程切片与验收门禁.md
9. docs/v3-agent中台/工程迭代/README.md
10. docs/调研/README.md
11. docs/v1-* / docs/v2-* 历史文档
```

任务单必须声明所属小工程。确认小工程后，只读该小工程目录下的范围、契约切片、技术设计、测试策略、路线图和任务包索引；不要把其他小工程文档加入默认阅读路径。

## 2. 版本定位

- V1 文档：早期通用大模型对话需求、技术方案、原型和实施计划，仅作为历史需求参考。
- V2 文档：Agent、记忆、Run、MCP、Skills 等增强设计，仅作为历史演进参考。
- V3 文档：当前优先架构基线，后续真实开发和 AI Native 交付均以 `docs/v3-agent中台/` 为准。
- V3 小工程：真实评审和实现按 `docs/v3-agent中台/工程迭代/<小工程>/` 推进；任务单声明哪个小工程，就只补读该小工程文档。
- 调研文档：解释 AgentScope Java、AgentScope Runtime Java、Hermes Agent 等方案取舍。

## 3. 当前基线结论

- 产品定位是企业内部 Agent 中台，不是单一聊天应用。
- 普通 Chat 默认执行底座是 `agentscope-java` 的 `ReActAgent`。
- 高风险执行底座是 `agentscope-runtime-java`。
- Hermes Agent 仅作为 API、Run、Capability 和 AI Native 方法参考，保留 `runtime-hermes` 占位，不作为 V3 默认执行底座。
- 通用 Agent 优先通过后台配置生成；复杂 Agent 可以通过外部 Agent 接入，但必须纳入目录、授权、审计和观测。
- 普通 CRUD 使用 MyBatis-Plus；SSE 高频 checkpoint / final flush 使用 R2DBC。
- UI E2E 必须通过 Playwright 真实操作页面验证，不能被 API E2E 替代。
- 真实开发必须先有 AI 任务单、owned modules / owned files 和目标验收等级。
- V3 真实代码根目录是 `source/v3-agent-platform` 和 `source/v3-agent-platform-ui`。
- 仓库级 AI 约束以根 `AGENTS.md`、`docs/AGENTS.md` 和 source 根目录下的 `AGENTS.md` 为准。
- `docs/v3-agent中台/RTK.md` 是需求、任务、知识、契约和测试的总追踪表。

## 4. 按任务阅读

产品、页面、体验：

```text
docs/v3-agent中台/01-产品蓝图与体验目标.md
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
```

后端架构、模块和运行链路：

```text
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md
docs/v3-agent中台/04-数据模型与安全审计-全局基线.md
```

AgentScope、Runtime、MCP、Skills、Hermes 取舍：

```text
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/调研/README.md
```

测试与验收：

```text
docs/v3-agent中台/05-测试策略与验收等级-全局基线.md
docs/v3-agent中台/06-AI-Native交付规约.md
```

AI Native 并行开发：

```text
docs/v3-agent中台/RTK.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/templates/
docs/v3-agent中台/08-小工程切片与验收门禁.md
docs/v3-agent中台/工程迭代/<小工程>/README.md
docs/v3-agent中台/工程迭代/<小工程>/02-工程契约切片.md
docs/v3-agent中台/工程迭代/<小工程>/05-工程路线图与任务.md
docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/README.md
```

开发环境和规范：

```text
docs/v3-agent中台/07-开发规范与本地环境.md
```

重大决策：

```text
docs/v3-agent中台/工程迭代/<小工程>/adr/
docs/v3-agent中台/templates/ADR-模板.md
```

AI 任务包：

```text
docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/
```
