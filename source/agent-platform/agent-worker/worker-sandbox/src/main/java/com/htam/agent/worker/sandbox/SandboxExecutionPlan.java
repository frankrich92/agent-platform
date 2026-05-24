package com.htam.agent.worker.sandbox;

import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.util.Map;

public record SandboxExecutionPlan(
        WorkerTask task,
        WorkerSandboxPolicy sandboxPolicy,
        WorkerTaskStatus status,
        String isolatedWorkspaceRef,
        String denialReason,
        Map<String, Object> environment) {

    public SandboxExecutionPlan {
        if (task == null) {
            throw new IllegalArgumentException("task 不能为空");
        }
        sandboxPolicy = sandboxPolicy == null ? WorkerSandboxPolicy.lockedDown() : sandboxPolicy;
        status = status == null ? WorkerTaskStatus.ACCEPTED : status;
        isolatedWorkspaceRef = isolatedWorkspaceRef == null || isolatedWorkspaceRef.isBlank()
                ? "sandbox:" + task.taskId()
                : isolatedWorkspaceRef;
        environment = environment == null ? Map.of() : Map.copyOf(environment);
    }
}
