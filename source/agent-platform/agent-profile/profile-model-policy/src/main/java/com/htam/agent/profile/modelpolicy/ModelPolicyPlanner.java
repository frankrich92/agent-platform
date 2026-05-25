package com.htam.agent.profile.modelpolicy;

import java.util.Map;

public class ModelPolicyPlanner {

    public ModelPolicyPlan defaultPlan(Long agentId, String modelConfigRef) {
        return new ModelPolicyPlan(agentId, modelConfigRef, 6, true, false, Map.of());
    }
}
