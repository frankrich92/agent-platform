# TASK-V3-014 architecture test

## 状态

Review

## 背景

多模块工程需要用自动化测试约束依赖方向，防止后续实现阶段模块边界失控。

## 目标

- 建立 architecture test，验证 `chat-minimal` 后端模块依赖方向。
- 禁止 `agent-domain` 依赖基础设施框架。
- 验证 `agent-api` 不依赖 `runtime-spi`，`RuntimeRequest` / `RuntimeEvent` 等运行时契约只归属 `runtime-spi`。
- 验证 `agent-biz`、`agent-platform`、`agent-runtimes`、`agent-repo`、`agent-api`、`agent-boot` 的依赖边界。
- 将测试纳入 Maven 默认验证路径。

最低 architecture test 规则：

```text
domain-no-spring
api-no-runtime-spi
biz-no-mapper
platform-no-biz-admin
runtime-no-repo
boot-no-core-business
repo-no-business-flow
```

## 非目标

- 不实现业务代码。
- 不调整已冻结公共契约。
- 不引入复杂 CI 配置。
- 不为未进入 `chat-minimal` 的 Admin、Task、Workflow、ExternalAgent 实现模块建立门禁。

## 必须阅读

```text
docs/v3-agent中台/02-Agent中台技术架构方案.md
docs/v3-agent中台/工程迭代/01-chat-minimal/03-工程技术设计.md
docs/v3-agent中台/07-开发规范与本地环境.md
source/v3-agent-platform/AGENTS.md
```

## owned modules

```text
source/v3-agent-platform
```

## owned files

```text
source/v3-agent-platform/**/src/test/**
source/v3-agent-platform/pom.xml
```

## 禁止修改

```text
source/v3-agent-platform/**/src/main/**
source/htam-agent-platform/
source/htam-agent-platform-ui/
```

## 输入契约

`chat-minimal` 模块依赖方向。

## 输出契约

可自动运行的 architecture test。

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

测试规则过宽会失去边界保护；规则过窄会阻碍合理实现。

## 完成后输出

- 已完成内容。
- 修改范围。
- 未完成内容。
- 测试结果。
- 风险。
- 是否触碰公共契约。
