package com.htam.agent.capability.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.KnowledgeBaseConfigDTO;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.mp.support.PageParams;

import java.util.List;

/**
 * 知识库配置Service
 *
 * @author huxuehao
 */
public interface KnowledgeBaseConfigService {
    IPage<KnowledgeBaseConfig> page(PageParams pageParams, KnowledgeBaseConfigDTO query);

    KnowledgeBaseConfig getById(Long id);

    boolean save(KnowledgeBaseConfig entity);

    List<Object> usedWithAgent(List<Long> ids);

    KnowledgeBaseConfig getByAgentId(Long agentId);

    boolean deleteByIds(List<Long> ids);

    /**
     * 更新知识库配置并触发关联智能体重新注册
     *
     * @param entity 知识库配置
     * @return 是否成功
     */
    boolean doUpdate(KnowledgeBaseConfig entity);
}
