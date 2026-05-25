package com.htam.agent.capability.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class McpExtensionPlannerTest {

    @Test
    void filtersResourcesAndPromptsByAgentExposure() {
        McpExtensionPlan plan = new McpExtensionPlanner().plan(
                List.of(
                        new McpResourceDescriptor("repo", "file-read", "repo://{path}", true, Map.of()),
                        new McpResourceDescriptor("repo", "secret-read", "secret://{key}", true, Map.of())),
                List.of(
                        new McpPromptDescriptor("prompt", "summarize", "prompt://summarize", List.of("text")),
                        new McpPromptDescriptor("prompt", "publish", "prompt://publish", List.of("target"))),
                new AgentMcpServerExposure(
                        1L,
                        "agent-1",
                        true,
                        List.of(),
                        List.of("file-read"),
                        List.of("summarize")));

        assertTrue(plan.resourcesEnabled());
        assertTrue(plan.promptsEnabled());
        assertEquals(1, plan.authorizedResourceCount());
        assertEquals(1, plan.authorizedPromptCount());
        assertEquals(List.of("repo"), plan.resourceNamespaces());
        assertEquals(List.of("prompt"), plan.promptNamespaces());
    }

    @Test
    void disablesExtensionsWhenExposureIsDisabled() {
        McpExtensionPlan plan = new McpExtensionPlanner().plan(
                List.of(new McpResourceDescriptor("repo", "file-read", "repo://{path}", true, Map.of())),
                List.of(new McpPromptDescriptor("prompt", "summarize", "prompt://summarize", List.of())),
                new AgentMcpServerExposure(1L, "agent-1", false, List.of(), List.of("file-read"), List.of("summarize")));

        assertFalse(plan.resourcesEnabled());
        assertFalse(plan.promptsEnabled());
        assertEquals(0, plan.authorizedResourceCount());
        assertEquals(0, plan.authorizedPromptCount());
    }
}
