package com.htam.agent.capability.tool;

import java.util.List;

public record ToolParallelExecutionPlan(
        List<ToolExecutionDecision> parallelGroup,
        List<ToolExecutionDecision> serialGroup,
        List<ToolExecutionDecision> deniedGroup) {

    public ToolParallelExecutionPlan {
        parallelGroup = parallelGroup == null ? List.of() : List.copyOf(parallelGroup);
        serialGroup = serialGroup == null ? List.of() : List.copyOf(serialGroup);
        deniedGroup = deniedGroup == null ? List.of() : List.copyOf(deniedGroup);
    }

    public boolean hasParallelWork() {
        return parallelGroup.size() > 1;
    }
}
