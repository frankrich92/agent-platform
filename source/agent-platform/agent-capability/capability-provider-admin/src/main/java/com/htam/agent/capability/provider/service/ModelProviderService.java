package com.htam.agent.capability.provider.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.ModelProviderDTO;
import com.htam.agent.common.entity.ModelProvider;
import com.htam.agent.common.mp.support.PageParams;

import java.util.List;

/**
 * 模型提供商Service
 *
 * @author huxuehao
 */
public interface ModelProviderService {
    IPage<ModelProvider> page(PageParams pageParams, ModelProviderDTO query);

    ModelProvider getById(Long id);

    boolean save(ModelProvider entity);

    List<Object> usedWithModel(List<Long> ids);

    /**
     * 删除模型供应商并触发关联智能体重新注册
     *
     * @param ids 供应商ID列表
     * @return 是否成功
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 更新模型供应商并触发关联智能体重新注册
     *
     * @param entity 模型供应商
     * @return 是否成功
     */
    boolean doUpdate(ModelProvider entity);
}
