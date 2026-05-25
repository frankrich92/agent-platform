package com.htam.agent.capability.tool;

import com.htam.agent.governance.risk.GovernanceDecision;

public record GovernedToolExecutionDecision(
        ToolExecutionDecision toolDecision,
        GovernanceDecision governanceDecision) {

    public boolean allowed() {
        return toolDecision.enabled() && governanceDecision.allowed();
    }

    public boolean requiresApproval() {
        return governanceDecision.requiresApproval();
    }
}
