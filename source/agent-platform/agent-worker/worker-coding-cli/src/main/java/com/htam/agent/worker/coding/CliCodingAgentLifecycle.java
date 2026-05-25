package com.htam.agent.worker.coding;

import com.htam.agent.worker.spi.WorkerRuntime;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CliCodingAgentLifecycle {

    private final CliCodingAgentPlanner planner;
    private final WorkerRuntime workerRuntime;
    private final ConcurrentMap<String, CliCodingAgentSession> sessions = new ConcurrentHashMap<>();

    public CliCodingAgentLifecycle(CliCodingAgentPlanner planner, WorkerRuntime workerRuntime) {
        this.planner = planner == null ? new CliCodingAgentPlanner() : planner;
        this.workerRuntime = workerRuntime;
    }

    public CliCodingAgentSession start(CliCodingAgentSpec spec, String workspaceRef, String instruction) {
        if (workerRuntime == null) {
            throw new IllegalStateException("workerRuntime 未配置");
        }
        WorkerTask task = planner.plan(spec, workspaceRef, instruction);
        WorkerTaskResult result = workerRuntime.submit(task);
        CliCodingAgentSession session = new CliCodingAgentSession(
                UUID.randomUUID().toString(),
                spec,
                task,
                result.status(),
                Instant.now(),
                result);
        sessions.put(session.sessionId(), session);
        return session;
    }

    public CliCodingAgentSession recordResult(String sessionId, WorkerTaskResult result) {
        return sessions.compute(sessionId, (id, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("CliCodingAgentSession 不存在: " + sessionId);
            }
            return current.withResult(result);
        });
    }

    public Optional<CliCodingAgentSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
