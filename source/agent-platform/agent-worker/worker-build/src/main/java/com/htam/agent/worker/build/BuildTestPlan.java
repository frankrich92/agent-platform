package com.htam.agent.worker.build;

import java.nio.file.Path;
import java.util.List;

public record BuildTestPlan(
        Path workspace,
        List<String> commands,
        boolean approvalRequired) {

    public BuildTestPlan {
        commands = commands == null ? List.of() : List.copyOf(commands);
    }
}
