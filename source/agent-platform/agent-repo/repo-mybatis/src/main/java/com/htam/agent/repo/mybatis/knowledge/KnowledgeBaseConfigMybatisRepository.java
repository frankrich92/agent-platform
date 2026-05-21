package com.htam.agent.repo.mybatis.knowledge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.knowledge.mapper.KnowledgeBaseConfigMapper;
import com.htam.agent.repo.knowledge.KnowledgeBaseConfigRepository;
import com.htam.agent.repo.support.RepoPage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class KnowledgeBaseConfigMybatisRepository implements KnowledgeBaseConfigRepository {
    private final KnowledgeBaseConfigMapper knowledgeBaseConfigMapper;

    @Override
    public RepoPage<KnowledgeBaseConfig> page(PageParams pageParams, String name, KbType kbType, Boolean enabled) {
        IPage<KnowledgeBaseConfig> page = knowledgeBaseConfigMapper.selectPage(MP.getPage(pageParams),
                Wrappers.<KnowledgeBaseConfig>lambdaQuery()
                        .like(name != null && !name.isBlank(), KnowledgeBaseConfig::getName, name)
                        .eq(kbType != null, KnowledgeBaseConfig::getKbType, kbType)
                        .eq(enabled != null, KnowledgeBaseConfig::getEnabled, enabled));
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public KnowledgeBaseConfig getById(Long id) {
        return knowledgeBaseConfigMapper.selectById(id);
    }

    @Override
    public List<KnowledgeBaseConfig> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return knowledgeBaseConfigMapper.selectBatchIds(ids);
    }

    @Override
    public boolean save(KnowledgeBaseConfig entity) {
        return knowledgeBaseConfigMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(KnowledgeBaseConfig entity) {
        return knowledgeBaseConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return knowledgeBaseConfigMapper.deleteBatchIds(ids) >= 0;
    }
}
