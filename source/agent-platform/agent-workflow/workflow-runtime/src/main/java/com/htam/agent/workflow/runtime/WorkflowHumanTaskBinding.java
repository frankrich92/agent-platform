package com.htam.agent.workflow.runtime;

public record WorkflowHumanTaskBinding(String taskId, String runId, String nodeId) {

    public WorkflowHumanTaskBinding {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId 不能为空");
        }
    }
}
