package com.htam.agent.profile.core;

import com.htam.agent.governance.risk.GovernanceActionRequest;
import com.htam.agent.governance.risk.GovernanceDecision;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicy;
import java.util.LinkedHashMap;
import java.util.Map;

public class GovernedProfileChangePlanner {

    private final GovernanceGuard governanceGuard;

    public GovernedProfileChangePlanner(GovernanceGuard governanceGuard) {
        if (governanceGuard == null) {
            throw new IllegalArgumentException("governanceGuard 不能为空");
        }
        this.governanceGuard = governanceGuard;
    }

    public GovernedProfileChangeDecision evaluate(
            ProfileDescriptor profile,
            String operation,
            String actor,
            LayeredRiskPolicy riskPolicy) {
        Map<String, Object> context = new LinkedHashMap<>(profile.metadata());
        context.put("agentId", profile.agentId());
        context.put("agentCode", profile.agentCode());
        context.put("profileVersion", profile.version());
        GovernanceDecision decision = governanceGuard.evaluate(new GovernanceActionRequest(
                null,
                null,
                actor,
                operation == null || operation.isBlank() ? "profile.change" : operation,
                riskPolicy,
                context));
        return new GovernedProfileChangeDecision(profile, operation, decision);
    }
}
