# agent-capability

## 模块作用

能力聚合层，管理模型、工具、MCP、Skill、知识库、RAG、连接器和能力目录。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `pom`。
- 子模块：
  - `capability-core`
  - `capability-tool`
  - `capability-mcp`
  - `capability-skill`
  - `capability-provider`
  - `capability-knowledge`
  - `capability-rag`
  - `capability-connector`
  - `capability-registry`
- 当前无 Java 源码，主要作为聚合 POM 或后续能力预留边界。

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先从本 README 的子模块列表了解职责拆分，再进入具体子模块查看服务、接口或适配实现。
