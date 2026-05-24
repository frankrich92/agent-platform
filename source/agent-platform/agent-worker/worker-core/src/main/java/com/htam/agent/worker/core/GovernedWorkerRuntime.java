package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerRiskPolicy;
import com.htam.agent.worker.spi.WorkerRuntime;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.time.Duration;
import java.util.Map;

public class GovernedWorkerRuntime implements WorkerRuntime {

    private final WorkerTaskLedger ledger;

    public GovernedWorkerRuntime(WorkerTaskLedger ledger) {
        this.ledger = ledger;
    }

    @Override
    public WorkerTaskResult submit(WorkerTask task) {
        if (ledger != null) {
            ledger.appendTask(task);
        }
        WorkerTaskStatus status = switch (task.riskPolicy()) {
            case DENY -> WorkerTaskStatus.DENIED;
            case ASK -> WorkerTaskStatus.APPROVAL_REQUIRED;
            case ALLOW -> WorkerTaskStatus.ACCEPTED;
        };
        WorkerTaskResult result = new WorkerTaskResult(task.taskId(), status, Duration.ZERO,
                status == WorkerTaskStatus.ACCEPTED ? "worker task accepted" : null,
                status == WorkerTaskStatus.DENIED ? "WORKER_POLICY_DENIED" : null,
                status == WorkerTaskStatus.DENIED ? "worker task denied by policy" : null,
                Map.of("taskType", task.taskType().name()));
        if (ledger != null) {
            ledger.appendResult(result);
        }
        return result;
    }
}
