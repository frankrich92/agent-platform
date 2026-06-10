package com.htam.agent.governance.risk;

import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.approval.ApprovalLedger;
import com.htam.agent.governance.approval.ApprovalRequest;
import com.htam.agent.governance.audit.AuditEvent;
import com.htam.agent.governance.audit.AuditLedger;
import com.htam.agent.governance.audit.AuditSeverity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 风险治理统一守卫。
 * 根据分层风险策略输出允许、拒绝或需审批的决策，并在需要时同步写入审批和审计台账。
 */
public class GovernanceGuard {

    private final LayeredRiskPolicyResolver riskPolicyResolver;
    private final ApprovalLedger approvalLedger;
    private final AuditLedger auditLedger;

    public GovernanceGuard(
            LayeredRiskPolicyResolver riskPolicyResolver,
            ApprovalLedger approvalLedger,
            AuditLedger auditLedger) {
        this.riskPolicyResolver = riskPolicyResolver == null ? new LayeredRiskPolicyResolver() : riskPolicyResolver;
        this.approvalLedger = approvalLedger;
        this.auditLedger = auditLedger;
    }

    public GovernanceDecision evaluate(GovernanceActionRequest request) {
        CapabilityRiskPolicy resolved = riskPolicyResolver.resolve(request.riskPolicy());
        if (resolved == CapabilityRiskPolicy.DENY) {
            audit(request, AuditSeverity.WARN, "governance.denied", Map.of("policy", resolved.name()));
            return new GovernanceDecision(GovernanceDecisionStatus.DENIED, null, "policy denied");
        }
        if (resolved == CapabilityRiskPolicy.ASK) {
            ApprovalRequest approval = createApproval(request);
            audit(request, AuditSeverity.INFO, "governance.approval_required",
                    Map.of("policy", resolved.name(), "approvalId", approval.approvalId()));
            return new GovernanceDecision(
                    GovernanceDecisionStatus.REQUIRES_APPROVAL,
                    approval.approvalId(),
                    "approval required");
        }
        audit(request, AuditSeverity.INFO, "governance.allowed", Map.of("policy", resolved.name()));
        return new GovernanceDecision(GovernanceDecisionStatus.ALLOWED, null, "allowed");
    }

    private ApprovalRequest createApproval(GovernanceActionRequest request) {
        if (approvalLedger == null) {
            throw new IllegalStateException("approvalLedger 未配置");
        }
        return approvalLedger.create(new ApprovalRequest(
                UUID.randomUUID().toString(),
                request.runId(),
                request.stepId(),
                request.actor(),
                request.action(),
                null,
                null,
                request.context()));
    }

    private void audit(
            GovernanceActionRequest request,
            AuditSeverity severity,
            String action,
            Map<String, Object> attributes) {
        if (auditLedger == null) {
            return;
        }
        Map<String, Object> merged = new LinkedHashMap<>(request.context());
        merged.putAll(attributes);
        auditLedger.append(new AuditEvent(
                UUID.randomUUID().toString(),
                String.valueOf(merged.getOrDefault("traceId", request.runId())),
                request.runId(),
                request.actor(),
                action,
                severity,
                null,
                merged));
    }
}
