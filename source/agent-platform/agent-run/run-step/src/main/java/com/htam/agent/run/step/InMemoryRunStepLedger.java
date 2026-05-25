package com.htam.agent.run.step;

import com.htam.agent.runtime.RunStep;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryRunStepLedger implements RunStepLedger {

    private final ConcurrentMap<String, List<RunStep>> stepsByRunId = new ConcurrentHashMap<>();

    @Override
    public void append(RunStep step) {
        if (step == null) {
            return;
        }
        stepsByRunId.compute(step.runId(), (runId, existing) -> {
            List<RunStep> steps = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            steps.add(step);
            steps.sort(Comparator.comparing(RunStep::startedAt, Comparator.nullsLast(Comparator.naturalOrder())));
            return List.copyOf(steps);
        });
    }

    @Override
    public List<RunStep> listByRunId(String runId) {
        if (runId == null || runId.isBlank()) {
            return List.of();
        }
        return stepsByRunId.getOrDefault(runId, List.of());
    }
}
