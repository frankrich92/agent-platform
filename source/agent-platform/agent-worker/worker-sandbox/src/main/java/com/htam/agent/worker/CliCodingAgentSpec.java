package com.htam.agent.worker;

import java.util.List;

public record CliCodingAgentSpec(
        String id,
        String displayName,
        String command,
        List<String> supportedSkills,
        WorkerSandboxPolicy sandboxPolicy) {

    public CliCodingAgentSpec {
        supportedSkills = supportedSkills == null ? List.of() : List.copyOf(supportedSkills);
        sandboxPolicy = sandboxPolicy == null ? WorkerSandboxPolicy.lockedDown() : sandboxPolicy;
    }
}
