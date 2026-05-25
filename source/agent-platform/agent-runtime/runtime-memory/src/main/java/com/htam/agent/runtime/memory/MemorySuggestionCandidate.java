package com.htam.agent.runtime.memory;

import java.time.Instant;
import java.util.Map;

public record MemorySuggestionCandidate(
        String suggestionId,
        Long ownerId,
        Long agentId,
        MemoryScope scope,
        String content,
        String sourceRunId,
        String sourceRef,
        double confidence,
        boolean humanReviewRequired,
        Instant createdAt,
        Map<String, Object> metadata) {

    public MemorySuggestionCandidate {
        if (suggestionId == null || suggestionId.isBlank()) {
            throw new IllegalArgumentException("suggestionId 不能为空");
        }
        scope = scope == null ? MemoryScope.SESSION : scope;
        content = content == null ? "" : content.strip();
        sourceRef = sourceRef == null || sourceRef.isBlank() ? sourceRunId : sourceRef;
        confidence = Math.max(0, Math.min(1, confidence));
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
