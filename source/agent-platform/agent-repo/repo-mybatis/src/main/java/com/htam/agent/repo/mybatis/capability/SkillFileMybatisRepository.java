package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.SkillFile;
import com.htam.agent.repo.capability.SkillFileRepository;
import com.htam.agent.repo.mybatis.capability.mapper.SkillFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SkillFileMybatisRepository implements SkillFileRepository {
    private final SkillFileMapper skillFileMapper;

    @Override
    public List<SkillFile> listBySkillId(Long skillId) {
        if (skillId == null) {
            return List.of();
        }
        return skillFileMapper.selectList(Wrappers.<SkillFile>lambdaQuery()
                .eq(SkillFile::getSkillId, skillId)
                .orderByAsc(SkillFile::getFileType)
                .orderByAsc(SkillFile::getSort)
                .orderByAsc(SkillFile::getFileName));
    }

    @Override
    public SkillFile getById(Long id) {
        return id == null ? null : skillFileMapper.selectById(id);
    }

    @Override
    public SkillFile getBySkillIdAndPath(Long skillId, String filePath) {
        if (skillId == null || filePath == null || filePath.isBlank()) {
            return null;
        }
        return skillFileMapper.selectOne(Wrappers.<SkillFile>lambdaQuery()
                .eq(SkillFile::getSkillId, skillId)
                .eq(SkillFile::getFilePath, filePath));
    }

    @Override
    public boolean save(SkillFile entity) {
        return skillFileMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(SkillFile entity) {
        return skillFileMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id == null || skillFileMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBySkillId(Long skillId) {
        if (skillId == null) {
            return true;
        }
        return skillFileMapper.delete(Wrappers.<SkillFile>lambdaQuery()
                .eq(SkillFile::getSkillId, skillId)) >= 0;
    }

    @Override
    public boolean deleteBySkillIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return true;
        }
        return skillFileMapper.delete(Wrappers.<SkillFile>lambdaQuery()
                .in(SkillFile::getSkillId, skillIds)) >= 0;
    }

    @Override
    public boolean removeBySkillIdAndPath(Long skillId, String filePath) {
        if (skillId == null || filePath == null || filePath.isBlank()) {
            return true;
        }
        return skillFileMapper.delete(Wrappers.<SkillFile>lambdaQuery()
                .eq(SkillFile::getSkillId, skillId)
                .eq(SkillFile::getFilePath, filePath)) >= 0;
    }

    @Override
    public boolean removeBySkillIdAndPathPrefix(Long skillId, String pathPrefix) {
        if (skillId == null || pathPrefix == null || pathPrefix.isBlank()) {
            return true;
        }
        return skillFileMapper.delete(Wrappers.<SkillFile>lambdaQuery()
                .eq(SkillFile::getSkillId, skillId)
                .likeRight(SkillFile::getFilePath, pathPrefix)) >= 0;
    }
}
