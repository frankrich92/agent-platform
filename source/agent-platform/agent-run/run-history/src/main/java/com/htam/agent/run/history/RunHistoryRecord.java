package com.htam.agent.run.history;

import com.htam.agent.run.message.RunMessageRecord;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.ToolCall;
import java.util.List;

public record RunHistoryRecord(
        String runId,
        List<RuntimeEvent> events,
        List<RunStep> steps,
        List<ToolCall> toolCalls,
        List<RunMessageRecord> messages) {

    public RunHistoryRecord {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        events = events == null ? List.of() : List.copyOf(events);
        steps = steps == null ? List.of() : List.copyOf(steps);
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        messages = messages == null ? List.of() : List.copyOf(messages);
    }
}
