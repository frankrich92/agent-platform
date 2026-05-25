# chat-minimal ADR

本目录保存 `EPIC-V3-A chat-minimal` 的真实架构决策。

共享模板：

```text
docs/v3-agent中台/templates/ADR-模板.md
```

规则：

- 一个 ADR 只记录一个重大决策。
- 决策影响多个小工程时，先在当前小工程记录，再回写 `docs/v3-agent中台/RTK.md` 和相关全局基线。
- 已 Accepted 的 ADR 不直接改语义；需要新 ADR supersede。

当前 ADR：

```text
ADR-0001-AgentScope普通Chat默认执行底座.md
ADR-0002-MyBatis-Plus与R2DBC持久化边界.md
ADR-0003-Spring-Boot-4.0.6与Maven-4-RC工具链选型.md
```
