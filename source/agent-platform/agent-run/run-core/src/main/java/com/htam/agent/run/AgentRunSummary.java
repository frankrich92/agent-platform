package com.htam.agent.run;

import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.runtime.AgentRunResult;

public record AgentRunSummary(
        String runId,
        Long agentId,
        Long sessionId,
        AgentRunResult runtimeResult,
        CapabilityPlan capabilityPlan,
        Integer userMessageId,
        Integer assistantMessageId) {
}
