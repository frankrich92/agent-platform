package com.htam.agent.repo.migration;

import java.util.Comparator;
import java.util.List;

public record MigrationPlan(List<MigrationStep> steps) {

    public MigrationPlan {
        steps = steps == null ? List.of() : steps.stream()
                .sorted(Comparator.comparing(MigrationStep::version))
                .toList();
    }

    public List<MigrationStep> pendingAfter(String currentVersion) {
        return steps.stream()
                .filter(step -> currentVersion == null || step.version().compareTo(currentVersion) > 0)
                .toList();
    }
}
