package com.htam.agent.repo.vector;

import java.util.Map;

public record VectorRecord(String id, String collection, float[] vector, Map<String, Object> metadata) {

    public VectorRecord {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id 不能为空");
        }
        if (collection == null || collection.isBlank()) {
            throw new IllegalArgumentException("collection 不能为空");
        }
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("vector 不能为空");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
