package com.htam.agent.runtime;

import java.util.Map;

public record SubAgentRunContext(
        Long parentAgentId,
        Long subAgentId,
        String parentRunId,
        String subRunId,
        String isolatedContextRef,
        String summary,
        Map<String, Object> handoff) {

    public SubAgentRunContext {
        handoff = handoff == null ? Map.of() : Map.copyOf(handoff);
    }
}
