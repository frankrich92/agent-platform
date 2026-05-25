package com.htam.agent.workflow.api;

import java.util.Map;

public record WorkflowRunDTO(
        String workflowId,
        String runId,
        String status,
        Map<String, Object> input) {

    public WorkflowRunDTO {
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        input = input == null ? Map.of() : Map.copyOf(input);
    }
}
