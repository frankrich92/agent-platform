package com.htam.agent.capability.tool.hook.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.HookConfigDTO;
import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.wrapper.HookConfigWrapper;

import java.util.List;

/**
 * Hook配置Service
 *
 * @author huxuehao
 */
public interface HookConfigService {
    IPage<HookConfig> page(PageParams pageParams, HookConfigDTO query);

    HookConfig getById(Long id);

    List<HookConfig> listByIds(List<Long> ids);

    boolean save(HookConfig entity);

    void SyncConfigToDatabase(List<HookConfigWrapper> configWrappers);

    List<Object> usedWithAgent(List<Long> ids);

    boolean deleteByIds(List<Long> ids);

    /**
     * 更新Hook配置并触发关联智能体重新注册
     *
     * @param entity Hook配置
     * @return 是否成功
     */
    boolean doUpdate(HookConfig entity);
}
