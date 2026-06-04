package com.htam.agent.run.event.cluster.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class ClusterRedisConfigTest {

    @Test
    void redisListenerExecutorUsesDedicatedBackPressurePolicy() {
        ThreadPoolTaskExecutor executor = new ClusterRedisConfig().redisListenerExecutor();

        try {
            ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();

            assertEquals(4, threadPoolExecutor.getCorePoolSize());
            assertEquals(8, threadPoolExecutor.getMaximumPoolSize());
            assertEquals(60, threadPoolExecutor.getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS));
            assertEquals(200, threadPoolExecutor.getQueue().remainingCapacity());
            assertEquals("redis-listener-", executor.getThreadNamePrefix());
            assertInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class,
                    threadPoolExecutor.getRejectedExecutionHandler());
        } finally {
            executor.shutdown();
        }
    }
}
