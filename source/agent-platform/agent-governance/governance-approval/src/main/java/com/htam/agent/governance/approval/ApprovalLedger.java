package com.htam.agent.governance.approval;

import java.util.List;
import java.util.Optional;

public interface ApprovalLedger {

    ApprovalRequest create(ApprovalRequest request);

    ApprovalRequest decide(String approvalId, ApprovalDecision decision, String reviewer);

    Optional<ApprovalRequest> findById(String approvalId);

    List<ApprovalRequest> listPendingByRunId(String runId);
}
