package com.htam.agent.capability;

import java.time.Instant;
import java.util.Map;

public record McpCallAuditEvent(
        String auditId,
        String namespace,
        String serverId,
        String toolName,
        String traceId,
        String parameterSummary,
        String resultSummary,
        String errorCode,
        Instant occurredAt,
        Map<String, Object> tags) {

    public McpCallAuditEvent {
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        tags = tags == null ? Map.of() : Map.copyOf(tags);
    }
}
