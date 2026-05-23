package com.htam.agent.runtime;

import java.util.Map;

/**
 * 平台侧 Agent 运行请求，不暴露具体运行时类型。
 */
public record AgentRunRequest(
        Long agentId,
        String input,
        String threadId,
        String runId,
        String capabilityPlanId,
        Map<String, Object> metadata) {

    public AgentRunRequest(Long agentId, String input, String threadId, String runId) {
        this(agentId, input, threadId, runId, null, Map.of());
    }

    public AgentRunRequest {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        input = input == null ? "" : input;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static AgentRunRequest of(Long agentId, String input) {
        return new AgentRunRequest(agentId, input, null, null);
    }
}
