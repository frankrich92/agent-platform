package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.AgentSubAgent;
import java.util.List;

public interface AgentSubAgentRepository {
    List<AgentSubAgent> listByParentAgentId(Long agentDefinitionId);

    boolean save(AgentSubAgent entity);

    boolean deleteByParentAgentIds(List<Long> agentIds);
}
