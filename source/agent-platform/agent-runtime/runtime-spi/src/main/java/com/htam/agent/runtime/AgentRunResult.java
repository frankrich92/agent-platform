package com.htam.agent.runtime;

import java.util.List;

/**
 * 平台侧 Agent 运行结果，不携带具体运行时对象。
 */
public record AgentRunResult(
        Long agentId,
        String runId,
        AgentRunStatus status,
        String message,
        List<RuntimeEvent> events,
        List<RunStep> steps,
        List<ToolCall> toolCalls) {

    public AgentRunResult(Long agentId, boolean success, String message) {
        this(agentId, null, success ? AgentRunStatus.SUCCEEDED : AgentRunStatus.FAILED,
                message, List.of(), List.of(), List.of());
    }

    public AgentRunResult {
        status = status == null ? AgentRunStatus.SUCCEEDED : status;
        events = events == null ? List.of() : List.copyOf(events);
        steps = steps == null ? List.of() : List.copyOf(steps);
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public boolean success() {
        return status == AgentRunStatus.SUCCEEDED;
    }

    public static AgentRunResult success(Long agentId) {
        return new AgentRunResult(agentId, true, null);
    }

    public static AgentRunResult success(Long agentId, String runId, String message, List<RuntimeEvent> events) {
        return new AgentRunResult(agentId, runId, AgentRunStatus.SUCCEEDED, message, events, List.of(), List.of());
    }

    public static AgentRunResult failure(Long agentId, String message) {
        return new AgentRunResult(agentId, false, message);
    }

    public static AgentRunResult failure(Long agentId, String runId, String message, List<RuntimeEvent> events) {
        return new AgentRunResult(agentId, runId, AgentRunStatus.FAILED, message, events, List.of(), List.of());
    }
}
