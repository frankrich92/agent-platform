package com.htam.agent.governance.risk;

public record GovernanceDecision(GovernanceDecisionStatus status, String approvalId, String reason) {

    public boolean allowed() {
        return status == GovernanceDecisionStatus.ALLOWED;
    }

    public boolean requiresApproval() {
        return status == GovernanceDecisionStatus.REQUIRES_APPROVAL;
    }
}
