package com.htam.agent.repo.cache;

import java.time.Duration;
import java.util.Optional;

public interface CacheRepository {

    CacheEntry put(String key, Object value, Duration ttl);

    Optional<Object> get(String key);

    default <T> Optional<T> get(String key, Class<T> type) {
        return get(key).filter(type::isInstance).map(type::cast);
    }

    boolean contains(String key);

    boolean evict(String key);

    int purgeExpired();
}
