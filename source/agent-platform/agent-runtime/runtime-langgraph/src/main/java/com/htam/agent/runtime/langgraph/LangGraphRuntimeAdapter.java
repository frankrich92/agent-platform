package com.htam.agent.runtime.langgraph;

import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.AgentRuntimeRunner;

public class LangGraphRuntimeAdapter implements AgentRuntimeRunner {

    private final LangGraphRuntimeDescriptor descriptor;

    public LangGraphRuntimeAdapter(LangGraphRuntimeDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public AgentRunResult run(AgentRunRequest request) {
        if (descriptor == null || !descriptor.enabled()) {
            return new AgentRunResult(request.agentId(), request.runId(), AgentRunStatus.FAILED,
                    "LangGraph runtime worker is not enabled", null, null, null);
        }
        return new AgentRunResult(request.agentId(), request.runId(), AgentRunStatus.ACCEPTED,
                "LangGraph worker dispatch accepted: " + descriptor.workerRef(), null, null, null);
    }
}
