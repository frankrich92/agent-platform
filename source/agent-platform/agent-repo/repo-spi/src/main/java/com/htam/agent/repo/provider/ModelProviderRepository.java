package com.htam.agent.repo.provider;

import com.htam.agent.common.entity.ModelProvider;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface ModelProviderRepository {
    RepoPage<ModelProvider> page(PageParams pageParams, String name, String type, Boolean enabled);

    ModelProvider getById(Long id);

    boolean save(ModelProvider entity);

    boolean updateById(ModelProvider entity);

    boolean deleteByIds(List<Long> ids);
}
