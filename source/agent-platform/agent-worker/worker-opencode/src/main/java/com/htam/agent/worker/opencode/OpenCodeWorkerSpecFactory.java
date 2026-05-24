package com.htam.agent.worker.opencode;

import com.htam.agent.worker.coding.CliCodingAgentSpec;
import com.htam.agent.worker.sandbox.WorkerSandboxPolicy;
import java.util.List;

public class OpenCodeWorkerSpecFactory {

    public CliCodingAgentSpec defaultSpec(WorkerSandboxPolicy sandboxPolicy) {
        return new CliCodingAgentSpec("opencode", "opencode", "opencode",
                List.of("code-edit", "test-run", "tool-use"), sandboxPolicy);
    }
}
