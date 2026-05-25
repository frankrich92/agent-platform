# ADR-0002 MyBatis-Plus 与 R2DBC 持久化边界

## 状态

Accepted

## 日期

2026-05-19

## 背景

`chat-minimal` 同时需要普通业务 CRUD、历史查询和 SSE 高频 checkpoint / final flush。普通 CRUD 适合使用 MyBatis-Plus，SSE 高频写入如果走阻塞式 MyBatis 链路，容易影响流式响应和并发恢复。

## 决策

V3 后端采用双持久化边界：

- `repo-mybatis` 负责普通 CRUD、后台配置、历史查询、分页查询和 final flush 后的结果读取。
- `repo-r2dbc-stream` 负责 SSE checkpoint、assistant message 累积内容更新、run / span / message final flush、流式状态更新和幂等 sequence 写入。
- `repo-spi` 定义端口，`agent-biz` 和 `agent-admin` 不直接依赖 Mapper、R2DBC Client 或具体持久化实现。
- MyBatis-Plus 与 R2DBC 不做跨技术事务，依靠 `runId`、`messageId`、`spanId`、`sequence`、状态机和 final flush 收敛一致性。

## 备选方案

- 全部使用 MyBatis-Plus：实现简单，但不适合 SSE 高频 checkpoint。
- 全部使用 R2DBC：流式链路统一，但会增加普通 CRUD 和后台管理实现复杂度。
- 引入消息队列或事件存储：长期可选，但对 `chat-minimal` POC 过重。

## 取舍分析

双边界能让普通 CRUD 和流式 checkpoint 各自使用合适技术，同时通过 `repo-spi` 避免业务层感知技术细节。

不做跨技术事务是刻意取舍：`chat-minimal` 更需要可观测、可恢复和最终收敛，而不是把流式 token 写入和历史查询强行包进一个事务模型。

## 影响范围

- `02-Agent中台技术架构方案.md`
- `04-数据模型与安全审计-全局基线.md`
- `工程迭代/01-chat-minimal/02-工程契约切片.md`
- `TASK-V3-013-repo-spi契约`
- `TASK-V3-023-stream-persistence`

## 验收方式

- POC-1：`repo-spi` 区分普通业务仓储和 stream checkpoint / final flush 仓储。
- POC-2：API/SSE E2E 覆盖 `Last-Event-ID` 恢复、checkpoint 幂等、final flush 和 MyBatis 历史查询可见。
- L5：真实链路中 Chat SSE、消息落库、Run/Span 和执行详情可互相追踪。

## 后续事项

- 如后续引入消息队列、事件存储或统一响应式持久化，需要新增 ADR supersede 本决策。
