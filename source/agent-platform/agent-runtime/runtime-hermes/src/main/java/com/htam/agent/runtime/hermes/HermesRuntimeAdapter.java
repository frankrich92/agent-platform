package com.htam.agent.runtime.hermes;

import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.AgentRuntimeRunner;

public class HermesRuntimeAdapter implements AgentRuntimeRunner {

    private final HermesRuntimeDescriptor descriptor;

    public HermesRuntimeAdapter(HermesRuntimeDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public AgentRunResult run(AgentRunRequest request) {
        if (descriptor == null || !descriptor.enabled()) {
            return new AgentRunResult(request.agentId(), request.runId(), AgentRunStatus.FAILED,
                    "Hermes runtime is not enabled", null, null, null);
        }
        return new AgentRunResult(request.agentId(), request.runId(), AgentRunStatus.ACCEPTED,
                "Hermes runtime dispatch accepted", null, null, null);
    }
}
