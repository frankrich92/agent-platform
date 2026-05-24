package com.htam.agent.governance.audit;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryAuditLedger implements AuditLedger {

    private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public AuditEvent append(AuditEvent event) {
        events.add(event);
        return event;
    }

    @Override
    public List<AuditEvent> listByRunId(String runId) {
        return events.stream()
                .filter(event -> runId == null || runId.equals(event.runId()))
                .sorted(Comparator.comparing(AuditEvent::occurredAt))
                .toList();
    }

    @Override
    public List<AuditEvent> listByTraceId(String traceId) {
        return events.stream()
                .filter(event -> traceId == null || traceId.equals(event.traceId()))
                .sorted(Comparator.comparing(AuditEvent::occurredAt))
                .toList();
    }
}
