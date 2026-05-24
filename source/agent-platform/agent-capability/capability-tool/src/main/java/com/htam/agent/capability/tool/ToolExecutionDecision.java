package com.htam.agent.capability.tool;

import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import java.time.Duration;

public record ToolExecutionDecision(
        String toolName,
        boolean enabled,
        boolean readOnly,
        boolean parallelAllowed,
        CapabilityRiskLevel riskLevel,
        CapabilityRiskPolicy riskPolicy,
        Duration schemaCacheTtl,
        int maxResultChars) {

    public ToolExecutionDecision {
        toolName = toolName == null || toolName.isBlank() ? "unknown-tool" : toolName;
        riskLevel = riskLevel == null ? CapabilityRiskLevel.LOW : riskLevel;
        riskPolicy = riskPolicy == null ? CapabilityRiskPolicy.ASK : riskPolicy;
        schemaCacheTtl = schemaCacheTtl == null ? Duration.ofMinutes(10) : schemaCacheTtl;
        maxResultChars = maxResultChars <= 0 ? 8192 : maxResultChars;
    }

    public boolean requiresApproval() {
        return enabled && riskPolicy == CapabilityRiskPolicy.ASK;
    }
}
