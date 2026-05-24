package com.htam.agent.capability.tool.hook.service.impl;

import com.htam.agent.common.entity.AgentHook;
import com.htam.agent.capability.tool.hook.service.AgentHookService;
import com.htam.agent.repo.capability.AgentHookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体Hook关联Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AgentHookServiceImpl implements AgentHookService {
    private final AgentHookRepository agentHookRepository;

    @Override
    public List<Long> getAgentIds(List<Long> hookIds) {
        if (hookIds == null || hookIds.isEmpty()) {
            return List.of();
        }
        return agentHookRepository.listByHookConfigIds(hookIds)
                .stream()
                .map(AgentHook::getAgentDefinitionId)
                .distinct()
                .toList();
    }

    @Override
    public List<Long> getHookIds(Long agentDefinitionId) {
        return agentHookRepository.listByAgentDefinitionId(agentDefinitionId)
                .stream()
                .map(AgentHook::getHookConfigId)
                .toList();
    }

    @Override
    public Boolean insertAgentHook(Long agentDefinitionId, List<Long> hookIds) {
        if (hookIds == null || hookIds.isEmpty()) {
            return true;
        }
        hookIds.forEach(hookId -> {
            agentHookRepository.save(new AgentHook(null, agentDefinitionId, hookId));
        });
        return true;
    }

    @Override
    public Boolean deleteAgentHook(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentHookRepository.deleteByAgentDefinitionIds(agentIds);
    }

    @Override
    public Boolean deleteByHookConfigIds(List<Long> hookIds) {
        if (hookIds == null || hookIds.isEmpty()) {
            return true;
        }
        return agentHookRepository.deleteByHookConfigIds(hookIds);
    }

    @Override
    public Boolean saveAgentHook(Long agentDefinitionId, List<Long> hookIds) {
        deleteAgentHook(List.of(agentDefinitionId));
        insertAgentHook(agentDefinitionId, hookIds);

        return Boolean.TRUE;
    }
}
