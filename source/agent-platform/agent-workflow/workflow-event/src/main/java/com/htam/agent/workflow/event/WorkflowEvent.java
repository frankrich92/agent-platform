package com.htam.agent.workflow.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WorkflowEvent(
        String eventId,
        String workflowId,
        String runId,
        String nodeId,
        WorkflowEventType eventType,
        Instant occurredAt,
        Map<String, Object> payload) {

    public WorkflowEvent {
        eventId = eventId == null || eventId.isBlank() ? UUID.randomUUID().toString() : eventId;
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        eventType = eventType == null ? WorkflowEventType.RUN_STARTED : eventType;
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
