package com.htam.agent.run;

import com.htam.agent.run.session.service.ChatSessionService;
import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityPlanService;
import com.htam.agent.common.dto.ChatMessageAppendDTO;
import com.htam.agent.common.vo.ChatMessageVO;
import com.htam.agent.governance.observability.AgentTraceContext;
import com.htam.agent.governance.observability.TraceContextPlanner;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRuntimeRunner;
import com.htam.agent.runtime.memory.ContextSegmentKind;
import com.htam.agent.runtime.memory.DefaultRuntimeContextPlanner;
import com.htam.agent.runtime.memory.MemoryWritePolicy;
import com.htam.agent.runtime.memory.RuntimeContextPlan;
import com.htam.agent.runtime.memory.RuntimeContextPlanner;
import com.htam.agent.runtime.memory.RuntimeContextSegment;
import com.htam.agent.runtime.memory.TokenBudget;
import com.htam.agent.runtime.planning.DoomLoopDecision;
import com.htam.agent.runtime.planning.DoomLoopDetector;
import com.htam.agent.runtime.planning.FailureRecoveryPlan;
import com.htam.agent.runtime.planning.FailureRecoveryPlanner;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DefaultAgentRunService implements AgentRunService {

    private final AgentRuntimeRunner runtimeRunner;
    private final CapabilityPlanService capabilityPlanService;
    private final ChatSessionService chatSessionService;
    private final AgentRunLedgerRecorder ledgerRecorder;
    private final TraceContextPlanner traceContextPlanner;
    private final RuntimeContextPlanner runtimeContextPlanner;
    private final DoomLoopDetector doomLoopDetector;
    private final FailureRecoveryPlanner failureRecoveryPlanner;

    public DefaultAgentRunService(
            AgentRuntimeRunner runtimeRunner,
            CapabilityPlanService capabilityPlanService,
            ChatSessionService chatSessionService,
            AgentRunLedgerRecorder ledgerRecorder) {
        this(runtimeRunner, capabilityPlanService, chatSessionService, ledgerRecorder,
                new TraceContextPlanner(), new DefaultRuntimeContextPlanner(),
                new DoomLoopDetector(), new FailureRecoveryPlanner());
    }

    @Autowired
    public DefaultAgentRunService(
            AgentRuntimeRunner runtimeRunner,
            CapabilityPlanService capabilityPlanService,
            ChatSessionService chatSessionService,
            AgentRunLedgerRecorder ledgerRecorder,
            TraceContextPlanner traceContextPlanner,
            RuntimeContextPlanner runtimeContextPlanner,
            DoomLoopDetector doomLoopDetector,
            FailureRecoveryPlanner failureRecoveryPlanner) {
        this.runtimeRunner = runtimeRunner;
        this.capabilityPlanService = capabilityPlanService;
        this.chatSessionService = chatSessionService;
        this.ledgerRecorder = ledgerRecorder;
        this.traceContextPlanner = traceContextPlanner == null ? new TraceContextPlanner() : traceContextPlanner;
        this.runtimeContextPlanner = runtimeContextPlanner == null
                ? new DefaultRuntimeContextPlanner()
                : runtimeContextPlanner;
        this.doomLoopDetector = doomLoopDetector == null ? new DoomLoopDetector() : doomLoopDetector;
        this.failureRecoveryPlanner = failureRecoveryPlanner == null
                ? new FailureRecoveryPlanner()
                : failureRecoveryPlanner;
    }

    @Override
    public AgentRunSummary run(AgentRunCommand command) {
        String runId = command.runId() == null || command.runId().isBlank()
                ? UUID.randomUUID().toString()
                : command.runId();
        CapabilityPlan capabilityPlan = capabilityPlanService.resolvePlan(command.agentId());
        AgentTraceContext traceContext = traceContextPlanner.plan(
                runId,
                command.sessionId(),
                command.agentId(),
                null,
                null,
                Map.of("source", "agent-run-service"));
        RuntimeContextPlan contextPlan = runtimeContextPlanner.plan(
                runId,
                tokenBudget(capabilityPlan),
                memoryWritePolicy(capabilityPlan),
                runtimeContextSegments(command, capabilityPlan));

        ChatMessageVO userMessage = null;
        if (command.recordMessages() && command.sessionId() != null) {
            userMessage = appendMessage(command.sessionId(), "user", command.input());
        }

        ledgerRecorder.runStarted(
                runId,
                command,
                capabilityPlan,
                userMessage == null ? null : userMessage.getId(),
                traceContext.traceId(),
                runtimeContextMetadata(contextPlan));

        AgentRunResult runtimeResult;
        try {
            runtimeResult = runtimeRunner.run(new AgentRunRequest(
                    command.agentId(),
                    command.input(),
                    command.sessionId() == null ? null : String.valueOf(command.sessionId()),
                    runId,
                    capabilityPlan.planId(),
                    runtimeMetadata(traceContext, capabilityPlan, contextPlan)));
            runtimeResult = withPlanningSignals(runtimeResult, command, capabilityPlan, traceContext);
        } catch (RuntimeException e) {
            ledgerRecorder.runFailed(runId, command.sessionId(), command.agentId(), e, traceContext.traceId());
            throw e;
        }

        ChatMessageVO assistantMessage = null;
        if (command.recordMessages() && command.sessionId() != null && runtimeResult.message() != null) {
            assistantMessage = appendMessage(command.sessionId(), "assistant", runtimeResult.message());
        }
        ledgerRecorder.runFinished(
                runId,
                command.sessionId(),
                runtimeResult,
                userMessage == null ? null : userMessage.getId(),
                assistantMessage == null ? null : assistantMessage.getId(),
                traceContext.traceId());

        return new AgentRunSummary(
                runId,
                command.agentId(),
                command.sessionId(),
                runtimeResult,
                capabilityPlan,
                userMessage == null ? null : userMessage.getId(),
                assistantMessage == null ? null : assistantMessage.getId());
    }

    private ChatMessageVO appendMessage(Long sessionId, String role, String content) {
        ChatMessageAppendDTO dto = new ChatMessageAppendDTO();
        dto.setRole(role);
        dto.setContent(content);
        return chatSessionService.appendMessage(sessionId, dto);
    }

    private Map<String, Object> runtimeMetadata(
            AgentTraceContext traceContext,
            CapabilityPlan capabilityPlan,
            RuntimeContextPlan contextPlan) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("traceId", traceContext.traceId());
        if (traceContext.parentTraceId() != null) {
            metadata.put("parentTraceId", traceContext.parentTraceId());
        }
        metadata.put("capabilityPlanId", capabilityPlan.planId());
        metadata.put("capabilityPlanItemCount", capabilityPlan.items().size());
        metadata.put("capabilityPlanEnabledItemCount", capabilityPlan.enabledItems().size());
        metadata.put("capabilityPlanExecutableItemCount", capabilityPlan.executableItems().size());
        metadata.put("capabilityPlanHighRiskDefaultPolicy", capabilityPlan.highRiskDefaultPolicy().name());
        metadata.putAll(runtimeContextMetadata(contextPlan));
        return metadata;
    }

    private AgentRunResult withPlanningSignals(
            AgentRunResult result,
            AgentRunCommand command,
            CapabilityPlan capabilityPlan,
            AgentTraceContext traceContext) {
        if (result == null) {
            return null;
        }
        List<RuntimeEvent> signals = new ArrayList<>();
        long nextSequence = nextSequence(result.events());
        DoomLoopDecision doomLoopDecision = doomLoopDetector.detect(
                result.steps(),
                result.toolCalls(),
                maxSteps(capabilityPlan),
                5,
                3);
        if (doomLoopDecision.detected()) {
            signals.add(signalEvent(
                    RuntimeEventType.DOOM_LOOP_DETECTED,
                    result.runId(),
                    command.sessionId(),
                    traceContext.traceId(),
                    nextSequence++,
                    Map.of(
                            "humanTakeoverRequired", doomLoopDecision.humanTakeoverRequired(),
                            "reasons", doomLoopDecision.reasons())));
        }
        if (!result.success()) {
            FailureRecoveryPlan recoveryPlan = failureRecoveryPlanner.plan(
                    new IllegalStateException(result.message() == null ? "runtime failed" : result.message()),
                    result.toolCalls(),
                    capabilityPlan.highRiskDefaultPolicy() == com.htam.agent.capability.CapabilityRiskPolicy.ASK);
            signals.add(signalEvent(
                    RuntimeEventType.FAILURE_RECOVERY_PLANNED,
                    result.runId(),
                    command.sessionId(),
                    traceContext.traceId(),
                    nextSequence,
                    Map.of(
                            "action", recoveryPlan.action().name(),
                            "retryAfterMillis", recoveryPlan.retryAfterMillis(),
                            "reasons", recoveryPlan.reasons())));
        }
        if (signals.isEmpty()) {
            return result;
        }
        List<RuntimeEvent> events = new ArrayList<>(result.events());
        events.addAll(signals);
        return new AgentRunResult(
                result.agentId(),
                result.runId(),
                result.status(),
                result.message(),
                events,
                result.steps(),
                result.toolCalls());
    }

    private static RuntimeEvent signalEvent(
            RuntimeEventType type,
            String runId,
            Long sessionId,
            String traceId,
            long sequence,
            Map<String, Object> payload) {
        return new RuntimeEvent(
                UUID.randomUUID().toString(),
                type,
                runId,
                sessionId == null ? null : String.valueOf(sessionId),
                null,
                traceId,
                sequence,
                Instant.now(),
                payload);
    }

    private static long nextSequence(List<RuntimeEvent> events) {
        return events == null || events.isEmpty()
                ? 2
                : events.stream().mapToLong(RuntimeEvent::sequence).max().orElse(1) + 1;
    }

    private static TokenBudget tokenBudget(CapabilityPlan capabilityPlan) {
        return new TokenBudget(8192, 1024, memoryEnabled(capabilityPlan) ? 1024 : 0, 1024);
    }

    private static MemoryWritePolicy memoryWritePolicy(CapabilityPlan capabilityPlan) {
        return new MemoryWritePolicy(memoryEnabled(capabilityPlan), true, 4000);
    }

    private static List<RuntimeContextSegment> runtimeContextSegments(
            AgentRunCommand command,
            CapabilityPlan capabilityPlan) {
        Map<ContextSegmentKind, Long> capabilityCounts = capabilityPlan.items().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> switch (item.kind()) {
                            case SKILL -> ContextSegmentKind.RUNTIME_HINT;
                            case KNOWLEDGE -> ContextSegmentKind.LONG_TERM_MEMORY;
                            default -> ContextSegmentKind.RUNTIME_HINT;
                        },
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()));
        List<RuntimeContextSegment> segments = new java.util.ArrayList<>();
        segments.add(new RuntimeContextSegment(
                "input",
                ContextSegmentKind.USER_INPUT,
                "run-input",
                estimateTokens(command.input()),
                false,
                Map.of("source", "agent-run-command")));
        if (command.sessionId() != null) {
            segments.add(new RuntimeContextSegment(
                    "session-history",
                    ContextSegmentKind.SESSION_HISTORY,
                    "chat_session:" + command.sessionId(),
                    0,
                    true,
                    Map.of("source", "chat-message-tree")));
        }
        if (memoryEnabled(capabilityPlan)) {
            segments.add(new RuntimeContextSegment(
                    "long-term-memory",
                    ContextSegmentKind.LONG_TERM_MEMORY,
                    "memory:agent:" + command.agentId(),
                    0,
                    true,
                    Map.of("source", "runtime-memory")));
        }
        segments.add(new RuntimeContextSegment(
                "capability-plan",
                ContextSegmentKind.RUNTIME_HINT,
                capabilityPlan.planId(),
                Math.max(1, capabilityPlan.items().size()) * 16,
                false,
                Map.of(
                        "itemCount", capabilityPlan.items().size(),
                        "enabledItemCount", capabilityPlan.enabledItems().size(),
                        "kindCounts", capabilityCounts.entrySet().stream()
                                .collect(java.util.stream.Collectors.toMap(
                                        entry -> entry.getKey().name(),
                                        Map.Entry::getValue,
                                        (left, right) -> left,
                                        LinkedHashMap::new)))));
        return segments;
    }

    private static Map<String, Object> runtimeContextMetadata(RuntimeContextPlan contextPlan) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("runtimeContextPlanId", contextPlan.planId());
        metadata.put("runtimeContextTotalTokens", contextPlan.totalTokens());
        metadata.put("runtimeContextBudgetTokens", contextPlan.tokenBudget().contextTokens());
        metadata.put("runtimeContextCompressionRequired", contextPlan.compressionRequired());
        metadata.put("runtimeContextCompressionStatus", contextPlan.status().name());
        metadata.put("runtimeContextMemoryFlushRequired", contextPlan.memoryFlushRequired());
        metadata.put("runtimeContextSegmentCount", contextPlan.segments().size());
        metadata.put("runtimeContextCompressionSpanCount", contextPlan.compressionSpans().size());
        if (contextPlan.fallbackReason() != null && !contextPlan.fallbackReason().isBlank()) {
            metadata.put("runtimeContextFallbackReason", contextPlan.fallbackReason());
        }
        metadata.put("runtimeContextSegmentKinds", contextPlan.segments().stream()
                .map(segment -> segment.kind().name())
                .distinct()
                .toList());
        return metadata;
    }

    private static boolean memoryEnabled(CapabilityPlan capabilityPlan) {
        return capabilityPlan.items().stream()
                .filter(item -> item.kind() == com.htam.agent.capability.CapabilityKind.MODEL_POLICY)
                .map(item -> item.attributes().get("enableMemory"))
                .anyMatch(Boolean.TRUE::equals);
    }

    private static int maxSteps(CapabilityPlan capabilityPlan) {
        return capabilityPlan.items().stream()
                .filter(item -> item.kind() == com.htam.agent.capability.CapabilityKind.MODEL_POLICY)
                .map(item -> item.attributes().get("maxIterations"))
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast)
                .filter(value -> value > 0)
                .findFirst()
                .orElse(50);
    }

    private static int estimateTokens(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(content.length() / 4.0));
    }
}
