package com.htam.agent.runtime.planning;

import java.util.List;

public record FailureRecoveryPlan(
        FailureRecoveryAction action,
        int retryAfterMillis,
        List<String> reasons) {

    public FailureRecoveryPlan {
        action = action == null ? FailureRecoveryAction.FAIL_FAST : action;
        retryAfterMillis = Math.max(0, retryAfterMillis);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
