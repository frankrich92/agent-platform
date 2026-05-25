package com.htam.agent.repo.migration;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryMigrationHistory implements MigrationHistory {

    private final CopyOnWriteArrayList<MigrationStep> applied = new CopyOnWriteArrayList<>();

    @Override
    public Optional<String> currentVersion() {
        return applied.stream()
                .map(MigrationStep::version)
                .max(Comparator.naturalOrder());
    }

    @Override
    public void recordApplied(MigrationStep step) {
        applied.removeIf(existing -> existing.version().equals(step.version()));
        applied.add(step);
    }

    @Override
    public List<MigrationStep> appliedSteps() {
        return applied.stream()
                .sorted(Comparator.comparing(MigrationStep::version))
                .toList();
    }
}
