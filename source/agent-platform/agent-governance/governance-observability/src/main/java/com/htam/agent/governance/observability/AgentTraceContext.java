package com.htam.agent.governance.observability;

import java.time.Instant;
import java.util.Map;

public record AgentTraceContext(
        String traceId,
        String parentTraceId,
        String runId,
        Long sessionId,
        Long agentId,
        Instant createdAt,
        Map<String, Object> attributes) {

    public AgentTraceContext {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId 不能为空");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
