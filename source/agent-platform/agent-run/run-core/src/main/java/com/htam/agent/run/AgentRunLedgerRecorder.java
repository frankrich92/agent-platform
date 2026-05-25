package com.htam.agent.run;

import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.run.event.RuntimeEventSink;
import com.htam.agent.run.step.RunStepSink;
import com.htam.agent.run.toolcall.ToolCallSink;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgentRunLedgerRecorder {

    private final List<RuntimeEventSink> eventSinks;
    private final List<RunStepSink> stepSinks;
    private final List<ToolCallSink> toolCallSinks;
    private final List<AgentRunLedger> runLedgers;
    private final AgentRunLedgerWriteQueue writeQueue;

    public AgentRunLedgerRecorder(
            List<RuntimeEventSink> eventSinks,
            List<RunStepSink> stepSinks,
            List<ToolCallSink> toolCallSinks) {
        this(eventSinks, stepSinks, toolCallSinks, List.of(), AgentRunLedgerWriteQueue.direct());
    }

    public AgentRunLedgerRecorder(
            List<RuntimeEventSink> eventSinks,
            List<RunStepSink> stepSinks,
            List<ToolCallSink> toolCallSinks,
            List<AgentRunLedger> runLedgers) {
        this(eventSinks, stepSinks, toolCallSinks, runLedgers, AgentRunLedgerWriteQueue.direct());
    }

    @Autowired
    public AgentRunLedgerRecorder(
            List<RuntimeEventSink> eventSinks,
            List<RunStepSink> stepSinks,
            List<ToolCallSink> toolCallSinks,
            List<AgentRunLedger> runLedgers,
            AgentRunLedgerWriteQueue writeQueue) {
        this.eventSinks = eventSinks == null ? List.of() : List.copyOf(eventSinks);
        this.stepSinks = stepSinks == null ? List.of() : List.copyOf(stepSinks);
        this.toolCallSinks = toolCallSinks == null ? List.of() : List.copyOf(toolCallSinks);
        this.runLedgers = runLedgers == null ? List.of() : List.copyOf(runLedgers);
        this.writeQueue = writeQueue == null ? AgentRunLedgerWriteQueue.direct() : writeQueue;
    }

    public void runStarted(
            String runId,
            AgentRunCommand command,
            CapabilityPlan capabilityPlan,
            Integer userMessageId) {
        runStarted(runId, command, capabilityPlan, userMessageId, runId);
    }

    public void runStarted(
            String runId,
            AgentRunCommand command,
            CapabilityPlan capabilityPlan,
            Integer userMessageId,
            String traceId) {
        runStarted(runId, command, capabilityPlan, userMessageId, traceId, Map.of());
    }

    public void runStarted(
            String runId,
            AgentRunCommand command,
            CapabilityPlan capabilityPlan,
            Integer userMessageId,
            String traceId,
            Map<String, Object> runMetadata) {
        Map<String, Object> metadata = payload(
                "userMessageId", userMessageId,
                "traceId", traceId,
                "title", title(command.input()),
                "titleSource", "input",
                "inputPreview", preview(command.input(), 120),
                "capabilityPlanId", capabilityPlan.planId(),
                "capabilityPlanItemCount", capabilityPlan.items().size(),
                "enabledCapabilityCount", capabilityPlan.enabledItems().size());
        metadata.putAll(runMetadata == null ? Map.of() : runMetadata);
        saveRun(new AgentRunRecord(
                runId,
                command.agentId(),
                command.sessionId() == null ? null : String.valueOf(command.sessionId()),
                AgentRunStatus.RUNNING,
                command.input(),
                null,
                traceId,
                Instant.now(),
                null,
                metadata));
        Map<String, Object> eventPayload = payload(
                "agentId", command.agentId(),
                "userMessageId", userMessageId,
                "traceId", traceId,
                "title", metadata.get("title"),
                "capabilityPlanId", capabilityPlan.planId(),
                "capabilityPlanItemCount", capabilityPlan.items().size(),
                "enabledCapabilityCount", capabilityPlan.enabledItems().size());
        eventPayload.putAll(runMetadata == null ? Map.of() : runMetadata);
        publish(new RuntimeEvent(
                UUID.randomUUID().toString(),
                RuntimeEventType.RUN_STARTED,
                runId,
                command.sessionId() == null ? null : String.valueOf(command.sessionId()),
                null,
                traceId,
                1,
                Instant.now(),
                eventPayload));
    }

    public void runFinished(
            String runId,
            Long sessionId,
            AgentRunResult result,
            Integer userMessageId,
            Integer assistantMessageId) {
        runFinished(runId, sessionId, result, userMessageId, assistantMessageId, runId);
    }

    public void runFinished(
            String runId,
            Long sessionId,
            AgentRunResult result,
            Integer userMessageId,
            Integer assistantMessageId,
            String traceId) {
        AgentRunResult runResult = result == null
                ? AgentRunResult.failure(null, runId, "runtime result is null", List.of())
                : result;
        flush();
        findRun(runId).ifPresentOrElse(
                existing -> saveRun(existing.withStatus(runResult.status(), runResult.message(), Instant.now())),
                () -> saveRun(new AgentRunRecord(
                        runId,
                        runResult.agentId(),
                        sessionId == null ? null : String.valueOf(sessionId),
                        runResult.status(),
                        null,
                        runResult.message(),
                        traceId,
                        Instant.now(),
                        Instant.now(),
                        payload(
                                "title", title(runResult.message()),
                                "titleSource", "result",
                                "userMessageId", userMessageId,
                                "assistantMessageId", assistantMessageId,
                                "traceId", traceId))));
        runResult.events().forEach(this::publish);
        runResult.steps().forEach(this::append);
        runResult.toolCalls().forEach(this::append);
        RuntimeEventType eventType = runResult.success()
                ? RuntimeEventType.RUN_COMPLETED
                : RuntimeEventType.RUN_FAILED;
        publish(new RuntimeEvent(
                UUID.randomUUID().toString(),
                eventType,
                runId,
                sessionId == null ? null : String.valueOf(sessionId),
                null,
                traceId,
                nextSequence(runResult.events()),
                Instant.now(),
                payload(
                        "agentId", runResult.agentId(),
                        "status", runResult.status().name(),
                        "traceId", traceId,
                        "userMessageId", userMessageId,
                        "assistantMessageId", assistantMessageId,
                        "stepCount", runResult.steps().size(),
                        "toolCallCount", runResult.toolCalls().size())));
        flush();
    }

    public void runFailed(String runId, Long sessionId, Long agentId, Throwable throwable) {
        runFailed(runId, sessionId, agentId, throwable, runId);
    }

    public void runFailed(String runId, Long sessionId, Long agentId, Throwable throwable, String traceId) {
        flush();
        findRun(runId).ifPresentOrElse(
                existing -> saveRun(existing.withStatus(
                        AgentRunStatus.FAILED,
                        throwable == null ? null : throwable.getMessage(),
                        Instant.now())),
                () -> saveRun(new AgentRunRecord(
                        runId,
                        agentId,
                        sessionId == null ? null : String.valueOf(sessionId),
                        AgentRunStatus.FAILED,
                        null,
                        throwable == null ? null : throwable.getMessage(),
                        traceId,
                        Instant.now(),
                        Instant.now(),
                        payload(
                                "traceId", traceId,
                                "errorType", throwable == null ? null : throwable.getClass().getName()))));
        publish(new RuntimeEvent(
                UUID.randomUUID().toString(),
                RuntimeEventType.RUN_FAILED,
                runId,
                sessionId == null ? null : String.valueOf(sessionId),
                null,
                traceId,
                2,
                Instant.now(),
                payload(
                        "agentId", agentId,
                        "traceId", traceId,
                        "errorType", throwable == null ? null : throwable.getClass().getName(),
                        "errorMessage", throwable == null ? null : throwable.getMessage())));
        flush();
    }

    private void publish(RuntimeEvent event) {
        eventSinks.forEach(sink -> writeQueue.submit(
                "runtime-event:" + (event == null ? "null" : event.runId() + ":" + event.sequence()),
                () -> sink.append(event)));
    }

    private void append(com.htam.agent.runtime.RunStep step) {
        stepSinks.forEach(sink -> writeQueue.submit(
                "run-step:" + (step == null ? "null" : step.runId() + ":" + step.stepId()),
                () -> sink.append(step)));
    }

    private void append(com.htam.agent.runtime.ToolCall toolCall) {
        toolCallSinks.forEach(sink -> writeQueue.submit(
                "tool-call:" + (toolCall == null ? "null" : toolCall.runId() + ":" + toolCall.toolCallId()),
                () -> sink.append(toolCall)));
    }

    private void saveRun(AgentRunRecord record) {
        runLedgers.forEach(ledger -> writeQueue.submit(
                "agent-run:" + (record == null ? "null" : record.runId()),
                () -> ledger.save(record)));
    }

    private java.util.Optional<AgentRunRecord> findRun(String runId) {
        return runLedgers.stream()
                .map(ledger -> ledger.findById(runId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .findFirst();
    }

    private static long nextSequence(List<RuntimeEvent> events) {
        return events.stream()
                .mapToLong(RuntimeEvent::sequence)
                .max()
                .orElse(1L) + 1L;
    }

    void flush() {
        writeQueue.flush();
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

    private static String title(String content) {
        String preview = preview(content, 40);
        return preview == null || preview.isBlank() ? "Untitled run" : preview;
    }

    private static String preview(String content, int maxChars) {
        if (content == null) {
            return null;
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        int limit = maxChars <= 0 ? 40 : maxChars;
        return normalized.length() <= limit ? normalized : normalized.substring(0, limit);
    }
}
