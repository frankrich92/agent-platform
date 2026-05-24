package com.htam.agent.worker.qwencode;

import com.htam.agent.worker.coding.CliCodingAgentSpec;
import com.htam.agent.worker.sandbox.WorkerSandboxPolicy;
import java.util.List;

public class QwenCodeWorkerSpecFactory {

    public CliCodingAgentSpec defaultSpec(WorkerSandboxPolicy sandboxPolicy) {
        return new CliCodingAgentSpec("qwen-code", "Qwen Code", "qwen-code",
                List.of("code-edit", "test-run"), sandboxPolicy);
    }
}
