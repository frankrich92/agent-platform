package com.htam.agent.repo.knowledge;

import java.util.List;

public interface PgVectorRepository {
    boolean isAvailable();

    void storeEmbedding(Long id, Long chunkId, Long documentId, Long knowledgeBaseConfigId, float[] embedding);

    List<VectorSearchRecord> search(float[] queryEmbedding, Long knowledgeBaseConfigId, int limit);

    void deleteByDocumentId(Long documentId);

    void deleteByKnowledgeBaseConfigId(Long knowledgeBaseConfigId);

    void deleteByChunkId(Long chunkId);
}
