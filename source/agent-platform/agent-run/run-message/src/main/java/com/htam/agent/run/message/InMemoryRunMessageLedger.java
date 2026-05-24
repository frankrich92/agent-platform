package com.htam.agent.run.message;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryRunMessageLedger implements RunMessageLedger {

    private final CopyOnWriteArrayList<RunMessageRecord> messages = new CopyOnWriteArrayList<>();

    @Override
    public RunMessageRecord append(RunMessageRecord message) {
        messages.add(message);
        return message;
    }

    @Override
    public List<RunMessageRecord> listBySessionId(String sessionId) {
        return messages.stream()
                .filter(message -> sessionId == null || sessionId.equals(message.sessionId()))
                .sorted(Comparator.comparing(RunMessageRecord::createdAt))
                .toList();
    }

    @Override
    public List<RunMessageRecord> listByRunId(String runId) {
        return messages.stream()
                .filter(message -> runId == null || runId.equals(message.runId()))
                .sorted(Comparator.comparing(RunMessageRecord::createdAt))
                .toList();
    }
}
