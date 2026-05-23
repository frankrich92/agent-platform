package com.htam.agent.studio.service.impl;

import com.htam.agent.common.entity.AgentStudio;
import com.htam.agent.repo.agent.AgentStudioRepository;
import com.htam.agent.studio.service.AgentStudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述：AgentStudioServiceImpl
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class AgentStudioServiceImpl implements AgentStudioService {
    private final AgentStudioRepository agentStudioRepository;

    @Override
    public List<Long> getAgentIds(List<Long> studioId) {
        if (studioId == null || studioId.isEmpty()) {
            return List.of();
        }
        return agentStudioRepository.listByStudioIds(studioId)
                .stream()
                .map(AgentStudio::getAgentDefinitionId)
                .distinct()
                .toList();
    }

    @Override
    public Long getStudioIdByAgentId(Long agentId) {
        AgentStudio agentStudio = agentStudioRepository.getFirstByAgentId(agentId);
        return agentStudio == null ? null : agentStudio.getStudioId();
    }

    @Override
    public Map<Long, Long> getStudioIdsByAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return Map.of();
        }
        return agentStudioRepository.listByAgentIds(agentIds)
                .stream()
                .collect(Collectors.toMap(
                        AgentStudio::getAgentDefinitionId,
                        AgentStudio::getStudioId,
                        (existing, replacement) -> existing));
    }

    @Override
    public Boolean insertAgentStudio(Long agentDefinitionId, List<Long> studioIds) {
        if (studioIds == null || studioIds.isEmpty()) {
            return false;
        }
        studioIds.forEach(studioId -> {
            agentStudioRepository.save(new AgentStudio(null, agentDefinitionId, studioId));
        });

        return true;
    }

    @Override
    public Boolean deleteAgentStudio(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentStudioRepository.deleteByAgentIds(agentIds);
    }

    @Override
    public Boolean saveAgentStudio(Long agentDefinitionId, List<Long> studioIds) {
        deleteAgentStudio(List.of(agentDefinitionId));
        insertAgentStudio(agentDefinitionId, studioIds);

        return Boolean.TRUE;
    }
}
