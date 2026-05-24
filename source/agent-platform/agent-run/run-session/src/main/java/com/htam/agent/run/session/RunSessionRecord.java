package com.htam.agent.run.session;

import java.time.Instant;
import java.util.Map;

public record RunSessionRecord(
        String sessionId,
        Long agentId,
        String title,
        RunSessionStatus status,
        Instant createdAt,
        Instant updatedAt,
        Map<String, Object> metadata) {

    public RunSessionRecord {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        status = status == null ? RunSessionStatus.ACTIVE : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
