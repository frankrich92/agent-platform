package com.htam.agent.agent.service;

import com.htam.agent.common.entity.AgentSubAgent;

import java.util.List;

/**
 * 智能体子智能体关联Service
 *
 * @author huxuehao
 */
public interface AgentSubAgentService {
    List<Long> getSubAgentIds(Long agentDefinitionId);
    Boolean insertSubAgent(Long agentDefinitionId, List<Long> subAgentIds);
    Boolean deleteSubAgent(List<Long> agentIds);
    Boolean saveSubAgent(Long agentDefinitionId, List<Long> subAgentIds);
}
