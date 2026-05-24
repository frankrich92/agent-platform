package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.SystemPromptTemplate;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.mybatis.capability.mapper.SystemPromptTemplateMapper;
import com.htam.agent.repo.capability.SystemPromptTemplateRepository;
import com.htam.agent.repo.support.RepoPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SystemPromptTemplateMybatisRepository implements SystemPromptTemplateRepository {
    private final SystemPromptTemplateMapper systemPromptTemplateMapper;

    @Override
    public RepoPage<SystemPromptTemplate> page(PageParams pageParams, String category, String name, Boolean enabled) {
        IPage<SystemPromptTemplate> page = systemPromptTemplateMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<SystemPromptTemplate>lambdaQuery()
                        .eq(category != null && !category.isBlank(), SystemPromptTemplate::getCategory, category)
                        .like(name != null && !name.isBlank(), SystemPromptTemplate::getName, name)
                        .eq(enabled != null, SystemPromptTemplate::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public SystemPromptTemplate getById(Long id) {
        return systemPromptTemplateMapper.selectById(id);
    }

    @Override
    public boolean save(SystemPromptTemplate entity) {
        return systemPromptTemplateMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(SystemPromptTemplate entity) {
        return systemPromptTemplateMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return systemPromptTemplateMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public List<String> listCategories() {
        return systemPromptTemplateMapper.selectList(Wrappers.<SystemPromptTemplate>lambdaQuery()
                        .select(SystemPromptTemplate::getCategory)
                        .isNotNull(SystemPromptTemplate::getCategory)
                        .groupBy(SystemPromptTemplate::getCategory))
                .stream()
                .map(SystemPromptTemplate::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .toList();
    }
}
