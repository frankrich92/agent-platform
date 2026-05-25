package com.htam.agent.worker.core;

import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.risk.GovernanceActionRequest;
import com.htam.agent.governance.risk.GovernanceDecision;
import com.htam.agent.governance.risk.GovernanceDecisionStatus;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicy;
import com.htam.agent.worker.spi.WorkerRiskPolicy;
import com.htam.agent.worker.spi.WorkerRuntime;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class GovernedWorkerRuntime implements WorkerRuntime {

    private final WorkerTaskLedger ledger;
    private final GovernanceGuard governanceGuard;

    public GovernedWorkerRuntime(WorkerTaskLedger ledger) {
        this(ledger, null);
    }

    public GovernedWorkerRuntime(WorkerTaskLedger ledger, GovernanceGuard governanceGuard) {
        this.ledger = ledger;
        this.governanceGuard = governanceGuard;
    }

    @Override
    public WorkerTaskResult submit(WorkerTask task) {
        if (ledger != null) {
            ledger.appendTask(task);
        }
        GovernanceDecision decision = evaluate(task);
        WorkerTaskStatus status = toWorkerStatus(decision.status());
        Map<String, Object> metadata = resultMetadata(task, decision);
        WorkerTaskResult result = new WorkerTaskResult(task.taskId(), status, Duration.ZERO,
                status == WorkerTaskStatus.ACCEPTED ? "worker task accepted" : null,
                status == WorkerTaskStatus.DENIED ? "WORKER_POLICY_DENIED" : null,
                status == WorkerTaskStatus.DENIED ? decision.reason() : null,
                metadata);
        if (ledger != null) {
            ledger.appendResult(result);
        }
        return result;
    }

    private GovernanceDecision evaluate(WorkerTask task) {
        if (governanceGuard != null) {
            return governanceGuard.evaluate(new GovernanceActionRequest(
                    task.taskId(),
                    null,
                    "worker",
                    "worker." + task.taskType().name().toLowerCase(),
                    toLayeredRiskPolicy(task.riskPolicy()),
                    requestContext(task)));
        }
        return switch (task.riskPolicy()) {
            case DENY -> new GovernanceDecision(GovernanceDecisionStatus.DENIED, null, "worker task denied by policy");
            case ASK -> new GovernanceDecision(GovernanceDecisionStatus.REQUIRES_APPROVAL, null, "approval required");
            case ALLOW -> new GovernanceDecision(GovernanceDecisionStatus.ALLOWED, null, "allowed");
        };
    }

    private static LayeredRiskPolicy toLayeredRiskPolicy(WorkerRiskPolicy riskPolicy) {
        CapabilityRiskPolicy capabilityRiskPolicy = CapabilityRiskPolicy.valueOf(riskPolicy.name());
        return new LayeredRiskPolicy(capabilityRiskPolicy, capabilityRiskPolicy, null);
    }

    private static WorkerTaskStatus toWorkerStatus(GovernanceDecisionStatus status) {
        return switch (status) {
            case ALLOWED -> WorkerTaskStatus.ACCEPTED;
            case REQUIRES_APPROVAL -> WorkerTaskStatus.APPROVAL_REQUIRED;
            case DENIED -> WorkerTaskStatus.DENIED;
        };
    }

    private static Map<String, Object> requestContext(WorkerTask task) {
        Map<String, Object> context = new LinkedHashMap<>(task.input());
        context.put("taskId", task.taskId());
        context.put("taskType", task.taskType().name());
        if (task.workspaceRef() != null && !task.workspaceRef().isBlank()) {
            context.put("workspaceRef", task.workspaceRef());
        }
        if (task.command() != null && !task.command().isBlank()) {
            context.put("command", task.command());
        }
        return context;
    }

    private static Map<String, Object> resultMetadata(WorkerTask task, GovernanceDecision decision) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("taskType", task.taskType().name());
        metadata.put("governanceStatus", decision.status().name());
        if (decision.approvalId() != null && !decision.approvalId().isBlank()) {
            metadata.put("approvalId", decision.approvalId());
        }
        return metadata;
    }
}
