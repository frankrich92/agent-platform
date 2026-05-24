package com.htam.agent.workflow;

public record WorkflowWorkerSelection(
        WorkflowWorkerPlan plan,
        String reason) {
}
