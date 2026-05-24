package com.htam.agent.repo.mybatis.provider;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.ModelProvider;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.mybatis.provider.mapper.ModelProviderMapper;
import com.htam.agent.repo.provider.ModelProviderRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ModelProviderMybatisRepository implements ModelProviderRepository {
    private final ModelProviderMapper modelProviderMapper;

    @Override
    public RepoPage<ModelProvider> page(PageParams pageParams, String name, String type, Boolean enabled) {
        IPage<ModelProvider> page = modelProviderMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<ModelProvider>lambdaQuery()
                        .like(name != null && !name.isBlank(), ModelProvider::getName, name)
                        .eq(type != null && !type.isBlank(), ModelProvider::getType, type)
                        .eq(enabled != null, ModelProvider::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public ModelProvider getById(Long id) {
        return modelProviderMapper.selectById(id);
    }

    @Override
    public boolean save(ModelProvider entity) {
        return modelProviderMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(ModelProvider entity) {
        return modelProviderMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return modelProviderMapper.deleteBatchIds(ids) > 0;
    }
}
