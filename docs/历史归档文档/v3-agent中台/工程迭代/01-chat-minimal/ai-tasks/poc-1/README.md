# POC-1 任务包

POC-1 聚焦 `chat-minimal` 所需的后端模块骨架、公共 Java 契约和 architecture test。

阶段门禁：

- `source/v3-agent-platform` Maven 多模块骨架可编译。
- `agent-domain`、`agent-api`、`runtime-spi`、`repo-spi` 形成最小契约。
- architecture test 能约束模块依赖方向。
- 不实现真实 Chat 业务链路。
- 不创建 Admin 完整治理、任务中心、工作流和外部复杂 Agent 的实现模块。

当前任务状态：

```text
TASK-V3-010-Maven多模块骨架        Review
TASK-V3-011-agent-domain契约       Review
TASK-V3-012-runtime-spi契约        Review
TASK-V3-013-repo-spi契约           Review
TASK-V3-014-architecture-test      Review
TASK-V3-015-agent-api契约          Review
```

验收说明：

- POC-1 已通过 `source/v3-agent-platform` 下的 `mvn test`。
- `integration` profile 尚未创建；当前以默认 `mvn test` 承载模块编译、单元测试、API/SSE E2E 和架构边界测试。
