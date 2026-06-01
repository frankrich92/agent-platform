package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.SkillFile;

import java.util.List;

public interface SkillFileRepository {
    List<SkillFile> listBySkillId(Long skillId);

    SkillFile getById(Long id);

    SkillFile getBySkillIdAndPath(Long skillId, String filePath);

    boolean save(SkillFile entity);

    boolean updateById(SkillFile entity);

    boolean deleteById(Long id);

    boolean deleteBySkillId(Long skillId);

    boolean deleteBySkillIds(List<Long> skillIds);

    boolean removeBySkillIdAndPath(Long skillId, String filePath);

    boolean removeBySkillIdAndPathPrefix(Long skillId, String pathPrefix);
}
