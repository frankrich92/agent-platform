package com.htam.agent.runtime;

/**
 * 平台侧 Agent 运行请求，不暴露具体运行时类型。
 */
public record AgentRunRequest(Long agentId, String input, String threadId, String runId) {

    public AgentRunRequest {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        input = input == null ? "" : input;
    }

    public static AgentRunRequest of(Long agentId, String input) {
        return new AgentRunRequest(agentId, input, null, null);
    }
}
