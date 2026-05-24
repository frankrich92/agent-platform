package com.htam.agent.governance.risk;

import com.htam.agent.capability.CapabilityRiskPolicy;
import java.util.List;

public class LayeredRiskPolicyResolver {

    public CapabilityRiskPolicy resolve(LayeredRiskPolicy policy) {
        LayeredRiskPolicy layeredPolicy = policy == null ? LayeredRiskPolicy.enterpriseDefault() : policy;
        List<CapabilityRiskPolicy> policies = List.of(
                layeredPolicy.platformPolicy(),
                layeredPolicy.profilePolicy(),
                layeredPolicy.sessionPolicy() == null
                        ? layeredPolicy.profilePolicy()
                        : layeredPolicy.sessionPolicy());
        if (policies.contains(CapabilityRiskPolicy.DENY)) {
            return CapabilityRiskPolicy.DENY;
        }
        if (policies.contains(CapabilityRiskPolicy.ASK)) {
            return CapabilityRiskPolicy.ASK;
        }
        return CapabilityRiskPolicy.ALLOW;
    }
}
