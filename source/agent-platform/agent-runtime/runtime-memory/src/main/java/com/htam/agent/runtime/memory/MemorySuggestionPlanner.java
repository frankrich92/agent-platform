package com.htam.agent.runtime.memory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MemorySuggestionPlanner {

    public MemorySuggestionPlan plan(
            String runId,
            Long ownerId,
            Long agentId,
            MemoryScope scope,
            List<String> extractedFacts,
            int maxMemoryChars,
            Map<String, Object> metadata) {
        int maxChars = maxMemoryChars <= 0 ? MemoryWritePolicy.enterpriseDefault().maxMemoryChars() : maxMemoryChars;
        List<MemorySuggestionCandidate> candidates = (extractedFacts == null ? List.<String>of() : extractedFacts)
                .stream()
                .map(fact -> fact == null ? "" : fact.strip())
                .filter(fact -> !fact.isBlank())
                .distinct()
                .map(fact -> candidate(runId, ownerId, agentId, scope, fact, maxChars, metadata))
                .toList();
        return new MemorySuggestionPlan("memory-suggestion-" + stableRunId(runId), runId, false, true, candidates);
    }

    private static MemorySuggestionCandidate candidate(
            String runId,
            Long ownerId,
            Long agentId,
            MemoryScope scope,
            String fact,
            int maxChars,
            Map<String, Object> metadata) {
        boolean truncated = fact.length() > maxChars;
        String content = truncated ? fact.substring(0, maxChars) : fact;
        Map<String, Object> attributes = new LinkedHashMap<>(metadata == null ? Map.of() : metadata);
        attributes.put("truncated", truncated);
        return new MemorySuggestionCandidate(
                UUID.randomUUID().toString(),
                ownerId,
                agentId,
                scope,
                content,
                runId,
                "run:" + stableRunId(runId),
                0.5,
                true,
                Instant.now(),
                attributes);
    }

    private static String stableRunId(String runId) {
        return runId == null || runId.isBlank() ? "unknown" : runId;
    }
}
