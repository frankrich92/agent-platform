package com.htam.agent.agent.service.impl;

import com.htam.agent.agent.service.AgentSubAgentService;
import com.htam.agent.common.entity.AgentSubAgent;
import com.htam.agent.repo.agent.AgentSubAgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体子智能体关联Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AgentSubAgentServiceImpl implements AgentSubAgentService {
    private final AgentSubAgentRepository agentSubAgentRepository;

    @Override
    public List<Long> getSubAgentIds(Long agentDefinitionId) {
        return agentSubAgentRepository.listByParentAgentId(agentDefinitionId)
                .stream()
                .map(AgentSubAgent::getSubAgentId).toList();
    }

    @Override
    public Boolean insertSubAgent(Long agentDefinitionId, List<Long> subAgentIds) {
        if (subAgentIds == null || subAgentIds.isEmpty()) {
            return Boolean.TRUE;
        }
        subAgentIds.forEach(subAgentId -> {
            agentSubAgentRepository.save(new AgentSubAgent(null, agentDefinitionId, subAgentId));
        });

        return true;
    }

    @Override
    public Boolean deleteSubAgent(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentSubAgentRepository.deleteByParentAgentIds(agentIds);
    }

    @Override
    public Boolean saveSubAgent(Long agentDefinitionId, List<Long> subAgentIds) {
        deleteSubAgent(List.of(agentDefinitionId));
        insertSubAgent(agentDefinitionId, subAgentIds);

        return Boolean.TRUE;
    }
}
