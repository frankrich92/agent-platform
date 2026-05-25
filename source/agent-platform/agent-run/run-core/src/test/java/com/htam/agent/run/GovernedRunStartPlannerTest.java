package com.htam.agent.run;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.approval.InMemoryApprovalLedger;
import com.htam.agent.governance.audit.InMemoryAuditLedger;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicy;
import com.htam.agent.governance.risk.LayeredRiskPolicyResolver;
import org.junit.jupiter.api.Test;

class GovernedRunStartPlannerTest {

    @Test
    void routesRunStartThroughGovernanceGuard() {
        InMemoryApprovalLedger approvals = new InMemoryApprovalLedger();
        GovernedRunStartPlanner planner = new GovernedRunStartPlanner(new GovernanceGuard(
                new LayeredRiskPolicyResolver(),
                approvals,
                new InMemoryAuditLedger()));

        GovernedRunStartDecision decision = planner.evaluate(
                new AgentRunCommand(1L, 2L, "hello", "run-1", true),
                "user-1",
                new LayeredRiskPolicy(CapabilityRiskPolicy.ALLOW, CapabilityRiskPolicy.ASK, null));

        assertTrue(decision.requiresApproval());
        assertTrue(approvals.listPendingByRunId("run-1").stream()
                .anyMatch(request -> "run.start".equals(request.action())));
    }
}
