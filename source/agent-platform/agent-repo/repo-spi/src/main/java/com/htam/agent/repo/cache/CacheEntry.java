package com.htam.agent.repo.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public record CacheEntry(String key, Object value, Instant createdAt, Instant expiresAt) {

    public CacheEntry {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key 不能为空");
        }
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static CacheEntry of(String key, Object value, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = ttl == null || ttl.isZero() || ttl.isNegative() ? null : now.plus(ttl);
        return new CacheEntry(key, value, now, expiresAt);
    }

    public boolean expired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now == null ? Instant.now() : now);
    }

    public Optional<Object> visibleValue(Instant now) {
        return expired(now) ? Optional.empty() : Optional.ofNullable(value);
    }
}
