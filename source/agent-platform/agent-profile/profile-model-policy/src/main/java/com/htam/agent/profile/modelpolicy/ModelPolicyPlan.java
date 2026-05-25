package com.htam.agent.profile.modelpolicy;

import java.util.Map;

public record ModelPolicyPlan(
        Long agentId,
        String modelConfigRef,
        int maxIterations,
        boolean planningEnabled,
        boolean planConfirmationRequired,
        Map<String, Object> overrides) {

    public ModelPolicyPlan {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        modelConfigRef = modelConfigRef == null ? "" : modelConfigRef;
        maxIterations = maxIterations <= 0 ? 6 : maxIterations;
        overrides = overrides == null ? Map.of() : Map.copyOf(overrides);
    }
}
