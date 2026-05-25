package com.htam.agent.repo.migration;

import java.util.List;

public class MigrationExecutor {

    private final MigrationHistory history;
    private final MigrationStepHandler stepHandler;

    public MigrationExecutor(MigrationHistory history, MigrationStepHandler stepHandler) {
        this.history = history;
        this.stepHandler = stepHandler;
    }

    public List<MigrationStep> applyPending(MigrationPlan plan) {
        String currentVersion = history.currentVersion().orElse(null);
        List<MigrationStep> pending = plan.pendingAfter(currentVersion);
        for (MigrationStep step : pending) {
            stepHandler.apply(step);
            history.recordApplied(step);
        }
        return pending;
    }
}
