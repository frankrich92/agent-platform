package com.htam.agent.worker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.worker.spi.WorkerRiskPolicy;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import com.htam.agent.worker.spi.WorkerTaskType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RemoteWorkerDispatcherTest {

    @Test
    void dispatchesToHealthyWorkerAndRecordsLease() {
        InMemoryWorkerRegistry registry = new InMemoryWorkerRegistry();
        registry.register(new WorkerNode(
                "worker-1",
                "http://worker-1",
                List.of(WorkerTaskType.SHELL),
                1,
                0,
                null,
                Map.of()));
        InMemoryWorkerTaskLedger ledger = new InMemoryWorkerTaskLedger();
        RemoteWorkerDispatcher dispatcher = new RemoteWorkerDispatcher(
                registry,
                new WorkerExecutionCoordinator(ledger),
                Duration.ofSeconds(30));

        WorkerTask task = new WorkerTask("task-1", WorkerTaskType.SHELL, "workspace-1",
                "echo ok", WorkerRiskPolicy.ALLOW, Map.of());

        assertEquals(WorkerTaskStatus.RUNNING, dispatcher.dispatch(task).status());
        assertTrue(ledger.findTask("task-1").isPresent());
        assertEquals(1, registry.findById("worker-1").orElseThrow().activeTasks());
    }
}
