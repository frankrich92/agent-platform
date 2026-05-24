package com.htam.agent.worker.spi;

import java.util.Map;

public record WorkerTask(
        String taskId,
        WorkerTaskType taskType,
        String workspaceRef,
        String command,
        WorkerRiskPolicy riskPolicy,
        Map<String, Object> input) {

    public WorkerTask {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId 不能为空");
        }
        taskType = taskType == null ? WorkerTaskType.SHELL : taskType;
        riskPolicy = riskPolicy == null ? WorkerRiskPolicy.ASK : riskPolicy;
        input = input == null ? Map.of() : Map.copyOf(input);
    }
}
