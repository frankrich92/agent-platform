package com.htam.agent.capability.mcp;

import java.util.List;

public record McpExtensionPlan(
        boolean resourcesEnabled,
        boolean promptsEnabled,
        boolean exposeAgentAsMcpServer,
        List<String> resourceNamespaces,
        List<String> promptNamespaces,
        int authorizedResourceCount,
        int authorizedPromptCount) {

    public McpExtensionPlan(
            boolean resourcesEnabled,
            boolean promptsEnabled,
            boolean exposeAgentAsMcpServer,
            List<String> resourceNamespaces,
            List<String> promptNamespaces) {
        this(resourcesEnabled, promptsEnabled, exposeAgentAsMcpServer, resourceNamespaces, promptNamespaces,
                resourceNamespaces == null ? 0 : resourceNamespaces.size(),
                promptNamespaces == null ? 0 : promptNamespaces.size());
    }

    public McpExtensionPlan {
        resourceNamespaces = resourceNamespaces == null ? List.of() : List.copyOf(resourceNamespaces);
        promptNamespaces = promptNamespaces == null ? List.of() : List.copyOf(promptNamespaces);
        authorizedResourceCount = Math.max(0, authorizedResourceCount);
        authorizedPromptCount = Math.max(0, authorizedPromptCount);
    }
}
