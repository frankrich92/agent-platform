# capability-rag

## 模块作用

RAG 能力模块，负责文档解析、分块、embedding、向量存储路由和本地检索服务。当前支持 pgvector、Milvus、Qdrant、Elasticsearch 和 Weaviate 向量存储。

## 代码结构

- `pom.xml`：Maven 模块声明，packaging 为 `jar`。
- `src/main/java`：当前包含 21 个 Java 源文件。
- 主要包：
  - `com.htam.agent.capability.knowledge.rag`
  - `com.htam.agent.capability.knowledge.rag.embedding`
  - `com.htam.agent.capability.knowledge.rag.parser`
  - `com.htam.agent.capability.knowledge.rag.parser.impl`
  - `com.htam.agent.capability.knowledge.rag.service`
  - `com.htam.agent.capability.knowledge.rag.store`
  - `com.htam.agent.capability.knowledge.rag.store.impl`
- 阅读入口：
  - `DocumentParser`
  - `EmbeddingRecord`
  - `EmbeddingService`
  - `RetrievalResult`
  - `VectorStoreConfig`
  - `BailianEmbeddingProvider`
  - `EmbeddingProvider`
  - `OllamaEmbeddingProvider`
  - `IParser`
  - `ExcelParser`
  - 其余 9 项见源码目录。

## 依赖边界

能力模块维护配置态和能力计划，运行时执行细节通过 runtime/worker/repo 边界协作。

## 阅读建议

先看 `com.htam.agent.capability.knowledge.rag` 下的服务接口或核心模型，再顺着实现类、仓储接口和测试用例理解运行链路。
