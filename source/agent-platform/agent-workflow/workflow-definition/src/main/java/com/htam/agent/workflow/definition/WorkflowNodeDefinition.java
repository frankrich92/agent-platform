package com.htam.agent.workflow.definition;

import java.util.List;
import java.util.Map;

public record WorkflowNodeDefinition(
        String nodeId,
        String nodeType,
        List<String> nextNodeIds,
        Map<String, Object> config) {

    public WorkflowNodeDefinition {
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId 不能为空");
        }
        if (nodeType == null || nodeType.isBlank()) {
            throw new IllegalArgumentException("nodeType 不能为空");
        }
        nextNodeIds = nextNodeIds == null ? List.of() : List.copyOf(nextNodeIds);
        config = config == null ? Map.of() : Map.copyOf(config);
    }
}
