package com.htam.agent.runtime.core;

import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.ToolCall;
import java.util.List;

public record RuntimeInteractionTrace(
        List<RuntimeEvent> events,
        List<RunStep> steps,
        List<ToolCall> toolCalls) {

    public RuntimeInteractionTrace {
        events = events == null ? List.of() : List.copyOf(events);
        steps = steps == null ? List.of() : List.copyOf(steps);
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }
}
