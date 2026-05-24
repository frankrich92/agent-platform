package com.htam.agent.runtime.context;

import java.util.List;

public record ContextBudgetDecision(
        int maxTokens,
        int estimatedTokens,
        boolean compressionRequired,
        boolean flushMemoryBeforeCompression,
        boolean downgradeOnFailure,
        List<String> selectedSegmentIds) {

    public ContextBudgetDecision {
        selectedSegmentIds = selectedSegmentIds == null ? List.of() : List.copyOf(selectedSegmentIds);
    }
}
