# profile-version

## 模块作用

Profile 版本发布模块，描述版本状态、灰度策略、回放对比、发布门禁和评估样本。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 8 个 Java 源文件。
- 主要包：
  - `com.htam.agent.profile.version`
- 阅读入口：
  - `ProfileEvalSample`
  - `ProfileGrayPolicy`
  - `ProfileReleaseAssessment`
  - `ProfileReleaseGate`
  - `ProfileReleasePlanner`
  - `ProfileReplayComparison`
  - `ProfileVersion`
  - `ProfileVersionStatus`

## 依赖边界

Profile 模块聚合智能体配置态，不直接暴露 AgentScope 等具体运行时对象。

## 阅读建议

先看 `com.htam.agent.profile.version` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
