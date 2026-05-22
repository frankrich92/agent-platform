package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.SystemPromptTemplate;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface SystemPromptTemplateRepository {
    RepoPage<SystemPromptTemplate> page(PageParams pageParams, String category, String name, Boolean enabled);

    SystemPromptTemplate getById(Long id);

    boolean save(SystemPromptTemplate entity);

    boolean updateById(SystemPromptTemplate entity);

    boolean deleteByIds(List<Long> ids);

    List<String> listCategories();
}
