package com.htam.agent.worker;

import java.time.Duration;
import java.util.List;

public record WorkerSandboxPolicy(
        boolean networkEnabled,
        boolean fileWriteEnabled,
        Duration timeout,
        List<String> allowedCommands,
        WorkerRiskPolicy defaultRiskPolicy) {

    public WorkerSandboxPolicy {
        timeout = timeout == null ? Duration.ofMinutes(5) : timeout;
        allowedCommands = allowedCommands == null ? List.of() : List.copyOf(allowedCommands);
        defaultRiskPolicy = defaultRiskPolicy == null ? WorkerRiskPolicy.ASK : defaultRiskPolicy;
    }

    public static WorkerSandboxPolicy lockedDown() {
        return new WorkerSandboxPolicy(false, false, Duration.ofMinutes(5), List.of(), WorkerRiskPolicy.ASK);
    }
}
