package com.htam.agent.run.replay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.run.event.InMemoryRuntimeEventLedger;
import com.htam.agent.run.history.LedgerRunHistoryReader;
import com.htam.agent.run.message.InMemoryRunMessageLedger;
import com.htam.agent.run.message.RunMessageRecord;
import com.htam.agent.run.message.RunMessageRole;
import com.htam.agent.run.step.InMemoryRunStepLedger;
import com.htam.agent.run.toolcall.InMemoryToolCallLedger;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RunReplayServiceTest {

    @Test
    void createsReplayFramesFromRunLedgers() {
        InMemoryRuntimeEventLedger eventLedger = new InMemoryRuntimeEventLedger();
        InMemoryRunStepLedger stepLedger = new InMemoryRunStepLedger();
        InMemoryToolCallLedger toolCallLedger = new InMemoryToolCallLedger();
        InMemoryRunMessageLedger messageLedger = new InMemoryRunMessageLedger();

        eventLedger.append(new RuntimeEvent("event-1", RuntimeEventType.RUN_STARTED, "run-1",
                "session-1", null, "trace-1", 1, Instant.now(), Map.of("agentId", 7L)));
        stepLedger.append(new RunStep("step-1", "run-1", RunStepType.MCP_CALL,
                AgentRunStatus.SUCCEEDED, Instant.now(), Instant.now(), Map.of("mcpServer", "git")));
        toolCallLedger.append(new ToolCall("tool-1", "run-1", "search",
                ToolCallPolicy.ALLOW, true, Duration.ofMillis(3), 0,
                "q=test", "ok", null, null, Map.of()));
        messageLedger.append(new RunMessageRecord("msg-1", "session-1", "run-1",
                RunMessageRole.ASSISTANT, "done", Instant.now(), Map.of()));

        RunReplayPlan replay = new RunReplayService(new LedgerRunHistoryReader(
                eventLedger, stepLedger, toolCallLedger, messageLedger)).createReplay("run-1");

        assertEquals("run-1", replay.runId());
        assertEquals(4, replay.frames().size());
        assertTrue(replay.frames().stream().anyMatch(frame -> frame.frameType().equals("toolCall")));
        assertTrue(replay.frames().stream().anyMatch(frame -> frame.frameType().equals("step:MCP_CALL")));
    }
}
