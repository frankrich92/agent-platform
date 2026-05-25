package com.htam.agent.capability.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolParallelExecutionPlannerTest {

    @Test
    void onlyReadOnlyAllowedToolsEnterParallelGroup() {
        ToolParallelExecutionPlanner planner = new ToolParallelExecutionPlanner();

        ToolParallelExecutionPlan plan = planner.plan(List.of(
                        tool("search", true, CapabilityRiskLevel.LOW, CapabilityRiskPolicy.ALLOW, true),
                        tool("fetch", true, CapabilityRiskLevel.LOW, CapabilityRiskPolicy.ALLOW, true),
                        tool("write", false, CapabilityRiskLevel.MEDIUM, CapabilityRiskPolicy.ASK, true),
                        tool("blocked", true, CapabilityRiskLevel.HIGH, CapabilityRiskPolicy.DENY, true),
                        tool("disabled", true, CapabilityRiskLevel.LOW, CapabilityRiskPolicy.ALLOW, false)),
                ToolExecutionOptions.enterpriseDefault());

        assertTrue(plan.hasParallelWork());
        assertEquals(List.of("search", "fetch"), plan.parallelGroup().stream()
                .map(ToolExecutionDecision::toolName)
                .toList());
        assertEquals(List.of("write"), plan.serialGroup().stream()
                .map(ToolExecutionDecision::toolName)
                .toList());
        assertEquals(List.of("blocked", "disabled"), plan.deniedGroup().stream()
                .map(ToolExecutionDecision::toolName)
                .toList());
    }

    @Test
    void schemaCacheRefreshesWhenExpiredOrHashChanged() {
        Instant cachedAt = Instant.parse("2026-05-25T00:00:00Z");
        ToolSchemaCacheEntry entry = new ToolSchemaCacheEntry(
                "tool:search",
                "hash-a",
                "schema-ref",
                cachedAt,
                Duration.ofMinutes(10));

        assertFalse(entry.refreshRequired("hash-a", cachedAt.plus(Duration.ofMinutes(5))));
        assertTrue(entry.refreshRequired("hash-b", cachedAt.plus(Duration.ofMinutes(5))));
        assertTrue(entry.refreshRequired("hash-a", cachedAt.plus(Duration.ofMinutes(11))));
    }

    private static CapabilityItem tool(
            String name,
            boolean readOnly,
            CapabilityRiskLevel riskLevel,
            CapabilityRiskPolicy riskPolicy,
            boolean enabled) {
        return new CapabilityItem(
                CapabilityKind.TOOL,
                "tool-" + name,
                name,
                "tool",
                enabled,
                readOnly,
                riskLevel,
                riskPolicy,
                null,
                List.of(),
                List.of(),
                Map.of());
    }
}
