# TASK-V3-010 Maven 多模块骨架

## 状态

Review

## 背景

V3 后端需要在 `source/v3-agent-platform` 新建多模块骨架，和旧 `source/htam-agent-platform` 分离。

## 目标

- 创建 V3 Maven 父工程和 `chat-minimal` POC-1 所需模块目录。
- 后端工具链固定为 JDK 21、Spring Boot 4.0.6、Maven 4.x RC。
- 只引入必要基础依赖和插件，不写业务逻辑。
- 建立基础编译、测试、格式和依赖管理约束。
- 确保模块依赖方向为后续 architecture test 留出空间。

## 非目标

- 不实现 API。
- 不写数据库迁移。
- 不接入真实 AgentScope 运行链路。
- 不创建 Admin 完整治理、任务中心、工作流和外部复杂 Agent 实现模块。
- 不迁移旧后端业务代码。

## 必须阅读

```text
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/工程迭代/01-chat-minimal/02-工程契约切片.md
docs/v3-agent中台/工程迭代/01-chat-minimal/03-工程技术设计.md
docs/v3-agent中台/工程迭代/01-chat-minimal/adr/ADR-0003-Spring-Boot-4.0.6与Maven-4-RC工具链选型.md
docs/v3-agent中台/07-开发规范与本地环境.md
source/v3-agent-platform/AGENTS.md
```

## owned modules

```text
source/v3-agent-platform
```

## owned files

```text
source/v3-agent-platform/pom.xml
source/v3-agent-platform/**/pom.xml
source/v3-agent-platform/**/src/test/**
```

## 禁止修改

```text
source/htam-agent-platform/
source/htam-agent-platform-ui/
source/fork_source/
docs/v1-通用大模型对话/
docs/v2-通用大模型对话/
```

## 输入契约

V3 后端模块划分和依赖方向。

## 输出契约

可执行 `mvn test` 的 `chat-minimal` 空骨架工程，父 POM 明确锁定 Spring Boot 4.0.6，并按 ADR-0003 报告 Maven 4 RC 和核心依赖兼容性验证结果。

## 目标验收等级

L2

## 必须执行的验证命令

```bash
cd source/v3-agent-platform
mvn test
```

## 真实 E2E 要求

无。

## 风险

过早引入非必要依赖会增加后续模块耦合。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
