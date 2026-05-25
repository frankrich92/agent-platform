package com.htam.agent.adapter.agui.run;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityPlanService;
import com.htam.agent.common.dto.ChatMessageAppendDTO;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.governance.observability.AgentTraceContext;
import com.htam.agent.governance.observability.TraceContextPlanner;
import com.htam.agent.repo.agent.AgentDefinitionRepository;
import com.htam.agent.run.AgentRunLedger;
import com.htam.agent.run.AgentRunRecord;
import com.htam.agent.run.event.RuntimeEventSink;
import com.htam.agent.run.session.lock.SessionLockManager;
import com.htam.agent.run.session.service.ChatSessionService;
import com.htam.agent.run.step.RunStepSink;
import com.htam.agent.run.toolcall.ToolCallSink;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.RunStep;
import com.htam.agent.runtime.RunStepType;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import com.htam.agent.runtime.ToolCall;
import com.htam.agent.runtime.ToolCallPolicy;
import io.agentscope.core.agui.event.AguiEvent;
import io.agentscope.core.agui.event.AguiEventType;
import io.agentscope.core.agui.model.AguiMessage;
import io.agentscope.core.agui.model.RunAgentInput;
import io.agentscope.core.agui.observer.AguiRunEventContext;
import io.agentscope.core.agui.observer.AguiRunEventObserver;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class AguiRunLedgerProjector implements AguiRunEventObserver {

    private static final int TOOL_SUMMARY_MAX_CHARS = 16_384;

    private final List<AgentRunLedger> runLedgers;
    private final List<RuntimeEventSink> eventSinks;
    private final List<RunStepSink> stepSinks;
    private final List<ToolCallSink> toolCallSinks;
    private final ChatSessionService chatSessionService;
    private final SessionLockManager sessionLockManager;
    private final AgentDefinitionRepository agentDefinitionRepository;
    private final CapabilityPlanService capabilityPlanService;
    private final TraceContextPlanner traceContextPlanner;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ConcurrentMap<String, ProjectionState> states = new ConcurrentHashMap<>();

    public AguiRunLedgerProjector(
            List<AgentRunLedger> runLedgers,
            List<RuntimeEventSink> eventSinks,
            List<RunStepSink> stepSinks,
            List<ToolCallSink> toolCallSinks,
            ChatSessionService chatSessionService,
            SessionLockManager sessionLockManager,
            AgentDefinitionRepository agentDefinitionRepository,
            CapabilityPlanService capabilityPlanService,
            TraceContextPlanner traceContextPlanner) {
        this.runLedgers = runLedgers == null ? List.of() : List.copyOf(runLedgers);
        this.eventSinks = eventSinks == null ? List.of() : List.copyOf(eventSinks);
        this.stepSinks = stepSinks == null ? List.of() : List.copyOf(stepSinks);
        this.toolCallSinks = toolCallSinks == null ? List.of() : List.copyOf(toolCallSinks);
        this.chatSessionService = chatSessionService;
        this.sessionLockManager = sessionLockManager;
        this.agentDefinitionRepository = agentDefinitionRepository;
        this.capabilityPlanService = capabilityPlanService;
        this.traceContextPlanner = traceContextPlanner == null ? new TraceContextPlanner() : traceContextPlanner;
    }

    @Override
    public void onEvent(AguiRunEventContext context, AguiEvent event) {
        String runId = nonBlank(event.getRunId(), context.runId());
        if (runId == null) {
            return;
        }
        ProjectionState state = states.computeIfAbsent(runId, id -> new ProjectionState(id, context.threadId()));
        state.sessionId = nonBlank(event.getThreadId(), context.threadId(), state.sessionId);
        if (context.input() != null && context.input().getForwardedProps() != null) {
            context.input().getForwardedProps().forEach((key, value) -> {
                if (key != null && value != null) {
                    state.forwardedProps.put(key, value);
                }
            });
        }
        handleEvent(context, state, event);
    }

    private void handleEvent(AguiRunEventContext context, ProjectionState state, AguiEvent event) {
        if (event instanceof AguiEvent.RunStarted) {
            startRun(context, state);
            Map<String, Object> eventPayload = payload(
                    "aguiType", event.getType().name(),
                    "traceId", state.traceId());
            eventPayload.putAll(capabilityMetadata(state.capabilityPlan));
            appendEvent(state, RuntimeEventType.RUN_STARTED, null, eventPayload);
            return;
        }
        if (event instanceof AguiEvent.TextMessageStart textStart) {
            appendEvent(state, RuntimeEventType.MESSAGE_STARTED, null,
                    payload("aguiType", event.getType().name(), "messageId", textStart.messageId(), "role", textStart.role()));
            return;
        }
        if (event instanceof AguiEvent.TextMessageContent textContent) {
            text(state.textByMessageId, textContent.messageId()).append(nullToEmpty(textContent.delta()));
            appendEvent(state, RuntimeEventType.MESSAGE_DELTA, null,
                    payload("aguiType", event.getType().name(), "messageId", textContent.messageId(), "delta", textContent.delta()));
            return;
        }
        if (event instanceof AguiEvent.TextMessageEnd textEnd) {
            StringBuilder textBuilder = state.textByMessageId.remove(textEnd.messageId());
            String text = textBuilder == null ? "" : textBuilder.toString();
            saveAssistantMessage(state, text);
            appendEvent(state, RuntimeEventType.MESSAGE_COMPLETED, null,
                    payload("aguiType", event.getType().name(), "messageId", textEnd.messageId()));
            return;
        }
        if (event instanceof AguiEvent.ReasoningMessageContent reasoningContent) {
            state.reasoning.append(nullToEmpty(reasoningContent.delta()));
            appendEvent(state, RuntimeEventType.MESSAGE_DELTA, null,
                    payload("aguiType", event.getType().name(), "messageId", reasoningContent.messageId(), "delta", reasoningContent.delta()));
            return;
        }
        if (event instanceof AguiEvent.ReasoningMessageEnd reasoningEnd) {
            appendEvent(state, RuntimeEventType.MESSAGE_COMPLETED, null,
                    payload("aguiType", event.getType().name(), "messageId", reasoningEnd.messageId()));
            return;
        }
        if (event instanceof AguiEvent.ToolCallStart toolStart) {
            ToolProjection tool = state.tool(toolStart.toolCallId());
            tool.name = toolStart.toolCallName();
            tool.startedAt = Instant.now();
            appendStep(new RunStep(toolStart.toolCallId(), state.runId, RunStepType.TOOL_CALL,
                    AgentRunStatus.RUNNING, tool.startedAt, null,
                    payload("toolName", tool.name)));
            appendEvent(state, RuntimeEventType.TOOL_CALL_STARTED, toolStart.toolCallId(),
                    payload("aguiType", event.getType().name(), "toolCallId", toolStart.toolCallId(), "toolName", tool.name));
            return;
        }
        if (event instanceof AguiEvent.ToolCallArgs toolArgs) {
            state.tool(toolArgs.toolCallId()).args.append(nullToEmpty(toolArgs.delta()));
            appendEvent(state, RuntimeEventType.TOOL_CALL_ARGUMENTS, toolArgs.toolCallId(),
                    payload("aguiType", event.getType().name(), "toolCallId", toolArgs.toolCallId(), "delta", toolArgs.delta()));
            return;
        }
        if (event instanceof AguiEvent.ToolCallEnd toolEnd) {
            appendEvent(state, RuntimeEventType.TOOL_CALL_ENDED, toolEnd.toolCallId(),
                    payload("aguiType", event.getType().name(), "toolCallId", toolEnd.toolCallId()));
            return;
        }
        if (event instanceof AguiEvent.ToolCallResult toolResult) {
            finishToolCall(state, toolResult);
            return;
        }
        if (event instanceof AguiEvent.Raw raw) {
            handleRaw(state, raw);
            return;
        }
        if (event instanceof AguiEvent.RunFinished) {
            finishRun(state);
            states.remove(state.runId);
            return;
        }
        appendEvent(state, RuntimeEventType.UNKNOWN, null, payload("aguiType", event.getType().name()));
    }

    private void startRun(AguiRunEventContext context, ProjectionState state) {
        state.agentId = resolveAgentId(context);
        state.input = latestUserMessage(context.input());
        state.traceContext = traceContextPlanner.plan(
                state.runId,
                parseLong(state.sessionId),
                state.agentId,
                stringProp(context.input(), "traceId"),
                stringProp(context.input(), "parentTraceId"),
                payload("source", "agui"));
        resolveCapabilityPlan(state);
        Map<String, Object> metadata = payload(
                "resolvedAgentId", context.resolvedAgentId(),
                "threadId", state.sessionId,
                "traceId", state.traceId(),
                "source", "agui");
        metadata.putAll(capabilityMetadata(state.capabilityPlan));
        saveRun(new AgentRunRecord(
                state.runId,
                state.agentId,
                state.sessionId,
                AgentRunStatus.RUNNING,
                state.input,
                null,
                state.traceId(),
                state.startedAt,
                null,
                metadata));
    }

    private void finishRun(ProjectionState state) {
        if (state.textByMessageId.isEmpty() && state.reasoning.length() > 0) {
            saveAssistantMessage(state, "");
        }
        AgentRunStatus status = state.failed ? AgentRunStatus.FAILED : AgentRunStatus.SUCCEEDED;
        saveRun(new AgentRunRecord(
                state.runId,
                state.agentId,
                state.sessionId,
                status,
                state.input,
                state.output,
                state.traceId(),
                state.startedAt,
                Instant.now(),
                payload("source", "agui", "traceId", state.traceId())));
        if (!state.terminalEventEmitted) {
            appendEvent(state, status == AgentRunStatus.FAILED ? RuntimeEventType.RUN_FAILED : RuntimeEventType.RUN_COMPLETED,
                    null, payload("aguiType", AguiEventType.RUN_FINISHED.name(), "status", status.name()));
            state.terminalEventEmitted = true;
        }
    }

    private void finishToolCall(ProjectionState state, AguiEvent.ToolCallResult toolResult) {
        ToolProjection tool = state.tool(toolResult.toolCallId());
        tool.result = nullToEmpty(toolResult.content());
        tool.endedAt = Instant.now();
        CapabilityItem capability = capabilityForTool(state, tool.name);
        ToolCallPolicy policy = toolPolicy(capability);
        boolean readOnly = capability != null && capability.readOnly();
        Duration duration = Duration.between(
                tool.startedAt == null ? tool.endedAt : tool.startedAt,
                tool.endedAt);
        String parameterSummary = truncated(tool.args.toString());
        String resultSummary = truncated(tool.result);
        appendStep(new RunStep(toolResult.toolCallId(), state.runId, RunStepType.TOOL_CALL,
                AgentRunStatus.SUCCEEDED, tool.startedAt, tool.endedAt,
                payload(
                        "toolName", tool.name,
                        "messageId", toolResult.messageId(),
                        "riskPolicy", policy.name(),
                        "readOnly", readOnly)));
        appendToolCall(new ToolCall(
                toolResult.toolCallId(),
                state.runId,
                tool.name,
                policy,
                readOnly,
                duration,
                0,
                parameterSummary,
                resultSummary,
                null,
                null,
                toolAuditTags(state, capability, tool, toolResult.messageId())));
        if (toolProcessActive(state)) {
            appendChatMessage(state.sessionId, "tool", toolContent(tool, duration));
        }
        appendEvent(state, RuntimeEventType.TOOL_CALL_COMPLETED, toolResult.toolCallId(),
                payload("aguiType", AguiEventType.TOOL_CALL_RESULT.name(),
                        "toolCallId", toolResult.toolCallId(),
                        "toolName", tool.name,
                        "messageId", toolResult.messageId()));
    }

    private void handleRaw(ProjectionState state, AguiEvent.Raw raw) {
        Object rawEvent = raw.rawEvent();
        String error = extractError(rawEvent);
        if (error != null && !error.isBlank()) {
            state.failed = true;
            state.output = error;
            appendChatMessage(state.sessionId, "error", error);
            saveRun(new AgentRunRecord(
                    state.runId,
                    state.agentId,
                    state.sessionId,
                    AgentRunStatus.FAILED,
                    state.input,
                    error,
                    state.traceId(),
                    state.startedAt,
                    Instant.now(),
                    payload("source", "agui", "traceId", state.traceId(), "error", error)));
            appendEvent(state, RuntimeEventType.RUN_FAILED, null,
                    payload("aguiType", AguiEventType.RAW.name(), "error", error));
            state.terminalEventEmitted = true;
        } else {
            appendEvent(state, RuntimeEventType.UNKNOWN, null,
                    payload("aguiType", AguiEventType.RAW.name(), "raw", String.valueOf(rawEvent)));
        }
    }

    private void saveAssistantMessage(ProjectionState state, String text) {
        String reasoning = state.reasoning.toString();
        String content = assistantContent(reasoning, text);
        state.output = text == null || text.isBlank() ? reasoning : text;
        if (!content.isBlank()) {
            appendChatMessage(state.sessionId, "assistant", content);
        }
        state.reasoning.setLength(0);
    }

    private void resolveCapabilityPlan(ProjectionState state) {
        if (state.agentId == null || capabilityPlanService == null) {
            return;
        }
        try {
            state.capabilityPlan = capabilityPlanService.resolvePlan(state.agentId);
            for (CapabilityItem item : state.capabilityPlan.items()) {
                indexCapability(state, item.name(), item);
                indexCapability(state, item.capabilityId(), item);
                indexCapability(state, item.namespace(), item);
                Object toolId = item.attributes().get("toolId");
                if (toolId != null) {
                    indexCapability(state, String.valueOf(toolId), item);
                }
            }
        } catch (Exception e) {
            appendEvent(state, RuntimeEventType.UNKNOWN, null,
                    payload("source", "agui", "capabilityPlanError", e.getMessage()));
        }
    }

    private Map<String, Object> capabilityMetadata(CapabilityPlan plan) {
        if (plan == null) {
            return Map.of();
        }
        return payload(
                "capabilityPlanId", plan.planId(),
                "capabilityPlanItemCount", plan.items().size(),
                "enabledCapabilityCount", plan.enabledItems().size(),
                "executableCapabilityCount", plan.executableItems().size(),
                "highRiskDefaultPolicy", plan.highRiskDefaultPolicy().name());
    }

    private CapabilityItem capabilityForTool(ProjectionState state, String toolName) {
        if (toolName == null || toolName.isBlank()) {
            return null;
        }
        return state.capabilitiesByName.get(normalize(toolName));
    }

    private ToolCallPolicy toolPolicy(CapabilityItem capability) {
        if (capability == null || capability.riskPolicy() == null) {
            return ToolCallPolicy.ASK;
        }
        return ToolCallPolicy.valueOf(capability.riskPolicy().name());
    }

    private Map<String, Object> toolAuditTags(
            ProjectionState state,
            CapabilityItem capability,
            ToolProjection tool,
            String messageId) {
        Map<String, Object> tags = payload(
                "source", "agui",
                "messageId", messageId,
                "traceId", state.traceId(),
                "parameterSize", tool.args.length(),
                "parameterTruncated", tool.args.length() > TOOL_SUMMARY_MAX_CHARS,
                "resultSize", tool.result == null ? 0 : tool.result.length(),
                "resultTruncated", tool.result != null && tool.result.length() > TOOL_SUMMARY_MAX_CHARS);
        if (capability != null) {
            tags.put("capabilityKind", capability.kind().name());
            tags.put("capabilityId", capability.capabilityId());
            tags.put("riskLevel", capability.riskLevel().name());
            tags.put("riskPolicy", capability.riskPolicy().name());
            tags.put("approvalStatus", capability.riskPolicy().name());
        }
        return tags;
    }

    private void appendEvent(
            ProjectionState state,
            RuntimeEventType eventType,
            String stepId,
            Map<String, Object> payload) {
        RuntimeEvent event = new RuntimeEvent(
                UUID.randomUUID().toString(),
                eventType,
                state.runId,
                state.sessionId,
                stepId,
                state.traceId(),
                state.nextSequence(),
                Instant.now(),
                payload);
        eventSinks.forEach(sink -> sink.append(event));
    }

    private void appendStep(RunStep step) {
        stepSinks.forEach(sink -> sink.append(step));
    }

    private void appendToolCall(ToolCall toolCall) {
        toolCallSinks.forEach(sink -> sink.append(toolCall));
    }

    private void saveRun(AgentRunRecord record) {
        runLedgers.forEach(ledger -> ledger.save(record));
    }

    private void appendChatMessage(String sessionId, String role, String content) {
        Long parsedSessionId = parseLong(sessionId);
        if (parsedSessionId == null || content == null || content.isBlank()) {
            return;
        }
        ReentrantLock lock = sessionLockManager.getLock(parsedSessionId);
        lock.lock();
        try {
            ChatMessageAppendDTO dto = new ChatMessageAppendDTO();
            dto.setRole(role);
            dto.setContent(content);
            chatSessionService.appendMessage(parsedSessionId, dto);
        } finally {
            lock.unlock();
            sessionLockManager.cleanupIfUnused(parsedSessionId, lock);
        }
    }

    private Long resolveAgentId(AguiRunEventContext context) {
        Object propAgentId = context.input() == null ? null : context.input().getForwardedProp("agentId");
        Long numeric = parseLong(propAgentId == null ? null : propAgentId.toString());
        if (numeric != null) {
            return numeric;
        }
        String agentCode = nonBlank(
                context.resolvedAgentId(),
                context.input() == null ? null : stringProp(context.input(), "agentCode"));
        if (agentCode == null) {
            return null;
        }
        AgentDefinition definition = agentDefinitionRepository.getByAgentCode(agentCode);
        return definition == null ? null : definition.getId();
    }

    private String latestUserMessage(RunAgentInput input) {
        if (input == null || input.getMessages() == null) {
            return "";
        }
        List<AguiMessage> messages = input.getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            AguiMessage message = messages.get(i);
            if (message != null && message.isUserMessage()) {
                return nullToEmpty(message.getContent());
            }
        }
        return "";
    }

    private boolean toolProcessActive(ProjectionState state) {
        Object value = state.forwardedProps.get("toolProcessActive");
        return !(value instanceof Boolean active) || active;
    }

    private String assistantContent(String reasoning, String content) {
        try {
            return objectMapper.writeValueAsString(payload(
                    "reasoning", nullToEmpty(reasoning),
                    "content", nullToEmpty(content)));
        } catch (Exception e) {
            return nullToEmpty(content);
        }
    }

    private String toolContent(ToolProjection tool, Duration duration) {
        StringBuilder block = new StringBuilder();
        if (tool.args.length() > 0) {
            block.append("\n````json\n")
                    .append(truncated(tool.args.toString()))
                    .append("\n````\n");
        }
        if (tool.result != null) {
            block.append("\n````\n")
                    .append(truncated(tool.result))
                    .append("\n````\n");
        }
        return "<details> <summary> <span class=\"tool-call-title\"> "
                + nullToEmpty(tool.name)
                + "（耗时："
                + duration.toMillis()
                + "ms） </span> </summary>\n\n "
                + block
                + " </details>";
    }

    private String extractError(Object rawEvent) {
        if (rawEvent instanceof Map<?, ?> map) {
            Object error = map.get("error");
            return error == null ? null : String.valueOf(error);
        }
        return null;
    }

    private static String stringProp(RunAgentInput input, String key) {
        if (input == null) {
            return null;
        }
        Object value = input.getForwardedProp(key);
        return value == null ? null : String.valueOf(value);
    }

    private static String nonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void indexCapability(ProjectionState state, String key, CapabilityItem capability) {
        if (key != null && !key.isBlank() && capability != null) {
            state.capabilitiesByName.putIfAbsent(normalize(key), capability);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String truncated(String value) {
        if (value == null || value.length() <= TOOL_SUMMARY_MAX_CHARS) {
            return value;
        }
        int omitted = value.length() - TOOL_SUMMARY_MAX_CHARS;
        return value.substring(0, TOOL_SUMMARY_MAX_CHARS)
                + "\n...[truncated "
                + omitted
                + " chars]";
    }

    private static StringBuilder text(Map<String, StringBuilder> values, String id) {
        return values.computeIfAbsent(id == null ? "default" : id, ignored -> new StringBuilder());
    }

    private static Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            Object value = entries[i + 1];
            if (value != null) {
                payload.put(String.valueOf(entries[i]), value);
            }
        }
        return payload;
    }

    private static final class ProjectionState {
        private final String runId;
        private final Instant startedAt = Instant.now();
        private final AtomicLong sequence = new AtomicLong();
        private final Map<String, StringBuilder> textByMessageId = new ConcurrentHashMap<>();
        private final StringBuilder reasoning = new StringBuilder();
        private final Map<String, ToolProjection> toolsById = new ConcurrentHashMap<>();
        private final Map<String, Object> forwardedProps = new ConcurrentHashMap<>();
        private final Map<String, CapabilityItem> capabilitiesByName = new ConcurrentHashMap<>();
        private String sessionId;
        private Long agentId;
        private String input;
        private String output;
        private CapabilityPlan capabilityPlan;
        private AgentTraceContext traceContext;
        private boolean failed;
        private boolean terminalEventEmitted;

        private ProjectionState(String runId, String sessionId) {
            this.runId = runId;
            this.sessionId = sessionId;
        }

        private long nextSequence() {
            return sequence.incrementAndGet();
        }

        private ToolProjection tool(String toolCallId) {
            return toolsById.computeIfAbsent(toolCallId == null ? "default" : toolCallId, ignored -> new ToolProjection());
        }

        private String traceId() {
            return traceContext == null ? runId : traceContext.traceId();
        }
    }

    private static final class ToolProjection {
        private String name;
        private Instant startedAt;
        private Instant endedAt;
        private final StringBuilder args = new StringBuilder();
        private String result;
    }
}
