package com.htam.agent.common.mcp;

public interface McpRuntimeDegradeRecorder {
    void recordSuccess(Long serverId,
                       Long activationRevision,
                       String configHash,
                       Integer runtimeFailThreshold);

    void recordFailure(Long serverId,
                       Long activationRevision,
                       String configHash,
                       Integer runtimeFailThreshold,
                       Throwable throwable);
}
