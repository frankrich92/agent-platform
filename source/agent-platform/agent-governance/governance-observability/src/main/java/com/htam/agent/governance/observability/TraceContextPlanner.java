package com.htam.agent.governance.observability;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TraceContextPlanner {

    public AgentTraceContext plan(
            String runId,
            Long sessionId,
            Long agentId,
            String incomingTraceId,
            String parentTraceId,
            Map<String, Object> attributes) {
        String traceId = incomingTraceId == null || incomingTraceId.isBlank()
                ? defaultTraceId(runId)
                : incomingTraceId;
        Map<String, Object> traceAttributes = new LinkedHashMap<>();
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                if (key != null && value != null) {
                    traceAttributes.put(key, value);
                }
            });
        }
        if (runId != null) {
            traceAttributes.put("runId", runId);
        }
        if (sessionId != null) {
            traceAttributes.put("sessionId", sessionId);
        }
        if (agentId != null) {
            traceAttributes.put("agentId", agentId);
        }
        return new AgentTraceContext(
                traceId,
                parentTraceId,
                runId,
                sessionId,
                agentId,
                Instant.now(),
                traceAttributes);
    }

    private static String defaultTraceId(String runId) {
        return runId == null || runId.isBlank()
                ? "trace-" + UUID.randomUUID()
                : runId;
    }
}
