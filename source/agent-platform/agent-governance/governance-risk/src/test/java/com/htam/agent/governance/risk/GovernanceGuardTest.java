package com.htam.agent.governance.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.approval.InMemoryApprovalLedger;
import com.htam.agent.governance.audit.InMemoryAuditLedger;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GovernanceGuardTest {

    @Test
    void evaluateCreatesApprovalAndAuditForAskPolicy() {
        InMemoryApprovalLedger approvals = new InMemoryApprovalLedger();
        InMemoryAuditLedger audit = new InMemoryAuditLedger();
        GovernanceGuard guard = new GovernanceGuard(new LayeredRiskPolicyResolver(), approvals, audit);

        GovernanceDecision decision = guard.evaluate(new GovernanceActionRequest(
                "run-1",
                "step-1",
                "user-1",
                "tool.call",
                new LayeredRiskPolicy(CapabilityRiskPolicy.ALLOW, CapabilityRiskPolicy.ASK, null),
                Map.of("traceId", "trace-1")));

        assertTrue(decision.requiresApproval());
        assertEquals(1, approvals.listPendingByRunId("run-1").size());
        assertEquals(1, audit.listByRunId("run-1").size());
    }

    @Test
    void evaluateDeniesWhenAnyLayerDenies() {
        GovernanceGuard guard = new GovernanceGuard(
                new LayeredRiskPolicyResolver(),
                new InMemoryApprovalLedger(),
                new InMemoryAuditLedger());

        GovernanceDecision decision = guard.evaluate(new GovernanceActionRequest(
                "run-1",
                "step-1",
                "user-1",
                "shell.exec",
                new LayeredRiskPolicy(CapabilityRiskPolicy.DENY, CapabilityRiskPolicy.ALLOW, null),
                Map.of()));

        assertEquals(GovernanceDecisionStatus.DENIED, decision.status());
    }
}
