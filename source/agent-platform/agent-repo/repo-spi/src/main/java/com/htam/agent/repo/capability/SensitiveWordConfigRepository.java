package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.SensitiveWordConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface SensitiveWordConfigRepository {
    RepoPage<SensitiveWordConfig> page(PageParams pageParams, String category, String name, Boolean enabled);

    SensitiveWordConfig getById(Long id);

    boolean save(SensitiveWordConfig entity);

    boolean updateById(SensitiveWordConfig entity);

    boolean deleteByIds(List<Long> ids);

    List<String> listCategories();
}
