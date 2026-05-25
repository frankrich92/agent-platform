package com.htam.agent.run;

import com.htam.agent.governance.risk.GovernanceDecision;

public record GovernedRunStartDecision(
        AgentRunCommand command,
        GovernanceDecision governanceDecision) {

    public GovernedRunStartDecision {
        if (command == null) {
            throw new IllegalArgumentException("command 不能为空");
        }
        if (governanceDecision == null) {
            throw new IllegalArgumentException("governanceDecision 不能为空");
        }
    }

    public boolean allowed() {
        return governanceDecision.allowed();
    }

    public boolean requiresApproval() {
        return governanceDecision.requiresApproval();
    }
}
