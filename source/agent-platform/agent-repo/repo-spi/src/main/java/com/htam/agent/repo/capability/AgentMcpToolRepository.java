package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.AgentMcpTool;

import java.util.List;

public interface AgentMcpToolRepository {
    List<AgentMcpTool> listByAgentDefinitionId(Long agentDefinitionId);

    boolean save(AgentMcpTool entity);

    boolean deleteByAgentDefinitionIds(List<Long> agentIds);

    boolean deleteByMcpToolIds(List<Long> mcpToolIds);
}
