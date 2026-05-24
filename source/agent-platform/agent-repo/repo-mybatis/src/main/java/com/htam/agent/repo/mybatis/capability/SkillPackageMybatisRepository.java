package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.capability.SkillPackageRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.repo.mybatis.capability.mapper.SkillPackageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SkillPackageMybatisRepository implements SkillPackageRepository {
    private final SkillPackageMapper skillPackageMapper;

    @Override
    public RepoPage<SkillPackage> page(PageParams pageParams, String name, String category, Boolean enabled) {
        IPage<SkillPackage> page = skillPackageMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<SkillPackage>lambdaQuery()
                        .like(name != null && !name.isBlank(), SkillPackage::getName, name)
                        .eq(category != null && !category.isBlank(), SkillPackage::getCategory, category)
                        .eq(enabled != null, SkillPackage::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public SkillPackage getById(Long id) {
        return skillPackageMapper.selectById(id);
    }

    @Override
    public SkillPackage getByName(String name) {
        return skillPackageMapper.selectOne(Wrappers.<SkillPackage>lambdaQuery()
                .eq(SkillPackage::getName, name));
    }

    @Override
    public List<SkillPackage> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return skillPackageMapper.selectBatchIds(ids);
    }

    @Override
    public List<SkillPackage> listEnabledBriefByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return skillPackageMapper.selectList(Wrappers.<SkillPackage>lambdaQuery()
                .select(SkillPackage::getId, SkillPackage::getName, SkillPackage::getDescription)
                .eq(SkillPackage::getEnabled, true)
                .in(SkillPackage::getId, ids));
    }

    @Override
    public List<SkillPackage> listWithScripts() {
        return skillPackageMapper.selectList(Wrappers.<SkillPackage>lambdaQuery()
                .ne(SkillPackage::getScripts, "[]"));
    }

    @Override
    public boolean save(SkillPackage entity) {
        return skillPackageMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(SkillPackage entity) {
        return skillPackageMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return skillPackageMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public List<String> listCategories() {
        return skillPackageMapper.selectList(Wrappers.<SkillPackage>lambdaQuery()
                        .select(SkillPackage::getCategory)
                        .isNotNull(SkillPackage::getCategory)
                        .groupBy(SkillPackage::getCategory))
                .stream()
                .map(SkillPackage::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .toList();
    }
}
