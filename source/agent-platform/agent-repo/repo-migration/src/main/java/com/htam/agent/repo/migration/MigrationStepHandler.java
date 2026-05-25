package com.htam.agent.repo.migration;

@FunctionalInterface
public interface MigrationStepHandler {

    void apply(MigrationStep step);
}
