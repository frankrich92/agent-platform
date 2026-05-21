package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.SensitiveWordConfig;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.capability.SensitiveWordConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.sensitive.mapper.SensitiveWordConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SensitiveWordConfigMybatisRepository implements SensitiveWordConfigRepository {
    private final SensitiveWordConfigMapper sensitiveWordConfigMapper;

    @Override
    public RepoPage<SensitiveWordConfig> page(PageParams pageParams, String category, String name, Boolean enabled) {
        IPage<SensitiveWordConfig> page = sensitiveWordConfigMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<SensitiveWordConfig>lambdaQuery()
                        .eq(category != null && !category.isBlank(), SensitiveWordConfig::getCategory, category)
                        .like(name != null && !name.isBlank(), SensitiveWordConfig::getName, name)
                        .eq(enabled != null, SensitiveWordConfig::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public SensitiveWordConfig getById(Long id) {
        return sensitiveWordConfigMapper.selectById(id);
    }

    @Override
    public boolean save(SensitiveWordConfig entity) {
        return sensitiveWordConfigMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(SensitiveWordConfig entity) {
        return sensitiveWordConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return sensitiveWordConfigMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public List<String> listCategories() {
        return sensitiveWordConfigMapper.selectList(Wrappers.<SensitiveWordConfig>lambdaQuery()
                        .select(SensitiveWordConfig::getCategory)
                        .isNotNull(SensitiveWordConfig::getCategory)
                        .groupBy(SensitiveWordConfig::getCategory))
                .stream()
                .map(SensitiveWordConfig::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .toList();
    }
}
