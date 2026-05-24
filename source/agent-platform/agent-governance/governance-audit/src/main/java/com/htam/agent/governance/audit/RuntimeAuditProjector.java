package com.htam.agent.governance.audit;

import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.ToolCall;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class RuntimeAuditProjector {

    public AuditEvent fromRuntimeEvent(RuntimeEvent event) {
        Map<String, Object> attributes = new LinkedHashMap<>(event.payload());
        attributes.put("eventType", event.eventType().name());
        attributes.put("stepId", event.stepId());
        return new AuditEvent(
                UUID.randomUUID().toString(),
                event.traceId(),
                event.runId(),
                "agent-runtime",
                "runtime." + event.eventType().name(),
                event.eventType().name().contains("FAILED") ? AuditSeverity.ERROR : AuditSeverity.INFO,
                event.timestamp(),
                attributes);
    }

    public AuditEvent fromToolCall(ToolCall toolCall) {
        Map<String, Object> attributes = new LinkedHashMap<>(toolCall.auditTags());
        attributes.put("toolName", toolCall.toolName());
        attributes.put("policy", toolCall.policy().name());
        attributes.put("readOnly", toolCall.readOnly());
        attributes.put("parameterSummary", toolCall.parameterSummary());
        attributes.put("resultSummary", toolCall.resultSummary());
        attributes.put("errorCode", toolCall.errorCode());
        return new AuditEvent(
                UUID.randomUUID().toString(),
                String.valueOf(attributes.getOrDefault("traceId", toolCall.runId())),
                toolCall.runId(),
                "agent-runtime",
                "tool.call",
                toolCall.failed() ? AuditSeverity.ERROR : AuditSeverity.INFO,
                null,
                attributes);
    }
}
