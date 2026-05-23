package com.htam.agent.core.rag.store.impl;

import com.htam.agent.core.rag.EmbeddingRecord;
import com.htam.agent.core.rag.RetrievalResult;
import com.htam.agent.core.rag.store.VectorStore;
import com.htam.agent.repo.knowledge.PgVectorRepository;
import com.htam.agent.repo.knowledge.VectorSearchRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PgVector向量存储服务
 *
 * @author huxuehao
 */
@Component
@ConditionalOnProperty(name = "rag.store", havingValue = "pgvector")
public class PgVectorStore implements VectorStore {
    private final PgVectorRepository pgVectorRepository;

    public PgVectorStore(PgVectorRepository pgVectorRepository) {
        this.pgVectorRepository = pgVectorRepository;
    }

    @Override
    public boolean isAvailable() {
        return pgVectorRepository.isAvailable();
    }

    @Override
    public void storeEmbedding(Long id, Long chunkId, Long documentId,
                               Long knowledgeBaseConfigId, float[] embedding) {
        if (!isAvailable()) {
            throw new RuntimeException("PgVector数据源未配置");
        }

        pgVectorRepository.storeEmbedding(id, chunkId, documentId, knowledgeBaseConfigId, embedding);
    }

    @Override
    public void storeEmbeddings(List<EmbeddingRecord> records) {
        if (!isAvailable()) {
            throw new RuntimeException("PgVector数据源未配置");
        }

        for (EmbeddingRecord record : records) {
            storeEmbedding(record.id(), record.chunkId(), record.documentId(),
                    record.knowledgeBaseConfigId(), record.embedding());
        }
    }

    @Override
    public List<RetrievalResult> search(float[] queryEmbedding, Long knowledgeBaseConfigId,
                                        int limit, double scoreThreshold) {
        if (!isAvailable()) {
            throw new RuntimeException("PgVector数据源未配置");
        }

        return pgVectorRepository.search(queryEmbedding, knowledgeBaseConfigId, limit)
                .stream()
                .filter(record -> record.score() >= scoreThreshold)
                .map(this::toRetrievalResult)
                .toList();
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        if (!isAvailable()) return;
        pgVectorRepository.deleteByDocumentId(documentId);
    }

    @Override
    public void deleteByKnowledgeBaseConfigId(Long knowledgeBaseConfigId) {
        if (!isAvailable()) return;
        pgVectorRepository.deleteByKnowledgeBaseConfigId(knowledgeBaseConfigId);
    }

    @Override
    public void deleteByChunkId(Long chunkId) {
        if (!isAvailable()) return;
        pgVectorRepository.deleteByChunkId(chunkId);
    }

    private RetrievalResult toRetrievalResult(VectorSearchRecord record) {
        return new RetrievalResult(record.chunkId(), record.documentId(), record.score());
    }
}
