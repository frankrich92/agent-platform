package com.htam.agent.governance.risk;

import com.htam.agent.capability.CapabilityRiskPolicy;

public record LayeredRiskPolicy(
        CapabilityRiskPolicy platformPolicy,
        CapabilityRiskPolicy profilePolicy,
        CapabilityRiskPolicy sessionPolicy) {

    public LayeredRiskPolicy {
        platformPolicy = platformPolicy == null ? CapabilityRiskPolicy.ASK : platformPolicy;
        profilePolicy = profilePolicy == null ? CapabilityRiskPolicy.ASK : profilePolicy;
    }

    public static LayeredRiskPolicy enterpriseDefault() {
        return new LayeredRiskPolicy(CapabilityRiskPolicy.ASK, CapabilityRiskPolicy.ASK, null);
    }
}
