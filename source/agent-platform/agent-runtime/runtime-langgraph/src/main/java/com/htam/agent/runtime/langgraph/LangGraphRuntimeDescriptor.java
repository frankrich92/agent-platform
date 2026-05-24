package com.htam.agent.runtime.langgraph;

public record LangGraphRuntimeDescriptor(
        String workerRef,
        boolean enabled,
        boolean longRunningRecoveryEnabled) {

    public LangGraphRuntimeDescriptor {
        workerRef = workerRef == null || workerRef.isBlank() ? "langgraph" : workerRef;
    }
}
