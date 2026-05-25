package com.htam.agent.profile.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.approval.InMemoryApprovalLedger;
import com.htam.agent.governance.audit.InMemoryAuditLedger;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicy;
import com.htam.agent.governance.risk.LayeredRiskPolicyResolver;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GovernedProfileChangePlannerTest {

    @Test
    void routesProfileChangesThroughGovernanceGuard() {
        InMemoryApprovalLedger approvals = new InMemoryApprovalLedger();
        GovernedProfileChangePlanner planner = new GovernedProfileChangePlanner(new GovernanceGuard(
                new LayeredRiskPolicyResolver(),
                approvals,
                new InMemoryAuditLedger()));

        GovernedProfileChangeDecision decision = planner.evaluate(
                new ProfileDescriptor(1L, "agent-code", "profile", "v1", true, null, Map.of()),
                "profile.model-policy.update",
                "user-1",
                new LayeredRiskPolicy(CapabilityRiskPolicy.ALLOW, CapabilityRiskPolicy.ASK, null));

        assertTrue(decision.requiresApproval());
        assertTrue(approvals.listPendingByRunId(null).stream()
                .anyMatch(request -> "profile.model-policy.update".equals(request.action())));
    }
}
