# TASK-V3-020 测试报告

## 状态

Review。

## 已执行命令

```bash
cd source/v3-agent-platform
mvn test
```

结果：通过。API/SSE E2E 通过 deterministic runtime 覆盖流式 delta、done、cancel 和失败边界映射。

```bash
set -a
source /Volumes/ppj/project/htam-agent-platform/source/v3-agent-platform/.env
set +a
export HTAM_AGENT_FALLBACK_ONLY=false
mvn -pl agent-boot -am spring-boot:run -Dspring-boot.run.arguments=--server.port=18080
```

结果：通过。真实 `.env` 下 `runtime-agentscope` 使用 AgentScope Java `ReActAgent` 和真实 LLM provider 完成 Chat run，API/SSE 验证结果为 `status=COMPLETED`、`assistant.delta=9`、`assistant.done=1`、`assistant.thinking=0`、`duplicateEventIds=0`。

## 环境信息

- OS / shell：macOS / zsh。
- JDK / Maven：JDK 21.0.11-tem / Maven 4.0.0-rc-5。
- 是否使用 `.env`：默认回归不使用；L5 使用本地 `.env`，未输出或提交密钥。

## 未覆盖范围

- 未覆盖 tool calling；`chat-minimal` L5 不强制 tool calling。

## 结论

runtime-agentscope 最小适配进入 Review；真实 L5 已补测通过。
