package com.htam.agent.repo.vector;

import com.htam.agent.repo.knowledge.PgVectorRepository;
import com.htam.agent.repo.knowledge.VectorSearchRecord;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PgVectorRepositoryAdapter implements VectorRepository {

    private final PgVectorRepository delegate;

    public PgVectorRepositoryAdapter(PgVectorRepository delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate 不能为空");
        }
        this.delegate = delegate;
    }

    @Override
    public VectorRecord upsert(VectorRecord record) {
        ensureAvailable();
        delegate.storeEmbedding(
                longValue(record.id(), record.metadata().get("id")),
                requiredLong(record.metadata(), "chunkId"),
                requiredLong(record.metadata(), "documentId"),
                knowledgeBaseId(record.collection(), record.metadata()),
                record.vector());
        return record;
    }

    @Override
    public Optional<VectorRecord> findById(String id) {
        return Optional.empty();
    }

    @Override
    public List<VectorSearchResult> search(float[] queryVector, int topK) {
        return search(null, queryVector, topK, Double.NEGATIVE_INFINITY, Map.of());
    }

    @Override
    public List<VectorSearchResult> search(
            String collection,
            float[] queryVector,
            int topK,
            double minScore,
            Map<String, Object> metadataFilter) {
        if (!delegate.isAvailable()) {
            return List.of();
        }
        Long knowledgeBaseConfigId = knowledgeBaseId(collection, metadataFilter);
        return delegate.search(queryVector, knowledgeBaseConfigId, topK).stream()
                .filter(record -> record.score() >= minScore)
                .map(record -> new VectorSearchResult(toVectorRecord(collection, knowledgeBaseConfigId, record), record.score()))
                .toList();
    }

    @Override
    public boolean delete(String id) {
        ensureAvailable();
        delegate.deleteByChunkId(Long.parseLong(id));
        return true;
    }

    private VectorRecord toVectorRecord(String collection, Long knowledgeBaseConfigId, VectorSearchRecord record) {
        return new VectorRecord(
                String.valueOf(record.chunkId()),
                collection == null || collection.isBlank() ? String.valueOf(knowledgeBaseConfigId) : collection,
                new float[] {0.0f},
                Map.of(
                        "chunkId", record.chunkId(),
                        "documentId", record.documentId(),
                        "knowledgeBaseConfigId", knowledgeBaseConfigId));
    }

    private void ensureAvailable() {
        if (!delegate.isAvailable()) {
            throw new IllegalStateException("PgVectorRepository 不可用");
        }
    }

    private static Long knowledgeBaseId(String collection, Map<String, Object> metadata) {
        Object value = metadata == null ? null : metadata.get("knowledgeBaseConfigId");
        if (value != null) {
            return requiredLong(metadata, "knowledgeBaseConfigId");
        }
        if (collection == null || collection.isBlank()) {
            throw new IllegalArgumentException("collection 或 knowledgeBaseConfigId 不能为空");
        }
        return Long.parseLong(collection.replace("kb:", ""));
    }

    private static Long requiredLong(Map<String, Object> metadata, String key) {
        if (metadata == null || !metadata.containsKey(key)) {
            throw new IllegalArgumentException(key + " 不能为空");
        }
        return longValue(String.valueOf(metadata.get(key)), metadata.get(key));
    }

    private static Long longValue(String fallback, Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value == null ? fallback : value));
    }
}
