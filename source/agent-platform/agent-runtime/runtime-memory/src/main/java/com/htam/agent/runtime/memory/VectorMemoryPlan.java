package com.htam.agent.runtime.memory;

import java.util.List;

public record VectorMemoryPlan(
        boolean enabled,
        String vectorStore,
        String embeddingModel,
        List<MemoryScope> scopes) {

    public VectorMemoryPlan {
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
    }
}
