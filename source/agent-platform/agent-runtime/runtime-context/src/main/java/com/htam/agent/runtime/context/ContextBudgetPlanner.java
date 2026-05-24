package com.htam.agent.runtime.context;

import java.util.List;

public class ContextBudgetPlanner {

    public ContextBudgetDecision plan(
            int maxTokens,
            List<ContextSegmentRef> segments,
            boolean flushMemoryBeforeCompression,
            boolean downgradeOnFailure) {
        int budget = maxTokens <= 0 ? 8192 : maxTokens;
        List<ContextSegmentRef> refs = segments == null ? List.of() : List.copyOf(segments);
        int estimated = refs.stream().mapToInt(ContextSegmentRef::estimatedTokens).sum();
        return new ContextBudgetDecision(
                budget,
                estimated,
                estimated > budget,
                estimated > budget && flushMemoryBeforeCompression,
                downgradeOnFailure,
                refs.stream().map(ContextSegmentRef::segmentId).toList());
    }
}
