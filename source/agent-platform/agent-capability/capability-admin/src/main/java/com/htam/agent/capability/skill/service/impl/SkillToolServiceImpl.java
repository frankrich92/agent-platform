package com.htam.agent.capability.skill.service.impl;

import com.htam.agent.common.entity.SkillTool;
import com.htam.agent.repo.capability.SkillToolRepository;
import com.htam.agent.capability.skill.service.SkillToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 技能工具关联Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class SkillToolServiceImpl implements SkillToolService {
    private final SkillToolRepository skillToolRepository;

    @Override
    public List<Long> getToolIds(Long skillId) {
        return skillToolRepository.listBySkillId(skillId)
                .stream()
                .map(SkillTool::getToolId)
                .toList();
    }

    @Override
    public List<Long> getSkillIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return skillToolRepository.listByToolIds(toolIds)
                .stream()
                .map(SkillTool::getSkillId)
                .distinct()
                .toList();
    }

    @Override
    public Boolean saveSkillTool(Long skillId, List<Long> toolIds) {
        deleteSkillTool(List.of(skillId));
        if (toolIds != null && !toolIds.isEmpty()) {
            toolIds.forEach(toolId -> {
                skillToolRepository.save(new SkillTool(null, skillId, toolId));
            });
        }
        return true;
    }

    @Override
    public Boolean deleteSkillTool(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return true;
        }
        return skillToolRepository.deleteBySkillIds(skillIds);
    }

    @Override
    public Boolean deleteByToolIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return true;
        }
        return skillToolRepository.deleteByToolIds(toolIds);
    }
}
