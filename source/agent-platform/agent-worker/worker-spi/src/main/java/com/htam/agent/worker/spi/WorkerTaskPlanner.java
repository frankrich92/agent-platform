package com.htam.agent.worker.spi;

import java.util.Map;
import java.util.UUID;

public class WorkerTaskPlanner {

    public WorkerTask plan(
            WorkerTaskType taskType,
            String workspaceRef,
            String command,
            WorkerRiskPolicy defaultRiskPolicy,
            Map<String, Object> input) {
        WorkerTaskType type = taskType == null ? WorkerTaskType.SHELL : taskType;
        WorkerRiskPolicy policy = riskPolicy(type, defaultRiskPolicy);
        return new WorkerTask(UUID.randomUUID().toString(), type, workspaceRef, command, policy, input);
    }

    private static WorkerRiskPolicy riskPolicy(WorkerTaskType taskType, WorkerRiskPolicy defaultRiskPolicy) {
        if (defaultRiskPolicy == WorkerRiskPolicy.DENY) {
            return WorkerRiskPolicy.DENY;
        }
        return switch (taskType) {
            case FILE_WRITE, SHELL, PYTHON, NODE, BUILD_TEST, BROWSER_AUTOMATION, CLI_CODING_AGENT ->
                    defaultRiskPolicy == WorkerRiskPolicy.ALLOW ? WorkerRiskPolicy.ASK : defaultRiskPolicy;
        };
    }
}
