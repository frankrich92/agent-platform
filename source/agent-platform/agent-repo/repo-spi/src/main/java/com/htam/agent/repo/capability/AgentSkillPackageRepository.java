package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.AgentSkillPackage;

import java.util.List;

public interface AgentSkillPackageRepository {
    List<AgentSkillPackage> listBySkillPackageIds(List<Long> skillPackageIds);

    List<AgentSkillPackage> listByAgentDefinitionId(Long agentDefinitionId);

    boolean save(AgentSkillPackage agentSkillPackage);

    boolean deleteByAgentDefinitionIds(List<Long> agentIds);

    boolean deleteBySkillPackageIds(List<Long> skillPackageIds);
}
