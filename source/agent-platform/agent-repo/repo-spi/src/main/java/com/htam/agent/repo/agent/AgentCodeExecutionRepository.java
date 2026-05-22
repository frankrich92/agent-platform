package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.AgentCodeExecution;
import java.util.List;

public interface AgentCodeExecutionRepository {
    List<AgentCodeExecution> listByCodeExecutionIds(List<Long> codeExecutionIds);

    AgentCodeExecution getByAgentId(Long agentId);

    boolean save(AgentCodeExecution entity);

    boolean deleteByAgentIds(List<Long> agentIds);
}
