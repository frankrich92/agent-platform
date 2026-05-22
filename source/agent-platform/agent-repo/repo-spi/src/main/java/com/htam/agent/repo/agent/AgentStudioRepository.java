package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.AgentStudio;
import java.util.List;

public interface AgentStudioRepository {
    List<AgentStudio> listByStudioIds(List<Long> studioIds);

    AgentStudio getFirstByAgentId(Long agentId);

    List<AgentStudio> listByAgentIds(List<Long> agentIds);

    boolean save(AgentStudio entity);

    boolean deleteByAgentIds(List<Long> agentIds);
}
