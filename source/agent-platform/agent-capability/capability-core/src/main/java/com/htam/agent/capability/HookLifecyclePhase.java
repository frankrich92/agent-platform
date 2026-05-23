package com.htam.agent.capability;

public enum HookLifecyclePhase {
    BEFORE_RUN,
    BEFORE_MODEL_CALL,
    BEFORE_TOOL_CALL,
    AFTER_TOOL_CALL,
    AFTER_MODEL_CALL,
    AFTER_RUN,
    ON_FAILURE
}
