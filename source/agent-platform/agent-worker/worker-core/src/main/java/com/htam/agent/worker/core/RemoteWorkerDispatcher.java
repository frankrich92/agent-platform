package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.time.Duration;
import java.util.Map;

public class RemoteWorkerDispatcher {

    private final WorkerRegistry registry;
    private final WorkerExecutionCoordinator coordinator;
    private final Duration heartbeatTtl;

    public RemoteWorkerDispatcher(
            WorkerRegistry registry,
            WorkerExecutionCoordinator coordinator,
            Duration heartbeatTtl) {
        this.registry = registry;
        this.coordinator = coordinator;
        this.heartbeatTtl = heartbeatTtl == null ? Duration.ofSeconds(30) : heartbeatTtl;
    }

    public WorkerTaskResult dispatch(WorkerTask task) {
        WorkerNode node = registry.listAvailable(task.taskType(), heartbeatTtl).stream()
                .findFirst()
                .orElse(null);
        if (node == null) {
            return new WorkerTaskResult(task.taskId(), WorkerTaskStatus.FAILED, Duration.ZERO,
                    null, "NO_WORKER_AVAILABLE", "no healthy worker accepts " + task.taskType(), Map.of());
        }
        registry.lease(node.workerId());
        WorkerExecutionLease lease = coordinator.lease(task, node.workerId());
        return new WorkerTaskResult(task.taskId(), WorkerTaskStatus.RUNNING, Duration.ZERO,
                "worker task dispatched", null, null,
                Map.of("workerId", lease.workerId(), "endpoint", node.endpoint()));
    }

    public WorkerTaskResult complete(WorkerExecutionLease lease, String outputSummary) {
        registry.release(lease.workerId());
        return coordinator.succeed(lease, outputSummary);
    }

    public WorkerTaskResult fail(WorkerExecutionLease lease, String errorCode, String errorMessage) {
        registry.release(lease.workerId());
        return coordinator.fail(lease, errorCode, errorMessage);
    }
}
