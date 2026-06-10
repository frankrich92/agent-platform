# adapter-rest

## 模块作用

REST 控制器模块，按 profile、run、capability、governance、worker 等领域组织 HTTP 入口。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 29 个 Java 源文件。
- 主要包：
  - `com.htam.agent.adapter.rest.capability.knowledge.controller`
  - `com.htam.agent.adapter.rest.capability.knowledge.rag.controller`
  - `com.htam.agent.adapter.rest.capability.mcp.controller`
  - `com.htam.agent.adapter.rest.capability.provider.controller`
  - `com.htam.agent.adapter.rest.capability.skill.controller`
  - `com.htam.agent.adapter.rest.capability.tool.controller`
  - `com.htam.agent.adapter.rest.capability.tool.hook.controller`
  - `com.htam.agent.adapter.rest.governance.iam.account.controller`
  - 其余 12 项见源码目录。
- 阅读入口：
  - `KnowledgeBaseConfigController`
  - `RagDocumentController`
  - `McpServerController`
  - `ModelConfigController`
  - `ModelProviderController`
  - `SkillFileController`
  - `SkillPackageController`
  - `ToolController`
  - `HookConfigController`
  - `AccountController`
  - 其余 19 项见源码目录。
- `src/test/java`：包含 1 个测试源文件，可用于理解模块当前验证重点。

## 依赖边界

适配器负责协议转换和参数校验，业务规则应下沉到 profile/run/capability/governance/worker 等模块。

## 阅读建议

先看 `com.htam.agent.adapter.rest.capability.knowledge.controller` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
