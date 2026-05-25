package com.htam.agent.workflow.definition;

import java.util.List;
import java.util.Map;

public record WorkflowDefinition(
        String workflowId,
        String name,
        List<WorkflowNodeDefinition> nodes,
        Map<String, Object> metadata) {

    public WorkflowDefinition {
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException("workflowId 不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
