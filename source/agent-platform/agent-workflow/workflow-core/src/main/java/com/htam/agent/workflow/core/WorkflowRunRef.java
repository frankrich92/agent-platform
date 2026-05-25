package com.htam.agent.workflow.core;

public record WorkflowRunRef(String workflowId, String runId) {

    public WorkflowRunRef {
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
    }
}
