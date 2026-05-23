package com.htam.agent.capability;

import java.time.Duration;

public record ToolExecutionOptions(
        boolean parallelReadOnlyEnabled,
        Duration schemaCacheTtl,
        int maxResultChars,
        CapabilityRiskPolicy highRiskFallbackPolicy) {

    public ToolExecutionOptions {
        schemaCacheTtl = schemaCacheTtl == null ? Duration.ofMinutes(10) : schemaCacheTtl;
        maxResultChars = maxResultChars <= 0 ? 8192 : maxResultChars;
        highRiskFallbackPolicy = highRiskFallbackPolicy == null ? CapabilityRiskPolicy.ASK : highRiskFallbackPolicy;
    }

    public static ToolExecutionOptions enterpriseDefault() {
        return new ToolExecutionOptions(true, Duration.ofMinutes(10), 8192, CapabilityRiskPolicy.ASK);
    }
}
