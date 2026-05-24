package com.htam.agent.capability.tool.hook.service;

import com.htam.agent.common.entity.AgentHook;

import java.util.List;

/**
 * 智能体Hook关联Service
 *
 * @author huxuehao
 */
public interface AgentHookService {
    List<Long> getAgentIds(List<Long> hookIds);

    List<Long> getHookIds(Long agentDefinitionId);

    Boolean insertAgentHook(Long agentDefinitionId, List<Long> hookIds);

    Boolean deleteAgentHook(List<Long> agentIds);

    Boolean deleteByHookConfigIds(List<Long> hookIds);

    Boolean saveAgentHook(Long agentDefinitionId, List<Long> hookIds);
}
