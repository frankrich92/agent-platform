package com.htam.agent.core.rag.store;

import com.htam.agent.core.rag.EmbeddingRecord;
import com.htam.agent.core.rag.RetrievalResult;

import java.util.List;

/**
 * 向量存储接口，抽象向量数据库的核心操作
 *
 * @author huxuehao
 */
public interface VectorStore {

    boolean isAvailable();

    void storeEmbedding(Long id, Long chunkId, Long documentId,
                        Long knowledgeBaseConfigId, float[] embedding);

    void storeEmbeddings(List<EmbeddingRecord> records);

    List<RetrievalResult> search(float[] queryEmbedding, Long knowledgeBaseConfigId,
                                 int limit, double scoreThreshold);

    void deleteByDocumentId(Long documentId);

    void deleteByKnowledgeBaseConfigId(Long knowledgeBaseConfigId);

    void deleteByChunkId(Long chunkId);
}
