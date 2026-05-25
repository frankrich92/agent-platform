package com.htam.agent.runtime.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.approval.InMemoryApprovalLedger;
import com.htam.agent.governance.audit.InMemoryAuditLedger;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicyResolver;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GovernedRuntimeRunnerTest {

    @Test
    void requiresApprovalBeforeRunningRuntimeWhenPolicyAsks() {
        InMemoryApprovalLedger approvals = new InMemoryApprovalLedger();
        InMemoryAuditLedger audit = new InMemoryAuditLedger();
        GovernedRuntimeRunner runner = new GovernedRuntimeRunner(
                request -> AgentRunResult.success(request.agentId()),
                new GovernanceGuard(new LayeredRiskPolicyResolver(), approvals, audit));

        AgentRunResult result = runner.run(new AgentRunRequest(
                1L,
                "hello",
                "thread-1",
                "run-1",
                "plan-1",
                Map.of("runtimePolicy", CapabilityRiskPolicy.ASK, "traceId", "trace-1")));

        assertEquals(AgentRunStatus.WAITING_APPROVAL, result.status());
        assertEquals(1, approvals.listPendingByRunId("run-1").size());
        assertEquals(1, audit.listByTraceId("trace-1").size());
    }
}
