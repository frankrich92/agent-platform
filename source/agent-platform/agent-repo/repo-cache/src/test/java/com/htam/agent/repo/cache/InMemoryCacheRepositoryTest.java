package com.htam.agent.repo.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class InMemoryCacheRepositoryTest {

    @Test
    void cacheHonorsTtlAndEviction() throws InterruptedException {
        InMemoryCacheRepository repository = new InMemoryCacheRepository();

        repository.put("run:1", "value", Duration.ofMillis(20));

        assertEquals("value", repository.get("run:1").orElseThrow());
        Thread.sleep(30L);
        assertTrue(repository.get("run:1").isEmpty());

        repository.put("run:2", "value", Duration.ofMinutes(1));
        assertTrue(repository.evict("run:2"));
        assertTrue(repository.get("run:2").isEmpty());
    }

    @Test
    void cacheSupportsTypedLookupContainsAndPurge() throws InterruptedException {
        InMemoryCacheRepository repository = new InMemoryCacheRepository();

        repository.put("run:1", 42, Duration.ofMinutes(1));
        repository.put("run:2", "expired", Duration.ofMillis(20));

        assertEquals(42, repository.get("run:1", Integer.class).orElseThrow());
        assertTrue(repository.contains("run:1"));

        Thread.sleep(30L);
        assertEquals(1, repository.purgeExpired());
        assertTrue(repository.get("run:2").isEmpty());
    }
}
