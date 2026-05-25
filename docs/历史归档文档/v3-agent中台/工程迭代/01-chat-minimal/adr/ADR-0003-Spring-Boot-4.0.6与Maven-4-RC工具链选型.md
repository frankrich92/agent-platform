# ADR-0003 Spring Boot 4.0.6 与 Maven 4 RC 工具链选型

## 状态

Accepted

## 日期

2026-05-19

## 背景

`chat-minimal` 的 POC-1 会创建 V3 后端 Maven 多模块骨架。工具链版本如果在任务执行前不固定，AI agent 会在父 POM、插件版本、依赖管理和验证命令上产生分叉。

当前人工决策为：

```text
JDK 21
Spring Boot 4.0.6
Maven 4.x RC
```

该组合比保守的 Spring Boot 3.x / Maven 3.9.x 风险更高，主要风险在 MyBatis-Plus、R2DBC、Flyway、测试插件和 Maven 4 RC 插件兼容性。由于用户已确认采用 Spring Boot 4.0.6、JDK 21、Maven 4.x RC，本 ADR 不再重新选型，只把兼容性验证和回退门禁写清楚。

## 决策

V3 后端 POC-1 以 JDK 21、Spring Boot 4.0.6、Maven 4.x RC 作为目标工具链基线。

`TASK-V3-010 Maven 多模块骨架` 必须在实际创建工程时完成以下验证：

- 父 POM 能解析 Spring Boot 4.0.6 依赖管理。
- `mvn test` 可在空骨架或最小测试下通过。
- MyBatis-Plus、R2DBC、Flyway、Testcontainers、JUnit、ArchUnit 等基础依赖如被引入，必须能完成依赖解析和最小测试。
- 若某个依赖未兼容 Spring Boot 4.0.6，不得静默降级 Spring Boot 或 Maven；必须在任务报告中标记 Blocked，并提出替代版本、影响范围和是否需要新 ADR supersede。

## 备选方案

方案 A：JDK 21 + Spring Boot 4.0.6 + Maven 4.x RC。

方案 B：JDK 21 + Spring Boot 3.x + Maven 3.9.x。

方案 C：先用 Spring Boot 3.x 创建骨架，后续单独升级到 Spring Boot 4。

本次采纳方案 A。方案 B / C 作为兼容性失败时的回退候选，不作为当前默认路线。

## 取舍分析

- 复杂度：方案 A 对依赖兼容性要求更高，需要 POC-1 明确验证门禁。
- 可维护性：统一 JDK / Spring Boot / Maven 基线后，任务单和 AGENTS 规则更清晰。
- 可测试性：Maven 4 RC 需要通过 `mvn test`、architecture test 和后续集成测试逐步验证。
- 安全性：工具链选择本身不改变安全边界，但失败时不能通过跳过测试或降级验证规避。
- AI Native 并行开发影响：版本固定后，子 Agent 不允许自行改父 POM 主版本；兼容性问题必须集中反馈给主 Agent 或 Review Agent。

## 影响范围

```text
影响模块：
source/v3-agent-platform

影响接口：
无直接 API / SSE 契约变更。

影响数据：
无直接数据模型变更。

影响测试：
TASK-V3-010、TASK-V3-014、后续所有 Maven 验证命令。

影响文档：
RTK.md
07-开发规范与本地环境.md
工程迭代/01-chat-minimal/ai-tasks/poc-1/TASK-V3-010-Maven多模块骨架/task.md
```

## 迁移策略

- POC-1 直接以该基线创建新工程，不从旧 `source/htam-agent-platform` 迁移 POM。
- 旧工程只作为实现参考，不继承旧工具链版本。
- 如果 Spring Boot 4.0.6 或 Maven 4 RC 在本地环境不可用，任务状态应标记为 Blocked，不能擅自改为 Spring Boot 3.x 或 Maven 3.9.x。
- 若人工确认回退，新增 ADR supersede 本决策，并同步更新 RTK、07 和相关任务单。

## 验收方式

```text
验收等级：
L2

验证命令：
cd source/v3-agent-platform
mvn test

E2E 场景：
无。POC-1 只验证骨架和契约测试。

人工评审点：
父 POM 是否锁定 Spring Boot 4.0.6。
Maven wrapper 或本地 Maven 是否指向 4.x RC。
是否记录依赖兼容性失败和处理结果。
```

## 后续事项

```text
后续事项：
TASK-V3-010 实测工具链。
TASK-V3-014 用 architecture test 验证模块边界。

风险：
第三方依赖对 Spring Boot 4.0.6 或 Maven 4 RC 的兼容性不足。

待确认：
实际工程创建后记录 MyBatis-Plus、R2DBC、Flyway、Testcontainers 的可用版本组合。
```
