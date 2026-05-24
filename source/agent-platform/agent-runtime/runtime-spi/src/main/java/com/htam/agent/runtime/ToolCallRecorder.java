package com.htam.agent.runtime;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ToolCallRecorder {

    public ToolCall succeeded(
            String runId,
            String toolName,
            ToolCallPolicy policy,
            boolean readOnly,
            Instant startedAt,
            Instant endedAt,
            long costMicros,
            String parameterSummary,
            String resultSummary,
            Map<String, Object> auditTags) {
        return record(runId, toolName, policy, readOnly, startedAt, endedAt, costMicros,
                parameterSummary, resultSummary, null, null, auditTags);
    }

    public ToolCall failed(
            String runId,
            String toolName,
            ToolCallPolicy policy,
            boolean readOnly,
            Instant startedAt,
            Instant endedAt,
            String parameterSummary,
            String errorCode,
            String errorMessage,
            Map<String, Object> auditTags) {
        return record(runId, toolName, policy, readOnly, startedAt, endedAt, 0,
                parameterSummary, null, errorCode, errorMessage, auditTags);
    }

    private ToolCall record(
            String runId,
            String toolName,
            ToolCallPolicy policy,
            boolean readOnly,
            Instant startedAt,
            Instant endedAt,
            long costMicros,
            String parameterSummary,
            String resultSummary,
            String errorCode,
            String errorMessage,
            Map<String, Object> auditTags) {
        return new ToolCall(UUID.randomUUID().toString(), runId, toolName, policy, readOnly,
                duration(startedAt, endedAt), Math.max(0, costMicros), parameterSummary, resultSummary,
                errorCode, errorMessage, auditTags);
    }

    private static Duration duration(Instant startedAt, Instant endedAt) {
        if (startedAt == null || endedAt == null || endedAt.isBefore(startedAt)) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, endedAt);
    }
}
