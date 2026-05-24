package com.htam.agent.runtime.memory;

import java.util.List;

public record RuntimeContextPlan(
        String planId,
        String runId,
        TokenBudget tokenBudget,
        MemoryWritePolicy memoryWritePolicy,
        List<RuntimeContextSegment> segments,
        List<CompressionSpan> compressionSpans,
        boolean compressionRequired,
        boolean memoryFlushRequired,
        CompressionStatus status,
        String fallbackReason) {

    public RuntimeContextPlan {
        planId = planId == null || planId.isBlank() ? "runtime-context-default" : planId;
        tokenBudget = tokenBudget == null ? new TokenBudget(8192, 0, 0, 0) : tokenBudget;
        memoryWritePolicy = memoryWritePolicy == null ? MemoryWritePolicy.enterpriseDefault() : memoryWritePolicy;
        segments = segments == null ? List.of() : List.copyOf(segments);
        compressionSpans = compressionSpans == null ? List.of() : List.copyOf(compressionSpans);
        status = status == null ? CompressionStatus.SKIPPED : status;
    }

    public int totalTokens() {
        return segments.stream().mapToInt(RuntimeContextSegment::tokenEstimate).sum();
    }

    public int overBudgetTokens() {
        return Math.max(0, totalTokens() - tokenBudget.contextTokens());
    }
}
