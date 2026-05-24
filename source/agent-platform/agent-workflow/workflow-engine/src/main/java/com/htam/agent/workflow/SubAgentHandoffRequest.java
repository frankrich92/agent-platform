package com.htam.agent.workflow;

import java.util.Map;

public record SubAgentHandoffRequest(
        Long parentAgentId,
        Long subAgentId,
        String parentRunId,
        String input,
        int maxInputChars,
        Map<String, Object> metadata) {

    public SubAgentHandoffRequest {
        maxInputChars = maxInputChars <= 0 ? 4000 : maxInputChars;
        input = input == null ? "" : input;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
