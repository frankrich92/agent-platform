package com.htam.agent.runtime.planning;

import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.ToolCall;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DoomLoopDetector {

    public DoomLoopDecision detect(
            List<RunStep> steps,
            List<ToolCall> toolCalls,
            int maxSteps,
            int repeatedToolThreshold,
            int repeatedErrorThreshold) {
        List<RunStep> stepList = steps == null ? List.of() : List.copyOf(steps);
        List<ToolCall> toolCallList = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        List<String> reasons = new ArrayList<>();
        if (maxSteps > 0 && stepList.size() > maxSteps) {
            reasons.add("step count exceeded maxSteps");
        }
        Map<String, Long> toolCounts = toolCallList.stream()
                .collect(Collectors.groupingBy(ToolCall::toolName, Collectors.counting()));
        if (toolCounts.values().stream().anyMatch(count -> count >= Math.max(2, repeatedToolThreshold))) {
            reasons.add("same tool repeated too many times");
        }
        Map<String, Long> errorCounts = toolCallList.stream()
                .filter(ToolCall::failed)
                .collect(Collectors.groupingBy(call -> call.errorCode() == null ? "UNKNOWN" : call.errorCode(),
                        Collectors.counting()));
        if (errorCounts.values().stream().anyMatch(count -> count >= Math.max(2, repeatedErrorThreshold))) {
            reasons.add("same error repeated too many times");
        }
        return new DoomLoopDecision(!reasons.isEmpty(), !reasons.isEmpty(), reasons);
    }
}
