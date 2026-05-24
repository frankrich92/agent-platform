package com.htam.agent.capability.provider.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.ModelConfigDTO;
import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.wrapper.ModelWrapper;

import java.util.List;

/**
 * 模型配置Service
 *
 * @author huxuehao
 */
public interface ModelConfigService {
    IPage<ModelConfig> page(PageParams pageParams, ModelConfigDTO query);

    ModelConfig getById(Long id);

    boolean save(ModelConfig entity);

    ModelWrapper getModelWrapperById(Long id);

    List<Object> usedWithAgent(List<Long> ids);

    /**
     * 删除模型配置并触发关联智能体重新注册
     *
     * @param ids 模型配置ID列表
     * @return 是否成功
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 更新模型配置并触发关联智能体重新注册
     *
     * @param entity 模型配置
     * @return 是否成功
     */
    boolean doUpdate(ModelConfig entity);
}
