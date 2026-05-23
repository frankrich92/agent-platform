package com.htam.agent.skill.service.impl;

import com.htam.agent.common.entity.AgentSkillPackage;
import com.htam.agent.repo.capability.AgentSkillPackageRepository;
import com.htam.agent.skill.service.AgentSkillPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体技能包关联Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AgentSkillPackageServiceImpl implements AgentSkillPackageService {
    private final AgentSkillPackageRepository agentSkillPackageRepository;

    @Override
    public List<Long> getAgentIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return List.of();
        }
        return agentSkillPackageRepository.listBySkillPackageIds(skillIds)
                .stream()
                .map(AgentSkillPackage::getAgentDefinitionId)
                .distinct()
                .toList();
    }

    @Override
    public List<Long> getSkillPackageIds(Long agentDefinitionId) {
        return agentSkillPackageRepository.listByAgentDefinitionId(agentDefinitionId)
                .stream()
                .map(AgentSkillPackage::getSkillPackageId)
                .toList();
    }

    @Override
    public Boolean insertAgentSkillPackage(Long agentDefinitionId, List<Long> skillPackageIds) {
        if (skillPackageIds == null || skillPackageIds.isEmpty()) {
            return true;
        }
        skillPackageIds.forEach(skillPackageId -> {
            agentSkillPackageRepository.save(new AgentSkillPackage(null, agentDefinitionId, skillPackageId));
        });

        return true;
    }

    @Override
    public Boolean deleteAgentSkillPackage(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentSkillPackageRepository.deleteByAgentDefinitionIds(agentIds);
    }

    @Override
    public Boolean deleteBySkillPackageIds(List<Long> skillPackageIds) {
        if (skillPackageIds == null || skillPackageIds.isEmpty()) {
            return true;
        }
        return agentSkillPackageRepository.deleteBySkillPackageIds(skillPackageIds);
    }

    @Override
    public Boolean saveAgentSkillPackage(Long agentDefinitionId, List<Long> skillPackageIds) {
        deleteAgentSkillPackage(List.of(agentDefinitionId));
        insertAgentSkillPackage(agentDefinitionId, skillPackageIds);

        return Boolean.TRUE;
    }
}
