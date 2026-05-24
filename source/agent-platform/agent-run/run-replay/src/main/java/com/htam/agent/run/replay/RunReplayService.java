package com.htam.agent.run.replay;

import com.htam.agent.run.history.RunHistoryReader;
import com.htam.agent.run.history.RunHistoryRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RunReplayService {

    private final RunHistoryReader historyReader;

    public RunReplayService(RunHistoryReader historyReader) {
        this.historyReader = historyReader;
    }

    public RunReplayPlan createReplay(String runId) {
        RunHistoryRecord history = historyReader.read(runId);
        List<RunReplayFrame> frames = new ArrayList<>();
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
