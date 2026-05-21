package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.SkillTool;
import com.htam.agent.repo.capability.SkillToolRepository;
import com.htam.agent.skill.mapper.SkillToolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SkillToolMybatisRepository implements SkillToolRepository {
    private final SkillToolMapper skillToolMapper;

    @Override
    public List<SkillTool> listBySkillId(Long skillId) {
        return skillToolMapper.selectList(Wrappers.<SkillTool>lambdaQuery()
                .eq(SkillTool::getSkillId, skillId));
    }

    @Override
    public List<SkillTool> listByToolIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return skillToolMapper.selectList(Wrappers.<SkillTool>lambdaQuery()
                .in(SkillTool::getToolId, toolIds));
    }

    @Override
    public boolean save(SkillTool skillTool) {
        return skillToolMapper.insert(skillTool) > 0;
    }

    @Override
    public boolean deleteBySkillIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return true;
        }
        return skillToolMapper.delete(Wrappers.<SkillTool>lambdaQuery()
                .in(SkillTool::getSkillId, skillIds)) >= 0;
    }

    @Override
    public boolean deleteByToolIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return true;
        }
        return skillToolMapper.delete(Wrappers.<SkillTool>lambdaQuery()
                .in(SkillTool::getToolId, toolIds)) >= 0;
    }
}
