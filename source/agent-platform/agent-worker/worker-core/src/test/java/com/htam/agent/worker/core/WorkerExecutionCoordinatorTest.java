package com.htam.agent.worker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.worker.spi.WorkerRiskPolicy;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import com.htam.agent.worker.spi.WorkerTaskType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WorkerExecutionCoordinatorTest {

    @Test
    void leaseAndCompleteRecordsTaskLifecycle() {
        InMemoryWorkerTaskLedger ledger = new InMemoryWorkerTaskLedger();
        WorkerExecutionCoordinator coordinator = new WorkerExecutionCoordinator(ledger);
        WorkerTask task = new WorkerTask(
                "task-1",
                WorkerTaskType.SHELL,
                "workspace-1",
                "echo ok",
                WorkerRiskPolicy.ALLOW,
                Map.of());

        WorkerExecutionLease lease = coordinator.lease(task, "worker-1");
        coordinator.succeed(lease, "ok");

        assertTrue(ledger.findTask("task-1").isPresent());
        assertEquals(WorkerTaskStatus.SUCCEEDED, ledger.findResult("task-1").orElseThrow().status());
    }
}
