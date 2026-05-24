package com.htam.agent.runtime.agentscope.runtime;

import com.htam.agent.runtime.agentscope.agent.IAgentFactory;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.AgentRuntimeRunner;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import com.htam.agent.runtime.core.RuntimeInteractionRecorder;
import com.htam.agent.runtime.core.RuntimeInteractionTrace;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.message.Msg;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AgentScope 运行时适配器。
 */
@Component
@RequiredArgsConstructor
public class AgentScopeRuntimeRunner implements AgentRuntimeRunner {

    private final IAgentFactory agentFactory;

    @Override
    public AgentRunResult run(AgentRunRequest request) {
        String runId = request.runId() == null || request.runId().isBlank()
                ? UUID.randomUUID().toString()
                : request.runId();
        String traceId = metadataValue(request, "traceId", runId);
        String stepId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        List<RuntimeEvent> events = new ArrayList<>();
        events.add(event(RuntimeEventType.RUN_STARTED, runId, request.threadId(), null, traceId, 1,
                Map.of(
                        "capabilityPlanId", request.capabilityPlanId() == null ? "" : request.capabilityPlanId(),
                        "capabilityPlanItemCount", metadataValue(request, "capabilityPlanItemCount", "0"),
                        "runtime", "agentscope")));
        events.add(event(RuntimeEventType.STEP_STARTED, runId, request.threadId(), stepId, traceId, 2,
                Map.of("stepType", RunStepType.MODEL_CALL.name(), "runtime", "agentscope")));
        try {
            Agent agent = agentFactory.getAgent(request.agentId());
            RuntimeInteractionTrace trace = RuntimeInteractionRecorder.withinTrace(
                    runId,
                    request.threadId(),
                    traceId,
                    3,
                    () -> {
                        agent.call(Msg.builder()
                                .textContent(request.input())
                                .build()).block();
                        return RuntimeInteractionRecorder.currentTrace();
                    });
            Instant endedAt = Instant.now();
            events.addAll(trace.events());
            events.add(event(RuntimeEventType.STEP_COMPLETED, runId, request.threadId(), stepId, traceId, 3,
                    Map.of("stepType", RunStepType.MODEL_CALL.name(), "status", AgentRunStatus.SUCCEEDED.name())));
            events.add(event(RuntimeEventType.RUN_COMPLETED, runId, request.threadId(), null, traceId, 4,
                    Map.of("status", AgentRunStatus.SUCCEEDED.name())));
            RunStep step = new RunStep(stepId, runId, RunStepType.MODEL_CALL, AgentRunStatus.SUCCEEDED,
                    startedAt, endedAt, Map.of("runtime", "agentscope"));
            return new AgentRunResult(request.agentId(), runId, AgentRunStatus.SUCCEEDED, null,
                    events, mergeSteps(step, trace), trace.toolCalls());
        } catch (RuntimeException ex) {
            Instant endedAt = Instant.now();
            events.add(event(RuntimeEventType.RUN_FAILED, runId, request.threadId(), stepId, traceId, 3,
                    Map.of("status", AgentRunStatus.FAILED.name(), "error", ex.getMessage() == null ? "" : ex.getMessage())));
            RunStep step = new RunStep(stepId, runId, RunStepType.MODEL_CALL, AgentRunStatus.FAILED,
                    startedAt, endedAt, Map.of("runtime", "agentscope", "error", ex.getMessage() == null ? "" : ex.getMessage()));
            return new AgentRunResult(request.agentId(), runId, AgentRunStatus.FAILED, ex.getMessage(),
                    events, List.of(step), List.of());
        }
    }

    private RuntimeEvent event(
            RuntimeEventType eventType,
            String runId,
            String sessionId,
            String stepId,
            String traceId,
            long sequence,
            Map<String, Object> payload) {
        return new RuntimeEvent(UUID.randomUUID().toString(), eventType, runId, sessionId, stepId, traceId,
                sequence, Instant.now(), payload);
    }

    private String metadataValue(AgentRunRequest request, String key, String fallback) {
        Object value = request.metadata().get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private List<RunStep> mergeSteps(RunStep modelStep, RuntimeInteractionTrace trace) {
        List<RunStep> steps = new ArrayList<>();
        steps.add(modelStep);
        steps.addAll(trace.steps());
        return steps;
    }
}
