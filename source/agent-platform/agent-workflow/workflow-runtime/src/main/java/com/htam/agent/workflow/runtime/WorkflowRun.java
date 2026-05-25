package com.htam.agent.workflow.runtime;

import java.util.Map;

public record WorkflowRun(
        String workflowId,
        String runId,
        WorkflowRunStatus status,
        Map<String, Object> input) {

    public WorkflowRun {
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        status = status == null ? WorkflowRunStatus.RUNNING : status;
        input = input == null ? Map.of() : Map.copyOf(input);
    }
}
