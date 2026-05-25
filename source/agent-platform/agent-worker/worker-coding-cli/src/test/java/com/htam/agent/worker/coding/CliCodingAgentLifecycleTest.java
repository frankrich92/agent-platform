package com.htam.agent.worker.coding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.worker.sandbox.WorkerSandboxPolicy;
import com.htam.agent.worker.spi.WorkerTaskResult;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CliCodingAgentLifecycleTest {

    @Test
    void startsCliWorkerSessionThroughWorkerRuntime() {
        CliCodingAgentLifecycle lifecycle = new CliCodingAgentLifecycle(
                new CliCodingAgentPlanner(),
                task -> new WorkerTaskResult(task.taskId(), WorkerTaskStatus.ACCEPTED,
                        Duration.ZERO, "accepted", null, null, Map.of()));
        CliCodingAgentSpec spec = new CliCodingAgentSpec(
                "codex",
                "Codex CLI",
                "codex",
                java.util.List.of("code-edit"),
                WorkerSandboxPolicy.lockedDown());

        CliCodingAgentSession session = lifecycle.start(spec, "workspace-1", "fix tests");

        assertEquals(WorkerTaskStatus.ACCEPTED, session.status());
        assertTrue(lifecycle.findById(session.sessionId()).isPresent());
    }
}
