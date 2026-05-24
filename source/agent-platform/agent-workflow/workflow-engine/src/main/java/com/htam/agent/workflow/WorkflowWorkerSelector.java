package com.htam.agent.workflow;

public class WorkflowWorkerSelector {

    public WorkflowWorkerSelection select(
            String planId,
            boolean complexLongRunningFlow,
            boolean langGraphAvailable,
            String workerRef) {
        if (complexLongRunningFlow && langGraphAvailable) {
            return new WorkflowWorkerSelection(
                    WorkflowWorkerPlan.langGraphWorker(planId, workerRef),
                    "complex long-running workflow");
        }
        return new WorkflowWorkerSelection(
                WorkflowWorkerPlan.internalDefault(planId),
                "internal workflow is sufficient");
    }
}
