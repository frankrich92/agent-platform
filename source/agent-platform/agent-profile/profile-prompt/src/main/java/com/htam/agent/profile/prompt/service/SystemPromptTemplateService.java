package com.htam.agent.profile.prompt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.SystemPromptTemplateDTO;
import com.htam.agent.common.entity.SystemPromptTemplate;
import com.htam.agent.common.mp.support.PageParams;

import java.util.List;

/**
 * 系统提示词模板Service
 *
 * @author huxuehao
 */
public interface SystemPromptTemplateService {
    IPage<SystemPromptTemplate> page(PageParams pageParams, SystemPromptTemplateDTO query);

    SystemPromptTemplate getById(Long id);

    boolean save(SystemPromptTemplate entity);

    List<Object> usedWithAgent(List<Long> ids);

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<String> listCategories();

    /**
     * 删除提示词模板并触发关联智能体重新注册
     *
     * @param ids 模板ID列表
     * @return 是否成功
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 更新提示词模板并触发关联智能体重新注册
     *
     * @param entity 提示词模板
     * @return 是否成功
     */
    boolean doUpdate(SystemPromptTemplate entity);
}
