package com.htam.agent.runtime.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectRuleContextPlannerTest {

    @Test
    void keepsHighestPrecedenceRulePerConflictKey() {
        ProjectRuleContextPlanner planner = new ProjectRuleContextPlanner();

        List<RuntimeContextSegment> segments = planner.planSegments(List.of(
                new ProjectRuleContextSource(
                        "repo/AGENTS.md",
                        MemoryScope.PROFILE,
                        "use apply_patch",
                        10,
                        "edit-policy",
                        List.of("repo")),
                new ProjectRuleContextSource(
                        "parent/AGENTS.md",
                        MemoryScope.ORGANIZATION,
                        "use cat to edit",
                        1,
                        "edit-policy",
                        List.of("org")),
                new ProjectRuleContextSource(
                        "team-memory.md",
                        MemoryScope.USER,
                        "prefer short answers",
                        5,
                        "style",
                        List.of("profile"))));

        assertEquals(List.of("repo/AGENTS.md", "team-memory.md"), segments.stream()
                .map(RuntimeContextSegment::contentRef)
                .toList());
        assertFalse(segments.getFirst().compressible());
        assertEquals("edit-policy", segments.getFirst().attributes().get("conflictKey"));
    }
}
