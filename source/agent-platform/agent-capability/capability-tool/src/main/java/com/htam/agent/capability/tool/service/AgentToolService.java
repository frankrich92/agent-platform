package com.htam.agent.capability.tool.service;

import com.htam.agent.common.entity.AgentTool;

import java.util.List;

/**
 * 智能体工具关联Service
 *
 * @author huxuehao
 */
public interface AgentToolService {
    List<Long> getAgentIds(List<Long> tools);

    List<Long> getToolIds(Long agentDefinitionId);

    Boolean insertAgentTool(Long agentDefinitionId, List<Long> toolIds);

    Boolean deleteAgentTool(List<Long> agentIds);

    Boolean deleteByToolIds(List<Long> toolIds);

    Boolean saveAgentTool(Long agentDefinitionId, List<Long> toolIds);
}
