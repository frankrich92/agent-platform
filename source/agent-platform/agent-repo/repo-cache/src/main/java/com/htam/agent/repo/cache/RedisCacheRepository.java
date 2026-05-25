package com.htam.agent.repo.cache;

import com.htam.agent.common.util.RedisUtils;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheRepository implements CacheRepository {

    private final RedisUtils redisUtils;

    public RedisCacheRepository(RedisUtils redisUtils) {
        if (redisUtils == null) {
            throw new IllegalArgumentException("redisUtils 不能为空");
        }
        this.redisUtils = redisUtils;
    }

    @Override
    public CacheEntry put(String key, Object value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            redisUtils.set(key, value);
        } else {
            redisUtils.setEx(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
        }
        return CacheEntry.of(key, value, ttl);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(redisUtils.get(key));
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(redisUtils.get(key, type));
    }

    @Override
    public boolean contains(String key) {
        return redisUtils.hasKey(key);
    }

    @Override
    public boolean evict(String key) {
        boolean existed = contains(key);
        redisUtils.delete(key);
        return existed;
    }

    @Override
    public int purgeExpired() {
        return 0;
    }
}
