# Docs Guidelines

本目录以中文文档为主。除上游接口、命令、代码符号、协议名和产品名外，新增和修改文档优先使用中文。

## 文档优先级

当前正式主线是 `docs/v3-agent中台/`。V1/V2 文档只作为历史需求、调研和演进参考，不要按 V1/V2 旧方案直接生成新代码。

修改文档前默认先读：

```text
docs/README.md
docs/v3-agent中台/README.md
docs/v3-agent中台/RTK.md
docs/v3-agent中台/06-AI-Native交付规约.md
docs/v3-agent中台/08-小工程切片与验收门禁.md
docs/v3-agent中台/工程迭代/README.md
```

涉及具体小工程时，按任务单声明的小工程补读 `工程迭代/<小工程>/README.md`、契约切片、测试策略和任务包索引。不要在通用文档任务里默认读取某个具体小工程。

## 修改规则

- 新增正式 V3 文档时，同步更新 `docs/README.md` 和 `docs/v3-agent中台/README.md`。
- 重大架构、安全、数据、契约决策应在对应小工程 `adr/` 下新增 ADR，不直接覆盖已确认结论。
- API、SSE、RuntimeEvent、错误码长期契约写入 `docs/v3-agent中台/03-API-SSE-运行时契约-全局基线.md`。
- 数据、安全、审计长期规则写入 `docs/v3-agent中台/04-数据模型与安全审计-全局基线.md`。
- 测试等级、Playwright、真实 E2E 规则写入 `docs/v3-agent中台/05-测试策略与验收等级-全局基线.md`。
- AI 任务单、并行开发、验收规约写入 `docs/v3-agent中台/06-AI-Native交付规约.md`。
- 需求、任务、知识、契约和测试的总追踪关系写入 `docs/v3-agent中台/RTK.md`。
- 执行过程记录、局部契约草案、测试报告和交接记录写入 `docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/poc-*` 或 `docs/v3-agent中台/工程迭代/<小工程>/ai-tasks/cross-cutting/`。

## 开源项目引用

涉及 Agent、MCP、Skills、SSE、运行时接口设计时，不要只写“参考某项目”。必须尽量精确到：

- 项目名称。
- 版本、commit 或“本地 fork 当前版本”；正式契约优先写明 commit。
- 源码路径或官方 endpoint。
- 类名、方法名、接口签名、API endpoint、事件名、DTO、schema、table、entity、状态枚举、字段名、关系和索引。
- 本项目采纳点、不采纳点和原因。

API、SSE、RuntimeEvent、错误码、DTO、数据模型、表结构或状态机设计不得只写“参考某项目”，必须说明实际参考的 API 或模型。

本地源码引用优先检查 `source/fork_source/` 和 `source/agentscope-demo/`。

## 格式与质量

- Markdown 标题层级保持稳定，避免频繁改名造成链接失效。
- 不要在文档中写入 `.env` 密钥、token、数据库密码或真实用户敏感数据。
- 不要大幅改写 V1/V2 历史正文；必要时只加历史提示或迁移说明。
- 提交前至少执行 Markdown 链接或路径检查、`git diff --check`。
