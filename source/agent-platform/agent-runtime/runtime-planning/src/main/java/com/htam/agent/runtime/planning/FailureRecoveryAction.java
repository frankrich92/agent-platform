package com.htam.agent.runtime.planning;

public enum FailureRecoveryAction {
    RETRY,
    COMPRESS_CONTEXT,
    DEGRADE_TOOL,
    REQUEST_APPROVAL,
    HANDOFF_TO_HUMAN,
    FAIL_FAST
}
