package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface SkillPackageRepository {
    RepoPage<SkillPackage> page(PageParams pageParams, String name, String category, Boolean enabled);

    SkillPackage getById(Long id);

    SkillPackage getByName(String name);

    List<SkillPackage> listByIds(List<Long> ids);

    List<SkillPackage> listAll();

    List<SkillPackage> listEnabledBriefByIds(List<Long> ids);

    List<SkillPackage> listWithScripts();

    boolean save(SkillPackage entity);

    boolean updateById(SkillPackage entity);

    boolean deleteByIds(List<Long> ids);

    List<String> listCategories();
}
