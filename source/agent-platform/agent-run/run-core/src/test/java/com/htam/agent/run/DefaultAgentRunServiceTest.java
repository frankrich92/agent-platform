package com.htam.agent.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityPlanService;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.run.event.InMemoryRuntimeEventLedger;
import com.htam.agent.run.step.InMemoryRunStepLedger;
import com.htam.agent.run.toolcall.InMemoryToolCallLedger;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.AgentRuntimeRunner;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultAgentRunServiceTest {

    @Test
    void runRecordsRuntimeEventsStepsAndToolCalls() {
        InMemoryRuntimeEventLedger eventLedger = new InMemoryRuntimeEventLedger();
        InMemoryRunStepLedger stepLedger = new InMemoryRunStepLedger();
        InMemoryToolCallLedger toolCallLedger = new InMemoryToolCallLedger();
        AgentRunLedgerRecorder recorder = new AgentRunLedgerRecorder(
                List.of(eventLedger),
                List.of(stepLedger),
                List.of(toolCallLedger));
        CapabilityPlanService capabilityPlanService = agentId -> new CapabilityPlan(
                "plan-" + agentId,
                agentId,
                "model-policy",
                CapabilityRiskPolicy.ASK,
                List.of(new CapabilityItem(
                        CapabilityKind.TOOL,
                        "tool-1",
                        "search",
                        "tool:read",
                        true,
                        true,
                        CapabilityRiskLevel.LOW,
                        CapabilityRiskPolicy.ALLOW,
                        null,
                        List.of(),
                        List.of(),
                        Map.of())));
        AgentRuntimeRunner runtimeRunner = request -> runtimeResult(request);

        DefaultAgentRunService service = new DefaultAgentRunService(
                runtimeRunner,
                capabilityPlanService,
                null,
                recorder);

        AgentRunSummary summary = service.run(AgentRunCommand.backgroundRun(7L, "invoke tool"));

        assertEquals(7L, summary.agentId());
        assertEquals("plan-7", summary.capabilityPlan().planId());
        assertEquals(AgentRunStatus.SUCCEEDED, summary.runtimeResult().status());
        assertEquals(5, eventLedger.listByRunId(summary.runId()).size());
        assertTrue(eventLedger.listByRunId(summary.runId()).stream()
                .anyMatch(event -> event.eventType() == RuntimeEventType.TOOL_CALL_COMPLETED));
        assertTrue(eventLedger.listByRunId(summary.runId()).stream()
                .anyMatch(event -> "mcp-git".equals(event.payload().get("mcpServer"))));
        assertTrue(eventLedger.listByRunId(summary.runId()).stream()
                .anyMatch(event -> "skill-review".equals(event.payload().get("skillName"))));
        assertEquals(3, stepLedger.listByRunId(summary.runId()).size());
        assertTrue(stepLedger.listByRunId(summary.runId()).stream()
                .anyMatch(step -> step.stepType() == RunStepType.MCP_CALL));
        assertTrue(stepLedger.listByRunId(summary.runId()).stream()
                .anyMatch(step -> step.stepType() == RunStepType.SKILL_LOAD));
        assertEquals(1, toolCallLedger.listByRunId(summary.runId()).size());
        assertEquals("search", toolCallLedger.listByRunId(summary.runId()).getFirst().toolName());
    }

    @Test
    void runAddsFailureRecoverySignalForFailedRuntimeResult() {
        InMemoryRuntimeEventLedger eventLedger = new InMemoryRuntimeEventLedger();
        AgentRunLedgerRecorder recorder = new AgentRunLedgerRecorder(
                List.of(eventLedger),
                List.of(),
                List.of());
        CapabilityPlanService capabilityPlanService = agentId -> new CapabilityPlan(
                "plan-" + agentId,
                agentId,
                "model-policy",
                CapabilityRiskPolicy.ASK,
                List.of());
        AgentRuntimeRunner runtimeRunner = request -> new AgentRunResult(
                request.agentId(),
                request.runId(),
                AgentRunStatus.FAILED,
                "tool approval required",
                List.of(),
                List.of(),
                List.of(new ToolCall(
                        "tool-call-approval",
                        request.runId(),
                        "deploy",
                        ToolCallPolicy.ASK,
                        false,
                        Duration.ofMillis(1),
                        0,
                        "{}",
                        null,
                        "APPROVAL_REQUIRED",
                        "approval required",
                        Map.of())));

        DefaultAgentRunService service = new DefaultAgentRunService(
                runtimeRunner,
                capabilityPlanService,
                null,
                recorder);

        AgentRunSummary summary = service.run(AgentRunCommand.backgroundRun(9L, "deploy"));

        assertEquals(AgentRunStatus.FAILED, summary.runtimeResult().status());
        assertTrue(eventLedger.listByRunId(summary.runId()).stream()
                .anyMatch(event -> event.eventType() == RuntimeEventType.FAILURE_RECOVERY_PLANNED
                        && "REQUEST_APPROVAL".equals(event.payload().get("action"))));
    }

    private static AgentRunResult runtimeResult(AgentRunRequest request) {
        assertEquals("plan-7", request.capabilityPlanId());
        assertEquals("plan-7", request.metadata().get("capabilityPlanId"));
        assertEquals("runtime-context-" + request.runId(), request.metadata().get("runtimeContextPlanId"));
        assertTrue((Integer) request.metadata().get("runtimeContextSegmentCount") >= 2);
        RuntimeEvent toolEvent = new RuntimeEvent(
                "event-1",
                RuntimeEventType.TOOL_CALL_COMPLETED,
                request.runId(),
                request.threadId(),
                "step-1",
                request.runId(),
                2,
                Instant.now(),
                Map.of("toolName", "search"));
        RuntimeEvent mcpEvent = new RuntimeEvent(
                "event-2",
                RuntimeEventType.STEP_COMPLETED,
                request.runId(),
                request.threadId(),
                "step-2",
                request.runId(),
                3,
                Instant.now(),
                Map.of("mcpServer", "mcp-git"));
        RuntimeEvent skillEvent = new RuntimeEvent(
                "event-3",
                RuntimeEventType.STEP_COMPLETED,
                request.runId(),
                request.threadId(),
                "step-3",
                request.runId(),
                4,
                Instant.now(),
                Map.of("skillName", "skill-review"));
        RunStep toolStep = new RunStep(
                "step-1",
                request.runId(),
                RunStepType.TOOL_CALL,
                AgentRunStatus.SUCCEEDED,
                Instant.now(),
                Instant.now(),
                Map.of("toolName", "search"));
        RunStep mcpStep = new RunStep(
                "step-2",
                request.runId(),
                RunStepType.MCP_CALL,
                AgentRunStatus.SUCCEEDED,
                Instant.now(),
                Instant.now(),
                Map.of("mcpServer", "mcp-git"));
        RunStep skillStep = new RunStep(
                "step-3",
                request.runId(),
                RunStepType.SKILL_LOAD,
                AgentRunStatus.SUCCEEDED,
                Instant.now(),
                Instant.now(),
                Map.of("skillName", "skill-review"));
        ToolCall toolCall = new ToolCall(
                "tool-call-1",
                request.runId(),
                "search",
                ToolCallPolicy.ALLOW,
                true,
                Duration.ofMillis(12),
                0,
                "query=invoke tool",
                "found",
                null,
                null,
                Map.of("capability", "tool"));
        return new AgentRunResult(
                request.agentId(),
                request.runId(),
                AgentRunStatus.SUCCEEDED,
                "done",
                List.of(toolEvent, mcpEvent, skillEvent),
                List.of(toolStep, mcpStep, skillStep),
                List.of(toolCall));
    }
}
