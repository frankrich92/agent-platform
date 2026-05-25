package com.htam.agent.workflow;

import java.util.Map;

public record SubAgentHandoffRequest(
        Long parentAgentId,
        Long subAgentId,
        String parentRunId,
        String input,
        int maxInputChars,
        int currentDepth,
        int maxDepth,
        Map<String, Object> metadata) {

    public SubAgentHandoffRequest(
            Long parentAgentId,
            Long subAgentId,
            String parentRunId,
            String input,
            int maxInputChars,
            Map<String, Object> metadata) {
        this(parentAgentId, subAgentId, parentRunId, input, maxInputChars, 0, 3, metadata);
    }

    public SubAgentHandoffRequest {
        maxInputChars = maxInputChars <= 0 ? 4000 : maxInputChars;
        currentDepth = Math.max(0, currentDepth);
        maxDepth = maxDepth <= 0 ? 3 : maxDepth;
        input = input == null ? "" : input;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean recursionAllowed() {
        return currentDepth < maxDepth;
    }
}
