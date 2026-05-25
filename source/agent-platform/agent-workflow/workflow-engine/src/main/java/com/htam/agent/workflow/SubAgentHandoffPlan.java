package com.htam.agent.workflow;

import java.util.Map;

public record SubAgentHandoffPlan(
        Long parentAgentId,
        Long subAgentId,
        String parentRunId,
        String isolatedContextRef,
        String input,
        boolean summaryRequired,
        int currentDepth,
        int maxDepth,
        Map<String, Object> handoff) {

    public SubAgentHandoffPlan {
        input = input == null ? "" : input;
        currentDepth = Math.max(0, currentDepth);
        maxDepth = maxDepth <= 0 ? 3 : maxDepth;
        handoff = handoff == null ? Map.of() : Map.copyOf(handoff);
    }
}
