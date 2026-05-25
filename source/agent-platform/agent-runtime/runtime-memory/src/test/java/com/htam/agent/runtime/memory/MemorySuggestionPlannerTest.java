package com.htam.agent.runtime.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MemorySuggestionPlannerTest {

    @Test
    void createsReviewOnlyMemorySuggestions() {
        MemorySuggestionPlan plan = new MemorySuggestionPlanner().plan(
                "run-1",
                7L,
                9L,
                MemoryScope.USER,
                List.of(" user prefers concise answers ", "", "user prefers concise answers"),
                12,
                Map.of("extractor", "test"));

        assertFalse(plan.silentWriteAllowed());
        assertTrue(plan.humanReviewRequired());
        assertEquals(1, plan.candidates().size());
        MemorySuggestionCandidate candidate = plan.candidates().getFirst();
        assertTrue(candidate.humanReviewRequired());
        assertEquals("user prefers", candidate.content());
        assertEquals("run-1", candidate.sourceRunId());
        assertEquals(true, candidate.metadata().get("truncated"));
    }
}
