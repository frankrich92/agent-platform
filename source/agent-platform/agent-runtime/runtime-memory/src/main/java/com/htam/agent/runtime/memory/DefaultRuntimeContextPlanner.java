package com.htam.agent.runtime.memory;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class DefaultRuntimeContextPlanner implements RuntimeContextPlanner {

    @Override
    public RuntimeContextPlan plan(
            String runId,
            TokenBudget tokenBudget,
            MemoryWritePolicy memoryWritePolicy,
            List<RuntimeContextSegment> segments) {
        TokenBudget budget = tokenBudget == null ? new TokenBudget(8192, 0, 0, 0) : tokenBudget;
        MemoryWritePolicy writePolicy = memoryWritePolicy == null
                ? MemoryWritePolicy.enterpriseDefault()
                : memoryWritePolicy;
        List<RuntimeContextSegment> contextSegments = segments == null ? List.of() : List.copyOf(segments);
        int totalTokens = contextSegments.stream().mapToInt(RuntimeContextSegment::tokenEstimate).sum();
        int overBudgetTokens = Math.max(0, totalTokens - budget.contextTokens());
        if (overBudgetTokens == 0) {
            return new RuntimeContextPlan(planId(runId), runId, budget, writePolicy, contextSegments,
                    List.of(), false, false, CompressionStatus.SKIPPED, null);
        }

        List<Integer> compressibleIndexes = IntStream.range(0, contextSegments.size())
                .filter(index -> contextSegments.get(index).compressible())
                .boxed()
                .toList();
        if (compressibleIndexes.isEmpty()) {
            CompressionStatus status = writePolicy.downgradeOnCompressionFailure()
                    ? CompressionStatus.FAILED_DOWNGRADED
                    : CompressionStatus.FAILED_BLOCKED;
            return new RuntimeContextPlan(planId(runId), runId, budget, writePolicy, contextSegments,
                    List.of(failedSpan(runId, totalTokens, status)),
                    true,
                    writePolicy.flushBeforeCompression(),
                    status,
                    "no compressible context segment");
        }

        int originalTokens = compressibleIndexes.stream()
                .mapToInt(index -> contextSegments.get(index).tokenEstimate())
                .sum();
        int compressedTokens = Math.max(0, originalTokens - overBudgetTokens);
        CompressionSpan span = new CompressionSpan(
                UUID.randomUUID().toString(),
                runId,
                compressibleIndexes.getFirst(),
                compressibleIndexes.getLast(),
                originalTokens,
                compressedTokens,
                CompressionStatus.SUCCEEDED,
                null);
        return new RuntimeContextPlan(planId(runId), runId, budget, writePolicy, contextSegments,
                List.of(span), true, writePolicy.flushBeforeCompression(), CompressionStatus.SUCCEEDED, null);
    }

    private static CompressionSpan failedSpan(String runId, int originalTokens, CompressionStatus status) {
        return new CompressionSpan(UUID.randomUUID().toString(), runId, 0, 0,
                originalTokens, originalTokens, status, "compression unavailable");
    }

    private static String planId(String runId) {
        return runId == null || runId.isBlank()
                ? "runtime-context-" + UUID.randomUUID()
                : "runtime-context-" + runId;
    }
}
