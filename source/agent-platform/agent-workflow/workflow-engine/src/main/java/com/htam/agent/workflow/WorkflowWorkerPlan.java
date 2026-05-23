package com.htam.agent.workflow;

import java.util.Map;

public record WorkflowWorkerPlan(
        String planId,
        WorkflowWorkerType workerType,
        boolean humanInterventionEnabled,
        boolean longRunningRecoveryEnabled,
        Map<String, Object> config) {

    public WorkflowWorkerPlan {
        workerType = workerType == null ? WorkflowWorkerType.INTERNAL : workerType;
        config = config == null ? Map.of() : Map.copyOf(config);
    }

    public static WorkflowWorkerPlan internalDefault(String planId) {
        return new WorkflowWorkerPlan(planId, WorkflowWorkerType.INTERNAL, true, true, Map.of());
    }
}
