package com.htam.agent.repo.vector;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VectorRepository {

    VectorRecord upsert(VectorRecord record);

    Optional<VectorRecord> findById(String id);

    List<VectorSearchResult> search(float[] queryVector, int topK);

    List<VectorSearchResult> search(
            String collection,
            float[] queryVector,
            int topK,
            double minScore,
            Map<String, Object> filters);

    boolean delete(String id);
}
