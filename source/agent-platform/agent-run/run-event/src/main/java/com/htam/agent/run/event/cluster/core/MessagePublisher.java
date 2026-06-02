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
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        scheduler = Executors.newScheduledThreadPool(2);
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
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
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(channel, message);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(channel, message);
            }
        });
    }

    private void publishWithRetry(String channel, String message, int attempt) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("发布Redis消息成功 - channel: {}", channel);
        } catch (Exception e) {
            if (attempt >= 2 || scheduler == null || scheduler.isShutdown()) {
                log.error("发布Redis消息失败 - channel: {}, error: {}", channel, e.getMessage(), e);
                return;
            }
            long delayMillis = switch (attempt) {
                case 0 -> 100L;
                case 1 -> 300L;
                default -> 500L;
            };
            log.warn("发布Redis消息失败，准备重试 - channel: {}, attempt: {}, error: {}",
                    channel, attempt + 1, e.getMessage());
            scheduler.schedule(() -> publishWithRetry(channel, message, attempt + 1),
                    delayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
