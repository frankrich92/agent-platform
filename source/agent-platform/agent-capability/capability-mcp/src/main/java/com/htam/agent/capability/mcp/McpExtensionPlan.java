package com.htam.agent.capability.mcp;

import java.util.List;

public record McpExtensionPlan(
        boolean resourcesEnabled,
        boolean promptsEnabled,
        boolean exposeAgentAsMcpServer,
        List<String> resourceNamespaces,
        List<String> promptNamespaces) {

    public McpExtensionPlan {
        resourceNamespaces = resourceNamespaces == null ? List.of() : List.copyOf(resourceNamespaces);
        promptNamespaces = promptNamespaces == null ? List.of() : List.copyOf(promptNamespaces);
    }
}
