package com.htam.agent.repo.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryCacheRepository implements CacheRepository {

    private final ConcurrentMap<String, CacheEntry> entries = new ConcurrentHashMap<>();

    @Override
    public CacheEntry put(String key, Object value, Duration ttl) {
        CacheEntry entry = CacheEntry.of(key, value, ttl);
        entries.put(key, entry);
        return entry;
    }

    @Override
    public Optional<Object> get(String key) {
        CacheEntry entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expired(Instant.now())) {
            entries.remove(key, entry);
            return Optional.empty();
        }
        return entry.visibleValue(Instant.now());
    }

    @Override
    public boolean contains(String key) {
        return get(key).isPresent();
    }

    @Override
    public boolean evict(String key) {
        return entries.remove(key) != null;
    }

    @Override
    public int purgeExpired() {
        Instant now = Instant.now();
        int before = entries.size();
        entries.entrySet().removeIf(entry -> entry.getValue().expired(now));
        return before - entries.size();
    }
}
