package com.htam.agent.core.runtime;

import com.htam.agent.core.agent.IAgentFactory;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRuntimeRunner;
import com.htam.agent.runtime.RuntimeEvent;
import com.htam.agent.runtime.RuntimeEventType;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.message.Msg;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * AgentScope 运行时适配器。
 */
@Component
@RequiredArgsConstructor
public class AgentScopeRuntimeRunner implements AgentRuntimeRunner {

    private final IAgentFactory agentFactory;

    @Override
    public AgentRunResult run(AgentRunRequest request) {
        String runId = request.runId() == null || request.runId().isBlank()
                ? UUID.randomUUID().toString()
                : request.runId();
        RuntimeEvent started = RuntimeEvent.of(UUID.randomUUID().toString(),
                RuntimeEventType.RUN_STARTED, runId, 1);
        try {
            Agent agent = agentFactory.getAgent(request.agentId());
            agent.call(Msg.builder()
                    .textContent(request.input())
                    .build()).block();
            RuntimeEvent completed = RuntimeEvent.of(UUID.randomUUID().toString(),
                    RuntimeEventType.RUN_COMPLETED, runId, 2);
            return AgentRunResult.success(request.agentId(), runId, null, List.of(started, completed));
        } catch (RuntimeException ex) {
            RuntimeEvent failed = RuntimeEvent.of(UUID.randomUUID().toString(),
                    RuntimeEventType.RUN_FAILED, runId, 2);
            return AgentRunResult.failure(request.agentId(), runId, ex.getMessage(), List.of(started, failed));
        }
    }
}
