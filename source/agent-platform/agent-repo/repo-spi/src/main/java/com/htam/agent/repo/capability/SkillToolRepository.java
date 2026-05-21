package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.SkillTool;

import java.util.List;

public interface SkillToolRepository {
    List<SkillTool> listBySkillId(Long skillId);

    List<SkillTool> listByToolIds(List<Long> toolIds);

    boolean save(SkillTool skillTool);

    boolean deleteBySkillIds(List<Long> skillIds);

    boolean deleteByToolIds(List<Long> toolIds);
}
