package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.AgentA2A;
import java.util.List;

public interface AgentA2aRepository {
    AgentA2A getByAgentId(Long agentId);

    boolean save(AgentA2A entity);

    boolean deleteByAgentIds(List<Long> agentIds);
}
