package com.htam.agent.runtime.memory;

import java.util.List;

public record MemoryEcosystemPlan(
        VectorMemoryPlan vectorMemoryPlan,
        KnowledgeGraphPlan knowledgeGraphPlan,
        boolean crossSessionNetworkEnabled,
        boolean humanReviewRequired,
        List<MemoryScope> allowedScopes) {

    public MemoryEcosystemPlan {
        allowedScopes = allowedScopes == null ? List.of() : List.copyOf(allowedScopes);
    }
}
