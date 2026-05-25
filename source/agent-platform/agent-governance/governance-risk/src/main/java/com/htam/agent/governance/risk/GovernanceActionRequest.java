package com.htam.agent.governance.risk;

import java.util.Map;

public record GovernanceActionRequest(
        String runId,
        String stepId,
        String actor,
        String action,
        LayeredRiskPolicy riskPolicy,
        Map<String, Object> context) {

    public GovernanceActionRequest {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action 不能为空");
        }
        actor = actor == null || actor.isBlank() ? "agent-platform" : actor;
        riskPolicy = riskPolicy == null ? LayeredRiskPolicy.enterpriseDefault() : riskPolicy;
        context = context == null ? Map.of() : Map.copyOf(context);
    }
}
