package com.htam.agent.worker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.worker.spi.WorkerRiskPolicy;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import com.htam.agent.worker.spi.WorkerTaskType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GovernedWorkerRuntimeTest {

    @Test
    void submitsWorkerTaskThroughPolicyAndLedger() {
        InMemoryWorkerTaskLedger ledger = new InMemoryWorkerTaskLedger();
        GovernedWorkerRuntime runtime = new GovernedWorkerRuntime(ledger);
        WorkerTask task = new WorkerTask("task-1", WorkerTaskType.SHELL, "workspace-1",
                "mvn test", WorkerRiskPolicy.ASK, Map.of());

        assertEquals(WorkerTaskStatus.APPROVAL_REQUIRED, runtime.submit(task).status());
        assertTrue(ledger.findTask("task-1").isPresent());
        assertTrue(ledger.findResult("task-1").isPresent());
    }
}
