package com.htam.agent.worker.coding;

import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.time.Instant;

public record CliCodingAgentSession(
        String sessionId,
        CliCodingAgentSpec spec,
        WorkerTask task,
        WorkerTaskStatus status,
        Instant startedAt,
        WorkerTaskResult lastResult) {

    public CliCodingAgentSession {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        if (spec == null) {
            throw new IllegalArgumentException("spec 不能为空");
        }
        if (task == null) {
            throw new IllegalArgumentException("task 不能为空");
        }
        status = status == null ? WorkerTaskStatus.ACCEPTED : status;
        startedAt = startedAt == null ? Instant.now() : startedAt;
    }

    public CliCodingAgentSession withResult(WorkerTaskResult result) {
        return new CliCodingAgentSession(sessionId, spec, task, result.status(), startedAt, result);
    }
}
