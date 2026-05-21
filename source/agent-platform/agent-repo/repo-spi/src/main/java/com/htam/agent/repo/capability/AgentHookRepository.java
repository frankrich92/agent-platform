package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.AgentHook;

import java.util.List;

public interface AgentHookRepository {
    List<AgentHook> listByHookConfigIds(List<Long> hookIds);

    List<AgentHook> listByAgentDefinitionId(Long agentDefinitionId);

    boolean save(AgentHook agentHook);

    boolean deleteByAgentDefinitionIds(List<Long> agentIds);

    boolean deleteByHookConfigIds(List<Long> hookIds);
}
