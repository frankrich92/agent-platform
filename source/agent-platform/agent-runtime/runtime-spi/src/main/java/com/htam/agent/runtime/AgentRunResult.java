package com.htam.agent.runtime;

/**
 * 平台侧 Agent 运行结果，不携带具体运行时对象。
 */
public record AgentRunResult(Long agentId, boolean success, String message) {

    public static AgentRunResult success(Long agentId) {
        return new AgentRunResult(agentId, true, null);
    }

    public static AgentRunResult failure(Long agentId, String message) {
        return new AgentRunResult(agentId, false, message);
    }
}
