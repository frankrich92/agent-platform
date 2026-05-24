package com.htam.agent.runtime.memory;

import java.time.Instant;
import java.util.Map;

public record LongTermMemory(
        String memoryId,
        Long ownerId,
        Long agentId,
        MemoryScope scope,
        String content,
        String sourceRunId,
        double confidence,
        Instant updatedAt,
        Map<String, Object> metadata) {

    public LongTermMemory {
        if (memoryId == null || memoryId.isBlank()) {
            throw new IllegalArgumentException("memoryId 不能为空");
        }
        scope = scope == null ? MemoryScope.SESSION : scope;
        updatedAt = updatedAt == null ? Instant.now() : updatedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
