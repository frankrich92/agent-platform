package com.htam.agent.capability.skill.service;

import com.htam.agent.common.entity.AgentSkillPackage;

import java.util.List;

/**
 * 智能体技能包关联Service
 *
 * @author huxuehao
 */
public interface AgentSkillPackageService {
    List<Long> getAgentIds(List<Long> skillIds);

    List<Long> getSkillPackageIds(Long agentDefinitionId);

    Boolean insertAgentSkillPackage(Long agentDefinitionId, List<Long> skillPackageIds);

    Boolean deleteAgentSkillPackage(List<Long> agentIds);

    Boolean deleteBySkillPackageIds(List<Long> skillPackageIds);

    Boolean saveAgentSkillPackage(Long agentDefinitionId, List<Long> skillPackageIds);
}
