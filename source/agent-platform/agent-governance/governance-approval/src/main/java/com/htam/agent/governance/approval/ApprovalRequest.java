package com.htam.agent.governance.approval;

import java.time.Instant;
import java.util.Map;

public record ApprovalRequest(
        String approvalId,
        String runId,
        String stepId,
        String requester,
        String action,
        ApprovalDecision decision,
        Instant createdAt,
        Map<String, Object> context) {

    public ApprovalRequest {
        if (approvalId == null || approvalId.isBlank()) {
            throw new IllegalArgumentException("approvalId 不能为空");
        }
        decision = decision == null ? ApprovalDecision.PENDING : decision;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        context = context == null ? Map.of() : Map.copyOf(context);
    }
}
