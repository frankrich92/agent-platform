package com.htam.agent.run.toolcall;

import com.htam.agent.runtime.ToolCall;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryToolCallLedger implements ToolCallLedger {

    private final ConcurrentMap<String, List<ToolCall>> toolCallsByRunId = new ConcurrentHashMap<>();

    @Override
    public void append(ToolCall toolCall) {
        if (toolCall == null) {
            return;
        }
        toolCallsByRunId.compute(toolCall.runId(), (runId, existing) -> {
            List<ToolCall> toolCalls = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            toolCalls.add(toolCall);
            return List.copyOf(toolCalls);
        });
    }

    @Override
    public List<ToolCall> listByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            return List.of();
        }
        return toolCallsByRunId.getOrDefault(runId, List.of());
    }
}
