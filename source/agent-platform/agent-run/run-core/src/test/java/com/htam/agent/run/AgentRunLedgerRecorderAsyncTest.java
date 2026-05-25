package com.htam.agent.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.run.event.InMemoryRuntimeEventLedger;
import com.htam.agent.run.step.InMemoryRunStepLedger;
import com.htam.agent.run.toolcall.InMemoryToolCallLedger;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AgentRunLedgerRecorderAsyncTest {

    @Test
    void asyncQueueFlushMakesSubmittedWritesVisible() {
        DefaultAgentRunLedgerWriteQueue queue = new DefaultAgentRunLedgerWriteQueue(true, 8, 1_000L, false);
        AtomicInteger writes = new AtomicInteger();

        queue.submit("increment", writes::incrementAndGet);
        queue.flush();
        queue.close();

        assertEquals(1, writes.get());
    }

    @Test
    void asyncQueueRetriesFailedWritesBeforeFlushReturns() {
        DefaultAgentRunLedgerWriteQueue queue =
                new DefaultAgentRunLedgerWriteQueue(true, 8, 1_000L, false, 2, 0L);
        AtomicInteger attempts = new AtomicInteger();

        queue.submit("flaky", () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("temporary failure");
            }
        });
        queue.flush();
        queue.close();

        assertEquals(3, attempts.get());
    }

    @Test
    void queueStopsAfterConfiguredRetries() {
        DefaultAgentRunLedgerWriteQueue queue =
                new DefaultAgentRunLedgerWriteQueue(false, 1, 1_000L, false, 1, 0L);
        AtomicInteger attempts = new AtomicInteger();

        queue.submit("always-fails", () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("permanent failure");
        });

        assertEquals(2, attempts.get());
    }

    @Test
    void asyncRecorderPersistsRunArtifactsAndTerminalStatus() {
        InMemoryRuntimeEventLedger eventLedger = new InMemoryRuntimeEventLedger();
        InMemoryRunStepLedger stepLedger = new InMemoryRunStepLedger();
        InMemoryToolCallLedger toolCallLedger = new InMemoryToolCallLedger();
        InMemoryAgentRunLedger runLedger = new InMemoryAgentRunLedger();
        DefaultAgentRunLedgerWriteQueue queue = new DefaultAgentRunLedgerWriteQueue(true, 32, 1_000L, false);
        AgentRunLedgerRecorder recorder = new AgentRunLedgerRecorder(
                List.of(eventLedger),
                List.of(stepLedger),
                List.of(toolCallLedger),
                List.of(runLedger),
                queue);
        String runId = "run-async-1";
        AgentRunCommand command = AgentRunCommand.backgroundRun(12L, "call tool");
        CapabilityPlan capabilityPlan = new CapabilityPlan(
                "plan-12",
                12L,
                "model-policy",
                CapabilityRiskPolicy.ALLOW,
                List.of());

        recorder.runStarted(runId, command, capabilityPlan, 42, "trace-async");
        recorder.runFinished(
                runId,
                null,
                runtimeResult(runId),
                42,
                43,
                "trace-async");
        recorder.flush();
        queue.close();

        AgentRunRecord run = runLedger.findById(runId).orElseThrow();
        assertEquals(AgentRunStatus.SUCCEEDED, run.status());
        assertEquals("done", run.output());
        assertNotNull(run.endedAt());
        assertEquals("call tool", run.metadata().get("title"));
        assertEquals("input", run.metadata().get("titleSource"));
        assertEquals(3, eventLedger.listByRunId(runId).size());
        assertTrue(eventLedger.listByRunId(runId).stream()
                .anyMatch(event -> event.eventType() == RuntimeEventType.RUN_COMPLETED));
        assertEquals(1, stepLedger.listByRunId(runId).size());
        assertEquals(1, toolCallLedger.listByRunId(runId).size());
    }

    private static AgentRunResult runtimeResult(String runId) {
        RuntimeEvent event = new RuntimeEvent(
                "event-async-1",
                RuntimeEventType.TOOL_CALL_COMPLETED,
                runId,
                null,
                "step-async-1",
                "trace-async",
                2,
                Instant.now(),
                Map.of("toolName", "search"));
        RunStep step = new RunStep(
                "step-async-1",
                runId,
                RunStepType.TOOL_CALL,
                AgentRunStatus.SUCCEEDED,
                Instant.now(),
                Instant.now(),
                Map.of("toolName", "search"));
        ToolCall toolCall = new ToolCall(
                "tool-call-async-1",
                runId,
                "search",
                ToolCallPolicy.ALLOW,
                true,
                Duration.ofMillis(5),
                0,
                "query=call tool",
                "found",
                null,
                null,
                Map.of("traceId", "trace-async"));
        return new AgentRunResult(
                12L,
                runId,
                AgentRunStatus.SUCCEEDED,
                "done",
                List.of(event),
                List.of(step),
                List.of(toolCall));
    }

    private static final class InMemoryAgentRunLedger implements AgentRunLedger {

        private final ConcurrentMap<String, AgentRunRecord> runs = new ConcurrentHashMap<>();

        @Override
        public AgentRunRecord save(AgentRunRecord record) {
            runs.put(record.runId(), record);
            return record;
        }

        @Override
        public Optional<AgentRunRecord> findById(String runId) {
            return Optional.ofNullable(runs.get(runId));
        }

        @Override
        public List<AgentRunRecord> listBySessionId(String sessionId) {
            if (sessionId == null || sessionId.isBlank()) {
                return List.of();
            }
            List<AgentRunRecord> records = new ArrayList<>();
            runs.values().stream()
                    .filter(record -> sessionId.equals(record.sessionId()))
                    .sorted(Comparator.comparing(AgentRunRecord::startedAt))
                    .forEach(records::add);
            return List.copyOf(records);
        }
    }
}
