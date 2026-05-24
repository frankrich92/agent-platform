package com.htam.agent.governance.approval;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryApprovalLedger implements ApprovalLedger {

    private final ConcurrentMap<String, ApprovalRequest> approvals = new ConcurrentHashMap<>();

    @Override
    public ApprovalRequest create(ApprovalRequest request) {
        approvals.put(request.approvalId(), request);
        return request;
    }

    @Override
    public ApprovalRequest decide(String approvalId, ApprovalDecision decision, String reviewer) {
        ApprovalRequest current = approvals.get(approvalId);
        if (current == null) {
            throw new IllegalArgumentException("ApprovalRequest 不存在: " + approvalId);
        }
        Map<String, Object> context = new LinkedHashMap<>(current.context());
        context.put("reviewer", reviewer);
        context.put("decidedAt", Instant.now().toString());
        ApprovalRequest updated = new ApprovalRequest(current.approvalId(), current.runId(), current.stepId(),
                current.requester(), current.action(), decision, current.createdAt(), context);
        approvals.put(approvalId, updated);
        return updated;
    }

    @Override
    public Optional<ApprovalRequest> findById(String approvalId) {
        return Optional.ofNullable(approvals.get(approvalId));
    }

    @Override
    public List<ApprovalRequest> listPendingByRunId(String runId) {
        return approvals.values().stream()
                .filter(request -> runId == null || runId.equals(request.runId()))
                .filter(request -> request.decision() == ApprovalDecision.PENDING)
                .toList();
    }
}
