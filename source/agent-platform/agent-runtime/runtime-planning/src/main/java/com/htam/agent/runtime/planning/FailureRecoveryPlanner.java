package com.htam.agent.runtime.planning;

import com.htam.agent.runtime.ToolCall;
import java.util.List;

public class FailureRecoveryPlanner {

    public FailureRecoveryPlan plan(Throwable throwable, List<ToolCall> recentToolCalls, boolean approvalPossible) {
        String message = throwable == null || throwable.getMessage() == null ? "" : throwable.getMessage().toLowerCase();
        List<ToolCall> calls = recentToolCalls == null ? List.of() : List.copyOf(recentToolCalls);
        if (approvalPossible && calls.stream().anyMatch(call -> call.policy().name().equals("ASK"))) {
            return new FailureRecoveryPlan(FailureRecoveryAction.REQUEST_APPROVAL, 0, List.of("approval policy required"));
        }
        if (message.contains("context") || message.contains("token")) {
            return new FailureRecoveryPlan(FailureRecoveryAction.COMPRESS_CONTEXT, 0, List.of("context budget failure"));
        }
        if (message.contains("timeout") || message.contains("connection")) {
            return new FailureRecoveryPlan(FailureRecoveryAction.RETRY, 500, List.of("transient runtime failure"));
        }
        if (calls.stream().anyMatch(ToolCall::failed)) {
            return new FailureRecoveryPlan(FailureRecoveryAction.DEGRADE_TOOL, 0, List.of("tool failed"));
        }
        return new FailureRecoveryPlan(FailureRecoveryAction.FAIL_FAST, 0, List.of("non-recoverable failure"));
    }
}
