package com.htam.agent.repo.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MigrationPlanTest {

    @Test
    void pendingAfterReturnsSortedFutureSteps() {
        MigrationPlan plan = new MigrationPlan(List.of(
                new MigrationStep("2026.02", "second", "db/02.sql"),
                new MigrationStep("2026.01", "first", "db/01.sql")));

        List<MigrationStep> pending = plan.pendingAfter("2026.01");

        assertEquals(1, pending.size());
        assertEquals("2026.02", pending.getFirst().version());
    }

    @Test
    void executorAppliesPendingStepsAndRecordsHistory() {
        MigrationPlan plan = new MigrationPlan(List.of(
                new MigrationStep("2026.01", "first", "db/01.sql"),
                new MigrationStep("2026.02", "second", "db/02.sql")));
        InMemoryMigrationHistory history = new InMemoryMigrationHistory();
        List<String> applied = new ArrayList<>();
        MigrationExecutor executor = new MigrationExecutor(history, step -> applied.add(step.version()));

        List<MigrationStep> firstRun = executor.applyPending(plan);
        List<MigrationStep> secondRun = executor.applyPending(plan);

        assertEquals(List.of("2026.01", "2026.02"), applied);
        assertEquals(2, firstRun.size());
        assertEquals(0, secondRun.size());
        assertEquals("2026.02", history.currentVersion().orElseThrow());
        assertEquals(2, history.appliedSteps().size());
    }
}
