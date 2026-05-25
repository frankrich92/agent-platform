package com.htam.agent.workflow.event;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryWorkflowEventSink implements WorkflowEventSink {

    private final CopyOnWriteArrayList<WorkflowEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public WorkflowEvent publish(WorkflowEvent event) {
        events.add(event);
        return event;
    }

    @Override
    public List<WorkflowEvent> listByRunId(String runId) {
        return events.stream()
                .filter(event -> runId == null || runId.equals(event.runId()))
                .sorted(Comparator.comparing(WorkflowEvent::occurredAt))
                .toList();
    }
}
