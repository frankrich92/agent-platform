package com.htam.agent.capability.mcp;

import java.util.List;

public record AgentMcpServerExposure(
        Long agentId,
        String serverName,
        boolean enabled,
        List<String> exposedToolNames,
        List<String> exposedPromptNames) {

    public AgentMcpServerExposure {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        serverName = serverName == null || serverName.isBlank() ? "agent-" + agentId : serverName;
        exposedToolNames = exposedToolNames == null ? List.of() : List.copyOf(exposedToolNames);
        exposedPromptNames = exposedPromptNames == null ? List.of() : List.copyOf(exposedPromptNames);
    }
}
