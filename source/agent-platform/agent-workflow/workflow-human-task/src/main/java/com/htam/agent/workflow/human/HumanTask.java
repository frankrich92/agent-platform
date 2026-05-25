package com.htam.agent.workflow.human;

import java.time.Instant;
import java.util.Map;

public record HumanTask(
        String taskId,
        String workflowId,
        String runId,
        String nodeId,
        HumanTaskStatus status,
        Instant createdAt,
        Map<String, Object> payload) {

    public HumanTask {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        status = status == null ? HumanTaskStatus.PENDING : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
