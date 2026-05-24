package com.htam.agent.run.session;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryRunSessionLedger implements RunSessionLedger {

    private final ConcurrentMap<String, RunSessionRecord> sessions = new ConcurrentHashMap<>();

    @Override
    public RunSessionRecord save(RunSessionRecord session) {
        sessions.put(session.sessionId(), session);
        return session;
    }

    @Override
    public Optional<RunSessionRecord> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<RunSessionRecord> listByAgentId(Long agentId) {
        return sessions.values().stream()
                .filter(session -> agentId == null || agentId.equals(session.agentId()))
                .sorted(Comparator.comparing(RunSessionRecord::updatedAt).reversed())
                .toList();
    }
}
