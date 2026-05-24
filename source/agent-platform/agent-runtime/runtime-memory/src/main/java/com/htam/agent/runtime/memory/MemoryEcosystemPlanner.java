package com.htam.agent.runtime.memory;

import java.util.List;

public class MemoryEcosystemPlanner {

    public MemoryEcosystemPlan plan(VectorMemoryPlan vectorMemoryPlan, KnowledgeGraphPlan knowledgeGraphPlan) {
        VectorMemoryPlan vectorPlan = vectorMemoryPlan == null
                ? new VectorMemoryPlan(false, null, null, List.of())
                : vectorMemoryPlan;
        KnowledgeGraphPlan graphPlan = knowledgeGraphPlan == null
                ? new KnowledgeGraphPlan(false, false, null)
                : knowledgeGraphPlan;
        boolean crossSessionNetworkEnabled = graphPlan.enabled() && graphPlan.crossSessionNetworkEnabled();
        boolean humanReviewRequired = vectorPlan.scopes().contains(MemoryScope.ORGANIZATION)
                || crossSessionNetworkEnabled;
        return new MemoryEcosystemPlan(vectorPlan, graphPlan, crossSessionNetworkEnabled,
                humanReviewRequired, vectorPlan.scopes());
    }
}
