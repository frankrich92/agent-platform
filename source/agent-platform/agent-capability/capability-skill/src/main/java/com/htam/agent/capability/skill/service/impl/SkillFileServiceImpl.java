package com.htam.agent.capability.skill.service.impl;

import com.htam.agent.capability.skill.service.SkillFileService;
import com.htam.agent.common.entity.SkillFile;
import com.htam.agent.repo.capability.SkillFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillFileServiceImpl implements SkillFileService {
    private final SkillFileRepository skillFileRepository;

    @Override
    public List<SkillFile> listBySkillId(Long skillId) {
        return skillFileRepository.listBySkillId(skillId);
    }

    @Override
    public SkillFile getById(Long id) {
        return skillFileRepository.getById(id);
    }

    @Override
    public SkillFile getBySkillIdAndPath(Long skillId, String filePath) {
        return skillFileRepository.getBySkillIdAndPath(skillId, filePath);
    }

    @Override
    public boolean save(SkillFile entity) {
        return skillFileRepository.save(entity);
    }

    @Override
    public boolean updateById(SkillFile entity) {
        return skillFileRepository.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContent(Long fileId, String content) {
        SkillFile file = new SkillFile();
        file.setId(fileId);
        file.setContent(content);
        return skillFileRepository.updateById(file);
    }

    @Override
    public boolean deleteById(Long id) {
        return skillFileRepository.deleteById(id);
    }

    @Override
    public boolean deleteBySkillId(Long skillId) {
        return skillFileRepository.deleteBySkillId(skillId);
    }

    @Override
    public boolean deleteBySkillIds(List<Long> skillIds) {
        return skillFileRepository.deleteBySkillIds(skillIds);
    }

    @Override
    public boolean removeBySkillIdAndPath(Long skillId, String filePath) {
        return skillFileRepository.removeBySkillIdAndPath(skillId, filePath);
    }

    @Override
    public boolean removeBySkillIdAndPathPrefix(Long skillId, String pathPrefix) {
        return skillFileRepository.removeBySkillIdAndPathPrefix(skillId, pathPrefix);
    }
}
