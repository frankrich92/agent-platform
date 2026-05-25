package com.htam.agent.worker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.governance.approval.InMemoryApprovalLedger;
import com.htam.agent.governance.audit.InMemoryAuditLedger;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicyResolver;
import com.htam.agent.worker.spi.WorkerRiskPolicy;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskStatus;
import com.htam.agent.worker.spi.WorkerTaskType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GovernedWorkerRuntimeTest {

    @Test
    void submitsWorkerTaskThroughPolicyAndLedger() {
        InMemoryWorkerTaskLedger ledger = new InMemoryWorkerTaskLedger();
        GovernedWorkerRuntime runtime = new GovernedWorkerRuntime(ledger);
        WorkerTask task = new WorkerTask("task-1", WorkerTaskType.SHELL, "workspace-1",
                "mvn test", WorkerRiskPolicy.ASK, Map.of());

        assertEquals(WorkerTaskStatus.APPROVAL_REQUIRED, runtime.submit(task).status());
        assertTrue(ledger.findTask("task-1").isPresent());
        assertTrue(ledger.findResult("task-1").isPresent());
    }

    @Test
    void submitsWorkerTaskThroughGovernanceGuard() {
        InMemoryWorkerTaskLedger ledger = new InMemoryWorkerTaskLedger();
        InMemoryApprovalLedger approvals = new InMemoryApprovalLedger();
        InMemoryAuditLedger audit = new InMemoryAuditLedger();
        GovernanceGuard guard = new GovernanceGuard(new LayeredRiskPolicyResolver(), approvals, audit);
        GovernedWorkerRuntime runtime = new GovernedWorkerRuntime(ledger, guard);
        WorkerTask task = new WorkerTask("task-2", WorkerTaskType.SHELL, "workspace-1",
                "mvn test", WorkerRiskPolicy.ASK, Map.of("traceId", "trace-1"));

        assertEquals(WorkerTaskStatus.APPROVAL_REQUIRED, runtime.submit(task).status());
        assertEquals(1, approvals.listPendingByRunId("task-2").size());
        assertEquals(1, audit.listByTraceId("trace-1").size());
        assertTrue(ledger.findResult("task-2").orElseThrow().metadata().containsKey("approvalId"));
    }

    @Test
    void deniesWorkerTaskThroughGovernanceGuard() {
        GovernedWorkerRuntime runtime = new GovernedWorkerRuntime(
                new InMemoryWorkerTaskLedger(),
                new GovernanceGuard(
                        new LayeredRiskPolicyResolver(),
                        new InMemoryApprovalLedger(),
                        new InMemoryAuditLedger()));
        WorkerTask task = new WorkerTask("task-3", WorkerTaskType.SHELL, "workspace-1",
                "rm -rf workspace", WorkerRiskPolicy.DENY, Map.of());

        assertEquals(WorkerTaskStatus.DENIED, runtime.submit(task).status());
    }
}
