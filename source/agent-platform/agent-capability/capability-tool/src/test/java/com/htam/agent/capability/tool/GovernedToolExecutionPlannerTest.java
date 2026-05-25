package com.htam.agent.capability.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.approval.InMemoryApprovalLedger;
import com.htam.agent.governance.audit.InMemoryAuditLedger;
import com.htam.agent.governance.risk.GovernanceDecisionStatus;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicyResolver;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GovernedToolExecutionPlannerTest {

    @Test
    void routesHighRiskToolThroughGovernanceGuard() {
        InMemoryApprovalLedger approvals = new InMemoryApprovalLedger();
        InMemoryAuditLedger audit = new InMemoryAuditLedger();
        GovernedToolExecutionPlanner planner = new GovernedToolExecutionPlanner(
                new ToolExecutionPlanner(),
                new GovernanceGuard(new LayeredRiskPolicyResolver(), approvals, audit));

        GovernedToolExecutionDecision decision = planner.decide(
                new CapabilityItem(
                        CapabilityKind.TOOL,
                        "tool-1",
                        "shell",
                        "tool",
                        true,
                        false,
                        CapabilityRiskLevel.HIGH,
                        CapabilityRiskPolicy.ASK,
                        null,
                        List.of(),
                        List.of(),
                        Map.of()),
                ToolExecutionOptions.enterpriseDefault(),
                "run-1",
                "step-1",
                Map.of("traceId", "trace-1"));

        assertTrue(decision.requiresApproval());
        assertEquals(GovernanceDecisionStatus.REQUIRES_APPROVAL, decision.governanceDecision().status());
        assertEquals(1, approvals.listPendingByRunId("run-1").size());
        assertEquals(1, audit.listByTraceId("trace-1").size());
    }
}
