package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AgentSkillPackage;
import com.htam.agent.repo.capability.AgentSkillPackageRepository;
import com.htam.agent.repo.mybatis.capability.mapper.AgentSkillPackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AgentSkillPackageMybatisRepository implements AgentSkillPackageRepository {
    private final AgentSkillPackageMapper agentSkillPackageMapper;

    @Override
    public List<AgentSkillPackage> listBySkillPackageIds(List<Long> skillPackageIds) {
        if (skillPackageIds == null || skillPackageIds.isEmpty()) {
            return List.of();
        }
        return agentSkillPackageMapper.selectList(Wrappers.<AgentSkillPackage>lambdaQuery()
                .in(AgentSkillPackage::getSkillPackageId, skillPackageIds));
    }

    @Override
    public List<AgentSkillPackage> listByAgentDefinitionId(Long agentDefinitionId) {
        return agentSkillPackageMapper.selectList(Wrappers.<AgentSkillPackage>lambdaQuery()
                .eq(AgentSkillPackage::getAgentDefinitionId, agentDefinitionId));
    }

    @Override
    public boolean save(AgentSkillPackage agentSkillPackage) {
        return agentSkillPackageMapper.insert(agentSkillPackage) > 0;
    }

    @Override
    public boolean deleteByAgentDefinitionIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentSkillPackageMapper.delete(Wrappers.<AgentSkillPackage>lambdaQuery()
                .in(AgentSkillPackage::getAgentDefinitionId, agentIds)) >= 0;
    }

    @Override
    public boolean deleteBySkillPackageIds(List<Long> skillPackageIds) {
        if (skillPackageIds == null || skillPackageIds.isEmpty()) {
            return true;
        }
        return agentSkillPackageMapper.delete(Wrappers.<AgentSkillPackage>lambdaQuery()
                .in(AgentSkillPackage::getSkillPackageId, skillPackageIds)) >= 0;
    }
}
