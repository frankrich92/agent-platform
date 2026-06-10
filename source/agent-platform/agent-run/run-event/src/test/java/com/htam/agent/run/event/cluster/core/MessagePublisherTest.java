package com.htam.agent.run.event.cluster.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class MessagePublisherTest {

    @Test
    void publishRetriesTransientRedisFailure() throws InterruptedException {
        RecordingRedisTemplate redisTemplate = new RecordingRedisTemplate(1);
        MessagePublisher publisher = new MessagePublisher(redisTemplate);
        publisher.init();

        try {
            publisher.publish("agent-events", "payload");

            assertTrue(redisTemplate.awaitPublish(), "message should be published after retry");
            assertEquals(2, redisTemplate.attempts());
            assertEquals("agent-events", redisTemplate.channel());
            assertEquals("payload", redisTemplate.message());
        } finally {
            publisher.destroy();
        }
    }

    @Test
    void publishAfterCommitPublishesWhenOnlySynchronizationIsActive() throws InterruptedException {
        RecordingRedisTemplate redisTemplate = new RecordingRedisTemplate(0);
        MessagePublisher publisher = new MessagePublisher(redisTemplate);
        publisher.init();
        TransactionSynchronizationManager.initSynchronization();

        try {
            publisher.publishAfterCommit("agent-events", "payload");

            assertTrue(redisTemplate.awaitPublish(), "message should be published without waiting for a real transaction");
            assertEquals(1, redisTemplate.attempts());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            publisher.destroy();
        }
    }

    private static class RecordingRedisTemplate extends StringRedisTemplate {

        private final AtomicInteger attempts = new AtomicInteger();
        private final CountDownLatch publishLatch = new CountDownLatch(1);
        private final int failuresBeforeSuccess;

        private volatile String channel;
        private volatile Object message;

        RecordingRedisTemplate(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        @Override
        public Long convertAndSend(String channel, Object message) {
            int attempt = attempts.incrementAndGet();
            if (attempt <= failuresBeforeSuccess) {
                throw new IllegalStateException("redis unavailable");
            }
            this.channel = channel;
            this.message = message;
            publishLatch.countDown();
            return 1L;
        }

        boolean awaitPublish() throws InterruptedException {
            return publishLatch.await(2, TimeUnit.SECONDS);
        }

        int attempts() {
            return attempts.get();
        }

        String channel() {
            return channel;
        }

        Object message() {
            return message;
        }
    }
}
