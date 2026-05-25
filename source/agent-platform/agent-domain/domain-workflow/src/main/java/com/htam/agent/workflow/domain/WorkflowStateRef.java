package com.htam.agent.workflow.domain;

public record WorkflowStateRef(String workflowId, String runId, String nodeId) {

    public WorkflowStateRef {
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
    }
}
