package com.htam.agent.profile.core;

import com.htam.agent.governance.risk.GovernanceDecision;

public record GovernedProfileChangeDecision(
        ProfileDescriptor profile,
        String operation,
        GovernanceDecision governanceDecision) {

    public GovernedProfileChangeDecision {
        if (profile == null) {
            throw new IllegalArgumentException("profile 不能为空");
        }
        operation = operation == null || operation.isBlank() ? "profile.change" : operation;
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
