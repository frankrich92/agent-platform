package com.htam.agent.runtime.core;

import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public final class RuntimeInteractionRecorder {

    private static final int TOOL_SUMMARY_MAX_CHARS = 16_384;

    private static final InheritableThreadLocal<TraceState> STATE = new InheritableThreadLocal<>();

    private RuntimeInteractionRecorder() {
    }

    public static <T> T withinTrace(
            String runId,
            String sessionId,
            String traceId,
            long firstSequence,
            Supplier<T> supplier) {
        TraceState previous = STATE.get();
        STATE.set(new TraceState(runId, sessionId, traceId, Math.max(1, firstSequence)));
        try {
            return supplier.get();
        } finally {
            TraceState state = STATE.get();
            if (previous == null) {
                STATE.remove();
            } else {
                STATE.set(previous);
            }
            if (state != null && previous != null) {
                previous.absorb(state);
            }
        }
    }

    public static RuntimeInteractionTrace currentTrace() {
        TraceState state = STATE.get();
        return state == null ? new RuntimeInteractionTrace(List.of(), List.of(), List.of()) : state.snapshot();
    }

    public static void recordSkillLoad(String skillName, String contentRef) {
        TraceState state = STATE.get();
        if (state == null) {
            return;
        }
        String stepId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Map<String, Object> payload = payload("skillName", skillName, "contentRef", contentRef);
        state.events.add(event(state, RuntimeEventType.STEP_COMPLETED, stepId, payload));
        state.steps.add(new RunStep(stepId, state.runId, RunStepType.SKILL_LOAD,
                AgentRunStatus.SUCCEEDED, now, now, payload));
    }

    public static void recordToolCall(
            String toolName,
            ToolCallPolicy policy,
            boolean readOnly,
            Instant startedAt,
            Instant endedAt,
            String parameterSummary,
            String resultSummary,
            String errorCode,
            String errorMessage,
            Map<String, Object> auditTags) {
        TraceState state = STATE.get();
        if (state == null) {
            return;
        }
        boolean failed = (errorCode != null && !errorCode.isBlank())
                || (errorMessage != null && !errorMessage.isBlank());
        String stepId = UUID.randomUUID().toString();
        Instant start = startedAt == null ? Instant.now() : startedAt;
        Instant end = endedAt == null ? Instant.now() : endedAt;
        String parameter = truncated(parameterSummary);
        String result = truncated(resultSummary);
        Map<String, Object> payload = payload(
                "toolName", toolName,
                "parameterSummary", parameter,
                "resultSummary", result,
                "errorCode", errorCode,
                "errorMessage", errorMessage);
        Map<String, Object> tags = mergedAuditTags(auditTags, parameterSummary, resultSummary);
        state.events.add(event(state,
                failed ? RuntimeEventType.TOOL_CALL_FAILED : RuntimeEventType.TOOL_CALL_COMPLETED,
                stepId,
                payload));
        state.steps.add(new RunStep(stepId, state.runId, RunStepType.TOOL_CALL,
                failed ? AgentRunStatus.FAILED : AgentRunStatus.SUCCEEDED, start, end, payload));
        state.toolCalls.add(new ToolCall(UUID.randomUUID().toString(), state.runId, toolName,
                policy, readOnly, duration(start, end), 0, parameter, result,
                errorCode, errorMessage, tags));
    }

    public static void recordMcpCall(
            String serverName,
            String toolName,
            Instant startedAt,
            Instant endedAt,
            String parameterSummary,
            String resultSummary,
            String errorMessage) {
        TraceState state = STATE.get();
        if (state == null) {
            return;
        }
        boolean failed = errorMessage != null && !errorMessage.isBlank();
        String stepId = UUID.randomUUID().toString();
        Instant start = startedAt == null ? Instant.now() : startedAt;
        Instant end = endedAt == null ? Instant.now() : endedAt;
        String parameter = truncated(parameterSummary);
        String result = truncated(resultSummary);
        Map<String, Object> payload = payload(
                "mcpServer", serverName,
                "toolName", toolName,
                "parameterSummary", parameter,
                "resultSummary", result,
                "errorMessage", errorMessage);
        state.events.add(event(state,
                failed ? RuntimeEventType.TOOL_CALL_FAILED : RuntimeEventType.TOOL_CALL_COMPLETED,
                stepId,
                payload));
        state.steps.add(new RunStep(stepId, state.runId, RunStepType.MCP_CALL,
                failed ? AgentRunStatus.FAILED : AgentRunStatus.SUCCEEDED, start, end, payload));
        state.toolCalls.add(new ToolCall(UUID.randomUUID().toString(), state.runId, toolName,
                ToolCallPolicy.ASK, false, duration(start, end), 0, parameter, result,
                failed ? "MCP_CALL_FAILED" : null, errorMessage,
                mergedAuditTags(payload("capability", "mcp", "mcpServer", serverName), parameterSummary, resultSummary)));
    }

    private static RuntimeEvent event(
            TraceState state,
            RuntimeEventType eventType,
            String stepId,
            Map<String, Object> payload) {
        return new RuntimeEvent(UUID.randomUUID().toString(), eventType, state.runId, state.sessionId,
                stepId, state.traceId, state.nextSequence.getAndIncrement(), Instant.now(), payload);
    }

    private static Duration duration(Instant startedAt, Instant endedAt) {
        if (startedAt == null || endedAt == null || endedAt.isBefore(startedAt)) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, endedAt);
    }

    private static Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            Object value = entries[i + 1];
            if (value != null) {
                payload.put(String.valueOf(entries[i]), value);
            }
        }
        return payload;
    }

    private static Map<String, Object> mergedAuditTags(
            Map<String, Object> auditTags,
            String parameterSummary,
            String resultSummary) {
        Map<String, Object> tags = new LinkedHashMap<>();
        if (auditTags != null) {
            tags.putAll(auditTags);
        }
        tags.put("parameterSize", parameterSummary == null ? 0 : parameterSummary.length());
        tags.put("parameterTruncated", parameterSummary != null && parameterSummary.length() > TOOL_SUMMARY_MAX_CHARS);
        tags.put("resultSize", resultSummary == null ? 0 : resultSummary.length());
        tags.put("resultTruncated", resultSummary != null && resultSummary.length() > TOOL_SUMMARY_MAX_CHARS);
        return tags;
    }

    private static String truncated(String value) {
        if (value == null || value.length() <= TOOL_SUMMARY_MAX_CHARS) {
            return value;
        }
        int omitted = value.length() - TOOL_SUMMARY_MAX_CHARS;
        return value.substring(0, TOOL_SUMMARY_MAX_CHARS)
                + "\n...[truncated "
                + omitted
                + " chars]";
    }

    private static final class TraceState {
        private final String runId;
        private final String sessionId;
        private final String traceId;
        private final AtomicLong nextSequence;
        private final List<RuntimeEvent> events = new ArrayList<>();
        private final List<RunStep> steps = new ArrayList<>();
        private final List<ToolCall> toolCalls = new ArrayList<>();

        private TraceState(String runId, String sessionId, String traceId, long firstSequence) {
            this.runId = runId;
            this.sessionId = sessionId;
            this.traceId = traceId;
            this.nextSequence = new AtomicLong(firstSequence);
        }

        private RuntimeInteractionTrace snapshot() {
            return new RuntimeInteractionTrace(events, steps, toolCalls);
        }

        private void absorb(TraceState child) {
            events.addAll(child.events);
            steps.addAll(child.steps);
            toolCalls.addAll(child.toolCalls);
        }
    }
}
