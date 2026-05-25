package com.htam.agent.run.replay;

import com.htam.agent.run.history.RunHistoryReader;
import com.htam.agent.run.history.RunHistoryRecord;
import com.htam.agent.run.message.RunMessageRecord;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.ToolCall;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RunReplayService {

    private final RunHistoryReader historyReader;

    public RunReplayService(RunHistoryReader historyReader) {
        this.historyReader = historyReader;
    }

    public RunReplayPlan createReplay(String runId) {
        RunHistoryRecord history = historyReader.read(runId);
        List<RunReplayFrame> frames = new ArrayList<>();
        if (!history.events().isEmpty()) {
            frames.addAll(eventFrames(history));
            return new RunReplayPlan("replay-" + UUID.randomUUID(), runId, frames.stream()
                    .sorted(Comparator.comparingLong(RunReplayFrame::sequence))
                    .toList());
        }
        history.events().forEach(event -> frames.add(new RunReplayFrame(
                event.sequence(),
                "event:" + event.eventType().name(),
                event.eventId(),
                event.timestamp(),
                event.payload())));
        history.steps().forEach(step -> frames.add(new RunReplayFrame(
                Long.MAX_VALUE - 2,
                "step:" + step.stepType().name(),
                step.stepId(),
                step.startedAt(),
                new LinkedHashMap<>(step.summary()))));
        history.toolCalls().forEach(toolCall -> frames.add(new RunReplayFrame(
                Long.MAX_VALUE - 1,
                "toolCall",
                toolCall.toolCallId(),
                null,
                toolPayload(toolCall.toolName(), toolCall.parameterSummary(), toolCall.resultSummary(),
                        toolCall.errorCode(), toolCall.errorMessage()))));
        history.messages().forEach(message -> frames.add(new RunReplayFrame(
                Long.MAX_VALUE,
                "message:" + message.role().name(),
                message.messageId(),
                message.createdAt(),
                Map.of("content", message.content()))));
        return new RunReplayPlan("replay-" + UUID.randomUUID(), runId, frames.stream()
                .sorted(Comparator.comparingLong(RunReplayFrame::sequence))
                .toList());
    }

    private List<RunReplayFrame> eventFrames(RunHistoryRecord history) {
        Map<String, RunStep> stepsById = history.steps().stream()
                .collect(Collectors.toMap(RunStep::stepId, step -> step, (left, right) -> right));
        Map<String, ToolCall> toolsById = history.toolCalls().stream()
                .collect(Collectors.toMap(ToolCall::toolCallId, call -> call, (left, right) -> right));
        List<Map<String, Object>> allSteps = history.steps().stream().map(this::stepPayload).toList();
        List<Map<String, Object>> allToolCalls = history.toolCalls().stream().map(this::toolCallPayload).toList();
        List<Map<String, Object>> allMessages = history.messages().stream().map(this::messagePayload).toList();

        return history.events().stream()
                .map(event -> {
                    Map<String, Object> payload = new LinkedHashMap<>(event.payload());
                    payload.put("eventType", event.eventType().name());
                    if (event.stepId() != null && stepsById.containsKey(event.stepId())) {
                        payload.put("step", stepPayload(stepsById.get(event.stepId())));
                    }
                    if (event.stepId() != null && toolsById.containsKey(event.stepId())) {
                        payload.put("toolCall", toolCallPayload(toolsById.get(event.stepId())));
                    }
                    if (isTerminalEvent(event)) {
                        payload.put("steps", allSteps);
                        payload.put("toolCalls", allToolCalls);
                        payload.put("messages", allMessages);
                    }
                    return new RunReplayFrame(
                            event.sequence(),
                            "event:" + event.eventType().name(),
                            event.eventId(),
                            event.timestamp(),
                            payload);
                })
                .toList();
    }

    private boolean isTerminalEvent(RuntimeEvent event) {
        return switch (event.eventType()) {
            case RUN_COMPLETED, RUN_FAILED -> true;
            default -> false;
        };
    }

    private Map<String, Object> stepPayload(RunStep step) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "stepId", step.stepId());
        put(payload, "runId", step.runId());
        put(payload, "stepType", step.stepType().name());
        put(payload, "status", step.status().name());
        put(payload, "startedAt", step.startedAt());
        put(payload, "endedAt", step.endedAt());
        put(payload, "summary", step.summary());
        return payload;
    }

    private Map<String, Object> toolCallPayload(ToolCall toolCall) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "toolCallId", toolCall.toolCallId());
        put(payload, "runId", toolCall.runId());
        put(payload, "toolName", toolCall.toolName());
        put(payload, "policy", toolCall.policy().name());
        put(payload, "readOnly", toolCall.readOnly());
        put(payload, "durationMillis", toolCall.duration().toMillis());
        put(payload, "costMicros", toolCall.costMicros());
        put(payload, "parameterSummary", toolCall.parameterSummary());
        put(payload, "resultSummary", toolCall.resultSummary());
        put(payload, "errorCode", toolCall.errorCode());
        put(payload, "errorMessage", toolCall.errorMessage());
        put(payload, "auditTags", toolCall.auditTags());
        return payload;
    }

    private Map<String, Object> messagePayload(RunMessageRecord message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, "messageId", message.messageId());
        put(payload, "sessionId", message.sessionId());
        put(payload, "runId", message.runId());
        put(payload, "role", message.role().name());
        put(payload, "content", message.content());
        put(payload, "createdAt", message.createdAt());
        return payload;
    }

    private static void put(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private static Map<String, Object> toolPayload(
            String toolName,
            String parameterSummary,
            String resultSummary,
            String errorCode,
            String errorMessage) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("toolName", toolName);
        if (parameterSummary != null) {
            payload.put("parameterSummary", parameterSummary);
        }
        if (resultSummary != null) {
            payload.put("resultSummary", resultSummary);
        }
        if (errorCode != null) {
            payload.put("errorCode", errorCode);
        }
        if (errorMessage != null) {
            payload.put("errorMessage", errorMessage);
        }
        return payload;
    }
}
