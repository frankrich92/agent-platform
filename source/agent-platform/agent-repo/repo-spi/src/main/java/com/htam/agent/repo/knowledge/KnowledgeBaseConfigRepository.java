package com.htam.agent.repo.knowledge;

import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.enums.KbType;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;
import java.util.List;

public interface KnowledgeBaseConfigRepository {
    RepoPage<KnowledgeBaseConfig> page(PageParams pageParams, String name, KbType kbType, Boolean enabled);

    KnowledgeBaseConfig getById(Long id);

    List<KnowledgeBaseConfig> listByIds(List<Long> ids);

    boolean save(KnowledgeBaseConfig entity);

    boolean updateById(KnowledgeBaseConfig entity);

    boolean deleteByIds(List<Long> ids);
}
