package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class WorkerExecutionCoordinator {

    private final WorkerTaskLedger ledger;

    public WorkerExecutionCoordinator(WorkerTaskLedger ledger) {
        this.ledger = ledger;
    }

    public WorkerExecutionLease lease(WorkerTask task, String workerId) {
        if (ledger != null) {
            ledger.appendTask(task);
            ledger.appendResult(new WorkerTaskResult(
                    task.taskId(),
                    WorkerTaskStatus.RUNNING,
                    Duration.ZERO,
                    "worker task leased",
                    null,
                    null,
                    Map.of("workerId", workerId == null ? "local" : workerId)));
        }
        return new WorkerExecutionLease(task.taskId(), workerId == null ? "local" : workerId, Instant.now());
    }

    public WorkerTaskResult succeed(WorkerExecutionLease lease, String outputSummary) {
        return finish(lease, WorkerTaskStatus.SUCCEEDED, outputSummary, null, null);
    }

    public WorkerTaskResult fail(WorkerExecutionLease lease, String errorCode, String errorMessage) {
        return finish(lease, WorkerTaskStatus.FAILED, null, errorCode, errorMessage);
    }

    private WorkerTaskResult finish(
            WorkerExecutionLease lease,
            WorkerTaskStatus status,
            String outputSummary,
            String errorCode,
            String errorMessage) {
        WorkerTaskResult result = new WorkerTaskResult(
                lease.taskId(),
                status,
                Duration.between(lease.leasedAt(), Instant.now()),
                outputSummary,
                errorCode,
                errorMessage,
                Map.of("workerId", lease.workerId()));
        if (ledger != null) {
            ledger.appendResult(result);
        }
        return result;
    }
}
