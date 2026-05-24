package com.htam.agent.run;

public record AgentRunCommand(
        Long agentId,
        Long sessionId,
        String input,
        String runId,
        boolean recordMessages) {

    public AgentRunCommand {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        input = input == null ? "" : input;
    }

    public static AgentRunCommand sessionRun(Long agentId, Long sessionId, String input) {
        return new AgentRunCommand(agentId, sessionId, input, null, true);
    }

    public static AgentRunCommand backgroundRun(Long agentId, String input) {
        return new AgentRunCommand(agentId, null, input, null, false);
    }
}
