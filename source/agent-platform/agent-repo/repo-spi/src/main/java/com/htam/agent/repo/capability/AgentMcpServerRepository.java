package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.AgentMcpServer;

import java.util.List;

public interface AgentMcpServerRepository {
    List<AgentMcpServer> listByMcpServerIds(List<Long> mcpServerIds);

    List<AgentMcpServer> listByAgentDefinitionId(Long agentDefinitionId);

    boolean save(AgentMcpServer entity);

    boolean deleteByAgentDefinitionIds(List<Long> agentIds);

    boolean deleteByMcpServerIds(List<Long> mcpServerIds);
}
