package com.htam.agent.runtime.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.runtime.RunStepType;
import com.htam.agent.runtime.ToolCallPolicy;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuntimeInteractionRecorderTest {

    @Test
    void recordsToolMcpAndSkillInteractionsInsideTrace() {
        RuntimeInteractionRecorder.withinTrace("run-1", "session-1", "trace-1", 10, () -> {
            RuntimeInteractionRecorder.recordSkillLoad("skill-review", "skill:1");
            RuntimeInteractionRecorder.recordToolCall("search", ToolCallPolicy.ALLOW, true,
                    Instant.now(), Instant.now(), "q=a", "ok", null, null, Map.of());
            RuntimeInteractionRecorder.recordMcpCall("mcp-git", "git-status",
                    Instant.now(), Instant.now(), "{}", "clean", null);
            RuntimeInteractionTrace trace = RuntimeInteractionRecorder.currentTrace();

            assertEquals(3, trace.events().size());
            assertEquals(3, trace.steps().size());
            assertEquals(2, trace.toolCalls().size());
            assertTrue(trace.steps().stream().anyMatch(step -> step.stepType() == RunStepType.SKILL_LOAD));
            assertTrue(trace.steps().stream().anyMatch(step -> step.stepType() == RunStepType.MCP_CALL));
            return null;
        });
    }
}
