package com.htam.agent.repo.provider;

import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;

import java.util.List;

public interface ModelConfigRepository {
    RepoPage<ModelConfig> page(PageParams pageParams, Long providerId, String name, Boolean enabled);

    ModelConfig getById(Long id);

    boolean save(ModelConfig entity);

    boolean updateById(ModelConfig entity);

    boolean deleteByIds(List<Long> ids);

    List<ModelConfig> listByProviderIds(List<Long> providerIds);
}
