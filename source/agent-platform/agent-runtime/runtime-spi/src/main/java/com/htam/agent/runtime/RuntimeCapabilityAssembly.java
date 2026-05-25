package com.htam.agent.runtime;

import java.util.Map;

public record RuntimeCapabilityAssembly(
        Long agentId,
        String runtimeType,
        int toolCount,
        int mcpCount,
        int skillCount,
        int knowledgeCount,
        boolean workspaceEnabled,
        Map<String, Object> attributes) {

    public RuntimeCapabilityAssembly {
        runtimeType = runtimeType == null || runtimeType.isBlank() ? "default" : runtimeType;
        toolCount = Math.max(0, toolCount);
        mcpCount = Math.max(0, mcpCount);
        skillCount = Math.max(0, skillCount);
        knowledgeCount = Math.max(0, knowledgeCount);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
