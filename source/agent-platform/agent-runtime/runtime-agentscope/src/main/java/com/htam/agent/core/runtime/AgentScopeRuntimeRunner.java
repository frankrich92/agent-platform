package com.htam.agent.core.runtime;

import com.htam.agent.core.agent.IAgentFactory;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRuntimeRunner;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.message.Msg;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AgentScope 运行时适配器。
 */
@Component
@RequiredArgsConstructor
public class AgentScopeRuntimeRunner implements AgentRuntimeRunner {

    private final IAgentFactory agentFactory;

    @Override
    public AgentRunResult run(AgentRunRequest request) {
        Agent agent = agentFactory.getAgent(request.agentId());
        agent.call(Msg.builder()
                .textContent(request.input())
                .build()).block();
        return AgentRunResult.success(request.agentId());
    }
}
