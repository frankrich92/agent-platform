package com.htam.agent.repo.vector;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VectorRepositoryRouter implements VectorRepository {

    private final VectorRepository primary;
    private final VectorRepository fallback;

    public VectorRepositoryRouter(VectorRepository primary, VectorRepository fallback) {
        this.primary = primary;
        this.fallback = fallback == null ? new InMemoryVectorRepository() : fallback;
    }

    @Override
    public VectorRecord upsert(VectorRecord record) {
        return target().upsert(record);
    }

    @Override
    public Optional<VectorRecord> findById(String id) {
        return target().findById(id);
    }

    @Override
    public List<VectorSearchResult> search(float[] queryVector, int topK) {
        return target().search(queryVector, topK);
    }

    @Override
    public List<VectorSearchResult> search(
            String collection,
            float[] queryVector,
            int topK,
            double minScore,
            Map<String, Object> metadataFilter) {
        return target().search(collection, queryVector, topK, minScore, metadataFilter);
    }

    @Override
    public boolean delete(String id) {
        return target().delete(id);
    }

    private VectorRepository target() {
        return primary == null ? fallback : primary;
    }
}
