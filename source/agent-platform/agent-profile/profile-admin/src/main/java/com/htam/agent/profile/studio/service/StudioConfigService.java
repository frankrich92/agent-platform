package com.htam.agent.profile.studio.service;

import com.htam.agent.common.entity.StudioConfig;

import java.util.List;

/**
 * 描述：StudioConfigService
 *
 * @author huxuehao
 **/
public interface StudioConfigService {
    List<StudioConfig> list();
    StudioConfig getById(Long id);
    Boolean save(StudioConfig entity);
    Boolean updateById(StudioConfig entity);
    Boolean removeByIds(List<Long> ids);
    List<Object> usedWithAgent(List<Long> ids);
}
