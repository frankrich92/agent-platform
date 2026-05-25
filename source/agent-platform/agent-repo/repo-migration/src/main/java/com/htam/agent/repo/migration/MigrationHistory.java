package com.htam.agent.repo.migration;

import java.util.List;
import java.util.Optional;

public interface MigrationHistory {

    Optional<String> currentVersion();

    void recordApplied(MigrationStep step);

    List<MigrationStep> appliedSteps();
}
