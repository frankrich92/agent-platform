package com.htam.agent.runtime;

import java.time.Duration;
import java.util.Map;

public record ToolCall(
        String toolCallId,
        String runId,
        String toolName,
        ToolCallPolicy policy,
        boolean readOnly,
        Duration duration,
        long costMicros,
        String parameterSummary,
        String resultSummary,
        String errorCode,
        String errorMessage,
        Map<String, Object> auditTags) {

    public ToolCall {
        if (toolCallId == null || toolCallId.isBlank()) {
            throw new IllegalArgumentException("toolCallId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        policy = policy == null ? ToolCallPolicy.ASK : policy;
        duration = duration == null ? Duration.ZERO : duration;
        auditTags = auditTags == null ? Map.of() : Map.copyOf(auditTags);
    }

    public boolean failed() {
        return (errorCode != null && !errorCode.isBlank())
                || (errorMessage != null && !errorMessage.isBlank());
    }

    public boolean succeeded() {
        return !failed();
    }
}
