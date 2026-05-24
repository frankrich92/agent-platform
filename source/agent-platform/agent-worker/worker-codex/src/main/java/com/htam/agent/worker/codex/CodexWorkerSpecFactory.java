package com.htam.agent.worker.codex;

import com.htam.agent.worker.coding.CliCodingAgentSpec;
import com.htam.agent.worker.sandbox.WorkerSandboxPolicy;
import java.util.List;

public class CodexWorkerSpecFactory {

    public CliCodingAgentSpec defaultSpec(WorkerSandboxPolicy sandboxPolicy) {
        return new CliCodingAgentSpec("codex", "Codex CLI", "codex",
                List.of("code-edit", "test-run", "repo-inspection"), sandboxPolicy);
    }
}
