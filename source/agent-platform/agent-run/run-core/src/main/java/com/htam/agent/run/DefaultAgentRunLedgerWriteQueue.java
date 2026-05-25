package com.htam.agent.run;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DefaultAgentRunLedgerWriteQueue implements AgentRunLedgerWriteQueue, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(DefaultAgentRunLedgerWriteQueue.class);
    private static final String PROPERTY_PREFIX = "agent.run.ledger.";

    private final boolean asyncEnabled;
    private final boolean failFastOnOverflow;
    private final long shutdownTimeoutMillis;
    private final int maxRetries;
    private final long retryBackoffMillis;
    private final ThreadPoolExecutor executor;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Autowired
    public DefaultAgentRunLedgerWriteQueue(Environment environment) {
        this(
                property(environment, "async-enabled", Boolean.class, false),
                property(environment, "queue-capacity", Integer.class, 10_000),
                property(environment, "shutdown-timeout-millis", Long.class, 5_000L),
                property(environment, "fail-fast-on-overflow", Boolean.class, false),
                property(environment, "max-retries", Integer.class, 3),
                property(environment, "retry-backoff-millis", Long.class, 100L));
    }

    DefaultAgentRunLedgerWriteQueue(
            boolean asyncEnabled,
            int queueCapacity,
            long shutdownTimeoutMillis,
            boolean failFastOnOverflow) {
        this(asyncEnabled, queueCapacity, shutdownTimeoutMillis, failFastOnOverflow, 3, 100L);
    }

    DefaultAgentRunLedgerWriteQueue(
            boolean asyncEnabled,
            int queueCapacity,
            long shutdownTimeoutMillis,
            boolean failFastOnOverflow,
            int maxRetries,
            long retryBackoffMillis) {
        this.asyncEnabled = asyncEnabled;
        this.failFastOnOverflow = failFastOnOverflow;
        this.shutdownTimeoutMillis = Math.max(0L, shutdownTimeoutMillis);
        this.maxRetries = Math.max(0, maxRetries);
        this.retryBackoffMillis = Math.max(0L, retryBackoffMillis);
        this.executor = asyncEnabled ? buildExecutor(Math.max(1, queueCapacity)) : null;
    }

    @Override
    public void submit(String description, Runnable write) {
        if (write == null) {
            return;
        }
        if (!asyncEnabled) {
            runWithRetry(description, write);
            return;
        }
        if (closed.get()) {
            runWithRetry(description, write);
            return;
        }
        try {
            executor.execute(() -> runWithRetry(description, write));
        } catch (RejectedExecutionException ex) {
            if (failFastOnOverflow) {
                throw ex;
            }
            log.warn("Agent run ledger queue is full; writing inline: {}", description);
            runWithRetry(description, write);
        }
    }

    @Override
    public void flush() {
        if (!asyncEnabled || closed.get()) {
            return;
        }
        CompletableFuture<Void> barrier = new CompletableFuture<>();
        try {
            if (!enqueueBarrier(barrier)) {
                return;
            }
            barrier.get();
        } catch (RejectedExecutionException ex) {
            if (failFastOnOverflow) {
                throw ex;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while flushing agent run ledger queue", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Failed to flush agent run ledger queue", ex);
        }
    }

    @Override
    public void close() {
        if (!asyncEnabled || !closed.compareAndSet(false, true)) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(shutdownTimeoutMillis, TimeUnit.MILLISECONDS)) {
                log.warn("Agent run ledger queue did not drain within {} ms", shutdownTimeoutMillis);
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    @Override
    public void destroy() {
        close();
    }

    private static ThreadPoolExecutor buildExecutor(int queueCapacity) {
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                runnable -> {
                    Thread thread = new Thread(runnable, "agent-run-ledger-writer");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy());
    }

    private boolean enqueueBarrier(CompletableFuture<Void> barrier) throws InterruptedException {
        while (!closed.get()) {
            try {
                executor.execute(() -> barrier.complete(null));
                return true;
            } catch (RejectedExecutionException ex) {
                if (failFastOnOverflow) {
                    throw ex;
                }
                Thread.sleep(10L);
            }
        }
        return false;
    }

    private void runWithRetry(String description, Runnable write) {
        int attempts = 0;
        while (true) {
            try {
                write.run();
                return;
            } catch (RuntimeException ex) {
                if (attempts >= maxRetries) {
                    log.warn(
                            "Failed to persist agent run ledger entry after {} retries: {} ({})",
                            attempts,
                            description,
                            ex.toString());
                    log.debug("Agent run ledger write failure details: {}", description, ex);
                    return;
                }
                attempts++;
                log.warn(
                        "Failed to persist agent run ledger entry; retrying {}/{}: {} ({})",
                        attempts,
                        maxRetries,
                        description,
                        ex.toString());
                log.debug("Agent run ledger write retry details: {}", description, ex);
                sleepBeforeRetry();
            }
        }
    }

    private void sleepBeforeRetry() {
        if (retryBackoffMillis <= 0L) {
            return;
        }
        try {
            Thread.sleep(retryBackoffMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static <T> T property(Environment environment, String name, Class<T> type, T defaultValue) {
        if (environment == null) {
            return defaultValue;
        }
        return environment.getProperty(PROPERTY_PREFIX + name, type, defaultValue);
    }
}
