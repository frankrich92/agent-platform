package com.htam.agent.workflow;

import java.util.Map;

public record SubAgentHandoffPlan(
        Long parentAgentId,
        Long subAgentId,
        String parentRunId,
        String isolatedContextRef,
        String input,
        boolean summaryRequired,
        Map<String, Object> handoff) {

    public SubAgentHandoffPlan {
        input = input == null ? "" : input;
        handoff = handoff == null ? Map.of() : Map.copyOf(handoff);
    }
}
