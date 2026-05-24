package com.htam.agent.governance.audit;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record AuditEvent(
        String auditId,
        String traceId,
        String runId,
        String actor,
        String action,
        AuditSeverity severity,
        Instant occurredAt,
        Map<String, Object> attributes) {

    public AuditEvent {
        if (auditId == null || auditId.isBlank()) {
            throw new IllegalArgumentException("auditId 不能为空");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action 不能为空");
        }
        severity = severity == null ? AuditSeverity.INFO : severity;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        attributes = sanitize(attributes);
    }

    private static Map<String, Object> sanitize(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                sanitized.put(key, value);
            }
        });
        return Map.copyOf(sanitized);
    }
}
