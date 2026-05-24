package com.htam.agent.run.message;

import java.time.Instant;
import java.util.Map;

public record RunMessageRecord(
        String messageId,
        String sessionId,
        String runId,
        RunMessageRole role,
        String content,
        Instant createdAt,
        Map<String, Object> metadata) {

    public RunMessageRecord {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId 不能为空");
        }
        role = role == null ? RunMessageRole.USER : role;
        content = content == null ? "" : content;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
