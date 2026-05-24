package com.htam.agent.worker.spi;

import java.time.Duration;
import java.util.Map;

public record WorkerTaskResult(
        String taskId,
        WorkerTaskStatus status,
        Duration duration,
        String outputSummary,
        String errorCode,
        String errorMessage,
        Map<String, Object> metadata) {

    public WorkerTaskResult {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        status = status == null ? WorkerTaskStatus.ACCEPTED : status;
        duration = duration == null ? Duration.ZERO : duration;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
