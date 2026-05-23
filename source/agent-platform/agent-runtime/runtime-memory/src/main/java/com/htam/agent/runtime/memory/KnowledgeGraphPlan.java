package com.htam.agent.runtime.memory;

public record KnowledgeGraphPlan(
        boolean enabled,
        boolean crossSessionNetworkEnabled,
        String graphStoreRef) {
}
