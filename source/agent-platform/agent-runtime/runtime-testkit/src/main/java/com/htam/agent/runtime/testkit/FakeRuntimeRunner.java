package com.htam.agent.runtime.testkit;

import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.AgentRuntimeRunner;
import java.util.function.Function;

public class FakeRuntimeRunner implements AgentRuntimeRunner {

    private final Function<AgentRunRequest, AgentRunResult> delegate;

    public FakeRuntimeRunner(Function<AgentRunRequest, AgentRunResult> delegate) {
        this.delegate = delegate;
    }

    @Override
    public AgentRunResult run(AgentRunRequest request) {
        if (delegate != null) {
            return delegate.apply(request);
        }
        return new AgentRunResult(request.agentId(), request.runId(), AgentRunStatus.SUCCEEDED,
                "fake runtime completed", null, null, null);
    }
}
