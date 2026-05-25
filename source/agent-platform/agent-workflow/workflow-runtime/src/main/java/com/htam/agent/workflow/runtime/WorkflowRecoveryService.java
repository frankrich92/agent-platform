package com.htam.agent.workflow.runtime;

import java.util.List;

public class WorkflowRecoveryService {

    private final WorkflowStateRepository stateRepository;

    public WorkflowRecoveryService(WorkflowStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public List<WorkflowRun> recoverRunningRuns() {
        if (stateRepository == null) {
            return List.of();
        }
        return stateRepository.listRunsByStatus(WorkflowRunStatus.RUNNING);
    }
}
