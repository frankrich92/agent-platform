package com.htam.agent.run.event.cluster.core;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述：Redis消息发布工具
 * 用于向Redis频道发布消息
 *
 * @author huxuehao
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_RETRIES = 3;
    private static final long[] RETRY_DELAYS_MS = {100L, 300L, 500L};

    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        scheduler = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "redis-publish");
            thread.setDaemon(true);
            return thread;
        });
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 向指定频道发布消息
     *
     * @param channel 频道名称
     * @param message 消息内容
     */
    public void publish(String channel, String message) {
        publishWithRetry(channel, message, 0);
    }

    /**
     * 当前存在事务时，延迟到提交后发布，避免订阅端读到未提交数据。
     */
    public void publishAfterCommit(String channel, String message) {
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (synchronizationActive && transactionActive) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeAsync(channel, message);
                }
            });
        } else {
            executeAsync(channel, message);
        }
    }

    private void executeAsync(String channel, String message) {
        if (scheduler == null || scheduler.isShutdown()) {
            publish(channel, message);
            return;
        }
        scheduler.execute(() -> publishWithRetry(channel, message, 0));
    }

    private void publishWithRetry(String channel, String message, int attempt) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("发布Redis消息成功 - channel: {}", channel);
        } catch (Exception e) {
            if (attempt >= MAX_RETRIES - 1 || scheduler == null || scheduler.isShutdown()) {
                log.error("发布Redis消息失败 - channel: {}, error: {}", channel, e.getMessage(), e);
                return;
            }
            long delayMillis = RETRY_DELAYS_MS[attempt];
            log.warn("发布Redis消息失败，准备重试 - channel: {}, attempt: {}, error: {}",
                    channel, attempt + 1, e.getMessage());
            scheduler.schedule(() -> publishWithRetry(channel, message, attempt + 1),
                    delayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
