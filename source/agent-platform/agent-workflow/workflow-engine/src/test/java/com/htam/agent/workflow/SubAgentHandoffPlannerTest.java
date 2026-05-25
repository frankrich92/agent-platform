package com.htam.agent.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SubAgentHandoffPlannerTest {

    private final SubAgentHandoffPlanner planner = new SubAgentHandoffPlanner();

    @Test
    void createsIsolatedBoundedHandoffWithDepthMetadata() {
        SubAgentHandoffPlan plan = planner.plan(new SubAgentHandoffRequest(
                10L,
                20L,
                "run-1",
                "abcdef",
                3,
                1,
                3,
                Map.of("purpose", "research")));

        assertEquals("abc", plan.input());
        assertTrue(plan.summaryRequired());
        assertTrue((Boolean) plan.handoff().get("isolatedContext"));
        assertTrue((Boolean) plan.handoff().get("inputTruncated"));
        assertEquals(2, plan.currentDepth());
        assertEquals(3, plan.maxDepth());
        assertEquals(2, plan.handoff().get("nextDepth"));
    }

    @Test
    void rejectsRecursiveHandoffWhenDepthLimitReached() {
        SubAgentHandoffRequest request = new SubAgentHandoffRequest(
                10L,
                20L,
                "run-1",
                "task",
                4000,
                3,
                3,
                Map.of());

        assertThrows(IllegalStateException.class, () -> planner.plan(request));
    }
}
