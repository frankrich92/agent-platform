package com.htam.agent.workflow.node;

import java.util.Map;

public record WorkflowNodeResult(
        String nodeId,
        WorkflowNodeStatus status,
        Map<String, Object> output,
        String errorMessage) {

    public WorkflowNodeResult {
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId 不能为空");
        }
        status = status == null ? WorkflowNodeStatus.SUCCEEDED : status;
        output = output == null ? Map.of() : Map.copyOf(output);
    }

    public boolean terminal() {
        return status == WorkflowNodeStatus.SUCCEEDED || status == WorkflowNodeStatus.FAILED;
    }
}
