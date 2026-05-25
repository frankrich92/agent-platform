package com.htam.agent.capability.mcp;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class McpExtensionPlanner {

    public McpExtensionPlan plan(
            List<McpResourceDescriptor> resources,
            List<McpPromptDescriptor> prompts,
            AgentMcpServerExposure agentExposure) {
        List<McpResourceDescriptor> resourceList = filterAuthorized(
                resources == null ? List.of() : List.copyOf(resources),
                agentExposure,
                AgentMcpServerExposure::exposedResourceNames,
                McpResourceDescriptor::resourceName);
        List<McpPromptDescriptor> promptList = filterAuthorized(
                prompts == null ? List.of() : List.copyOf(prompts),
                agentExposure,
                AgentMcpServerExposure::exposedPromptNames,
                McpPromptDescriptor::promptName);
        return new McpExtensionPlan(
                !resourceList.isEmpty(),
                !promptList.isEmpty(),
                agentExposure != null && agentExposure.enabled(),
                resourceList.stream().map(McpResourceDescriptor::namespace).distinct().toList(),
                promptList.stream().map(McpPromptDescriptor::namespace).distinct().toList(),
                resourceList.size(),
                promptList.size());
    }

    private static <T> List<T> filterAuthorized(
            List<T> items,
            AgentMcpServerExposure agentExposure,
            Function<AgentMcpServerExposure, List<String>> exposureNames,
            Function<T, String> itemName) {
        if (agentExposure == null) {
            return items;
        }
        if (!agentExposure.enabled()) {
            return List.of();
        }
        Set<String> allowed = Set.copyOf(exposureNames.apply(agentExposure));
        if (allowed.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(item -> allowed.contains(itemName.apply(item)))
                .toList();
    }
}
