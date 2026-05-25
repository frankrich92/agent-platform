package com.htam.agent.worker.core;

import java.time.Instant;

public record WorkerExecutionLease(String taskId, String workerId, Instant leasedAt) {

    public WorkerExecutionLease {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        workerId = workerId == null || workerId.isBlank() ? "local" : workerId;
        leasedAt = leasedAt == null ? Instant.now() : leasedAt;
    }
}
