package com.htam.agent.repo.vector;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryVectorRepository implements VectorRepository {

    private final CopyOnWriteArrayList<VectorRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public VectorRecord upsert(VectorRecord record) {
        records.removeIf(existing -> existing.id().equals(record.id()));
        records.add(record);
        return record;
    }

    @Override
    public Optional<VectorRecord> findById(String id) {
        return records.stream()
                .filter(record -> record.id().equals(id))
                .findFirst();
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
        if (queryVector == null || queryVector.length == 0 || topK <= 0) {
            return List.of();
        }
        Map<String, Object> filters = metadataFilter == null ? Map.of() : metadataFilter;
        return records.stream()
                .filter(record -> collection == null || collection.isBlank() || collection.equals(record.collection()))
                .filter(record -> record.vector().length == queryVector.length)
                .map(record -> new VectorSearchResult(record, cosineSimilarity(queryVector, record.vector())))
                .filter(result -> result.score() >= minScore)
                .filter(result -> metadataMatches(result.record(), filters))
                .sorted(Comparator.comparingDouble(VectorSearchResult::score).reversed())
                .limit(topK)
                .toList();
    }

    @Override
    public boolean delete(String id) {
        return records.removeIf(record -> record.id().equals(id));
    }

    private static boolean metadataMatches(VectorRecord record, Map<String, Object> filters) {
        return filters.entrySet().stream()
                .allMatch(entry -> entry.getValue().equals(record.metadata().get(entry.getKey())));
    }

    private static double cosineSimilarity(float[] left, float[] right) {
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
