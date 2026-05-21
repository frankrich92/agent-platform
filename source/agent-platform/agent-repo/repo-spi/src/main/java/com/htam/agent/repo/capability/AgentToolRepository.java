package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.AgentTool;

import java.util.List;

public interface AgentToolRepository {
    List<AgentTool> listByToolIds(List<Long> toolIds);

    List<AgentTool> listByAgentDefinitionId(Long agentDefinitionId);

    boolean save(AgentTool agentTool);

    boolean deleteByAgentDefinitionIds(List<Long> agentIds);

    boolean deleteByToolIds(List<Long> toolIds);
}
