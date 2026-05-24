package com.htam.agent.capability.mcp;

import java.util.List;

public class McpExtensionPlanner {

    public McpExtensionPlan plan(
            List<McpResourceDescriptor> resources,
            List<McpPromptDescriptor> prompts,
            AgentMcpServerExposure agentExposure) {
        List<McpResourceDescriptor> resourceList = resources == null ? List.of() : List.copyOf(resources);
        List<McpPromptDescriptor> promptList = prompts == null ? List.of() : List.copyOf(prompts);
        return new McpExtensionPlan(
                !resourceList.isEmpty(),
                !promptList.isEmpty(),
                agentExposure != null && agentExposure.enabled(),
                resourceList.stream().map(McpResourceDescriptor::namespace).distinct().toList(),
                promptList.stream().map(McpPromptDescriptor::namespace).distinct().toList());
    }
}
