package com.htam.agent.repo.mybatis.provider;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.mybatis.provider.mapper.ModelConfigMapper;
import com.htam.agent.repo.provider.ModelConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ModelConfigMybatisRepository implements ModelConfigRepository {
    private final ModelConfigMapper modelConfigMapper;

    @Override
    public RepoPage<ModelConfig> page(PageParams pageParams, Long providerId, String name, Boolean enabled) {
        IPage<ModelConfig> page = modelConfigMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<ModelConfig>lambdaQuery()
                        .eq(providerId != null, ModelConfig::getProviderId, providerId)
                        .like(name != null && !name.isBlank(), ModelConfig::getName, name)
                        .eq(enabled != null, ModelConfig::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public ModelConfig getById(Long id) {
        return modelConfigMapper.selectById(id);
    }

    @Override
    public boolean save(ModelConfig entity) {
        return modelConfigMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(ModelConfig entity) {
        return modelConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return modelConfigMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public List<ModelConfig> listByProviderIds(List<Long> providerIds) {
        if (providerIds == null || providerIds.isEmpty()) {
            return List.of();
        }
        return modelConfigMapper.selectList(Wrappers.<ModelConfig>lambdaQuery()
                .in(ModelConfig::getProviderId, providerIds));
    }
}
