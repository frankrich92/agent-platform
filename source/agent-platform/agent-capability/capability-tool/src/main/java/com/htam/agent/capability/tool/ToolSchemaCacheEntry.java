package com.htam.agent.capability.tool;

import java.time.Duration;
import java.time.Instant;

public record ToolSchemaCacheEntry(
        String cacheKey,
        String schemaHash,
        String schemaRef,
        Instant cachedAt,
        Duration ttl) {

    public ToolSchemaCacheEntry {
        if (cacheKey == null || cacheKey.isBlank()) {
            throw new IllegalArgumentException("cacheKey 不能为空");
        }
        cachedAt = cachedAt == null ? Instant.now() : cachedAt;
        ttl = ttl == null ? Duration.ofMinutes(10) : ttl;
    }

    public boolean expired(Instant now) {
        Instant current = now == null ? Instant.now() : now;
        return cachedAt.plus(ttl).isBefore(current);
    }
}
