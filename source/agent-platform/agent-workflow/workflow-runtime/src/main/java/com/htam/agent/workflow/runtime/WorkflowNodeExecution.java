package com.htam.agent.workflow.runtime;

import com.htam.agent.workflow.node.WorkflowNodeStatus;
import java.util.Map;

public record WorkflowNodeExecution(
        String nodeId,
        WorkflowNodeStatus status,
        Map<String, Object> output,
        String errorMessage) {

    public WorkflowNodeExecution {
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId 不能为空");
        }
        status = status == null ? WorkflowNodeStatus.PENDING : status;
        output = output == null ? Map.of() : Map.copyOf(output);
    }

    public boolean terminal() {
        return status == WorkflowNodeStatus.SUCCEEDED || status == WorkflowNodeStatus.FAILED;
    }
}
