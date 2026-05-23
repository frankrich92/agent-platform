package com.htam.agent.runtime;

import java.time.Instant;
import java.util.Map;

public record RuntimeEvent(
        String eventId,
        RuntimeEventType eventType,
        String runId,
        String sessionId,
        String stepId,
        String traceId,
        long sequence,
        Instant timestamp,
        Map<String, Object> payload) {

    public RuntimeEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        eventType = eventType == null ? RuntimeEventType.UNKNOWN : eventType;
        timestamp = timestamp == null ? Instant.now() : timestamp;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }

    public static RuntimeEvent of(String eventId, RuntimeEventType eventType, String runId, long sequence) {
        return new RuntimeEvent(eventId, eventType, runId, null, null, null, sequence, Instant.now(), Map.of());
    }
}
