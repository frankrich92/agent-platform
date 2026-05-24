package com.htam.agent.worker.sandbox;

import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import com.htam.agent.worker.spi.WorkerTaskType;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkerSandboxPlanner {

    public SandboxExecutionPlan plan(WorkerTask task, WorkerSandboxPolicy policy) {
        WorkerSandboxPolicy sandboxPolicy = policy == null ? WorkerSandboxPolicy.lockedDown() : policy;
        if (task == null) {
            throw new IllegalArgumentException("task 不能为空");
        }
        String denialReason = denialReason(task, sandboxPolicy);
        WorkerTaskStatus status = denialReason == null
                ? (task.requiresApproval() ? WorkerTaskStatus.APPROVAL_REQUIRED : WorkerTaskStatus.ACCEPTED)
                : WorkerTaskStatus.DENIED;
        Map<String, Object> environment = new LinkedHashMap<>();
        environment.put("networkEnabled", sandboxPolicy.networkEnabled());
        environment.put("fileWriteEnabled", sandboxPolicy.fileWriteEnabled());
        environment.put("timeout", sandboxPolicy.timeout().toString());
        environment.put("allowedCommands", sandboxPolicy.allowedCommands());
        return new SandboxExecutionPlan(task, sandboxPolicy, status, workspaceRef(task), denialReason, environment);
    }

    private static String denialReason(WorkerTask task, WorkerSandboxPolicy policy) {
        if (task.taskType() == WorkerTaskType.FILE_WRITE && !policy.fileWriteEnabled()) {
            return "file write is disabled by sandbox policy";
        }
        if (task.command() != null && !task.command().isBlank()
                && !policy.allowedCommands().contains(task.command())) {
            return "command is not allowed by sandbox policy";
        }
        return null;
    }

    private static String workspaceRef(WorkerTask task) {
        return task.workspaceRef() == null || task.workspaceRef().isBlank()
                ? "sandbox:" + task.taskId()
                : task.workspaceRef();
    }
}
