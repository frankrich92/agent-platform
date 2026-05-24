package com.htam.agent.run;

import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.run.event.RuntimeEventSink;
import com.htam.agent.run.step.RunStepSink;
import com.htam.agent.run.toolcall.ToolCallSink;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AgentRunLedgerRecorder {

    private final List<RuntimeEventSink> eventSinks;
    private final List<RunStepSink> stepSinks;
    private final List<ToolCallSink> toolCallSinks;

    public AgentRunLedgerRecorder(
            List<RuntimeEventSink> eventSinks,
            List<RunStepSink> stepSinks,
            List<ToolCallSink> toolCallSinks) {
        this.eventSinks = eventSinks == null ? List.of() : List.copyOf(eventSinks);
        this.stepSinks = stepSinks == null ? List.of() : List.copyOf(stepSinks);
        this.toolCallSinks = toolCallSinks == null ? List.of() : List.copyOf(toolCallSinks);
    }

    public void runStarted(
            String runId,
            AgentRunCommand command,
            CapabilityPlan capabilityPlan,
            Integer userMessageId) {
        publish(new RuntimeEvent(
                UUID.randomUUID().toString(),
                RuntimeEventType.RUN_STARTED,
                runId,
                command.sessionId() == null ? null : String.valueOf(command.sessionId()),
                null,
                runId,
                1,
                Instant.now(),
                payload(
                        "agentId", command.agentId(),
                        "userMessageId", userMessageId,
                        "capabilityPlanId", capabilityPlan.planId(),
                        "capabilityPlanItemCount", capabilityPlan.items().size(),
                        "enabledCapabilityCount", capabilityPlan.enabledItems().size())));
    }

    public void runFinished(
            String runId,
            Long sessionId,
            AgentRunResult result,
            Integer userMessageId,
            Integer assistantMessageId) {
        AgentRunResult runResult = result == null
                ? AgentRunResult.failure(null, runId, "runtime result is null", List.of())
                : result;
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
                runId,
                nextSequence(runResult.events()),
                Instant.now(),
                payload(
                        "agentId", runResult.agentId(),
                        "status", runResult.status().name(),
                        "userMessageId", userMessageId,
                        "assistantMessageId", assistantMessageId,
                        "stepCount", runResult.steps().size(),
                        "toolCallCount", runResult.toolCalls().size())));
    }

    public void runFailed(String runId, Long sessionId, Long agentId, Throwable throwable) {
        publish(new RuntimeEvent(
                UUID.randomUUID().toString(),
                RuntimeEventType.RUN_FAILED,
                runId,
                sessionId == null ? null : String.valueOf(sessionId),
                null,
                runId,
                2,
                Instant.now(),
                payload(
                        "agentId", agentId,
                        "errorType", throwable == null ? null : throwable.getClass().getName(),
                        "errorMessage", throwable == null ? null : throwable.getMessage())));
    }

    private void publish(RuntimeEvent event) {
        eventSinks.forEach(sink -> sink.append(event));
    }

    private void append(com.htam.agent.runtime.RunStep step) {
        stepSinks.forEach(sink -> sink.append(step));
    }

    private void append(com.htam.agent.runtime.ToolCall toolCall) {
        toolCallSinks.forEach(sink -> sink.append(toolCall));
    }

    private static long nextSequence(List<RuntimeEvent> events) {
        return events.stream()
                .mapToLong(RuntimeEvent::sequence)
                .max()
                .orElse(1L) + 1L;
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
}
