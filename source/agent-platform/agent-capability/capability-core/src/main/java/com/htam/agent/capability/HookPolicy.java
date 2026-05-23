package com.htam.agent.capability;

import java.util.List;

public record HookPolicy(
        List<HookLifecyclePhase> phases,
        boolean deterministicOnly,
        CapabilityRiskLevel maxRiskLevel) {

    public HookPolicy {
        phases = phases == null ? List.of() : List.copyOf(phases);
        maxRiskLevel = maxRiskLevel == null ? CapabilityRiskLevel.LOW : maxRiskLevel;
    }

    public static HookPolicy lowRiskDefault() {
        return new HookPolicy(List.of(
                HookLifecyclePhase.BEFORE_RUN,
                HookLifecyclePhase.BEFORE_TOOL_CALL,
                HookLifecyclePhase.AFTER_TOOL_CALL,
                HookLifecyclePhase.AFTER_RUN,
                HookLifecyclePhase.ON_FAILURE), true, CapabilityRiskLevel.LOW);
    }
}
