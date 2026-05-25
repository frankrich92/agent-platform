package com.htam.agent.runtime.memory;

import java.util.List;

public record MemorySuggestionPlan(
        String planId,
        String runId,
        boolean silentWriteAllowed,
        boolean humanReviewRequired,
        List<MemorySuggestionCandidate> candidates) {

    public MemorySuggestionPlan {
        planId = planId == null || planId.isBlank() ? "memory-suggestion-" + runId : planId;
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        humanReviewRequired = humanReviewRequired || !candidates.isEmpty();
        silentWriteAllowed = false;
    }
}
