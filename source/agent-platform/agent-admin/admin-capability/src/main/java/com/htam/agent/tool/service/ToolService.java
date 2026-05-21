package com.htam.agent.tool.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.ToolDTO;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.wrapper.ToolInfoWrapper;

import java.util.List;

/**
 * 工具Service
 *
 * @author huxuehao
 */
public interface ToolService {
    IPage<ToolConfig> page(PageParams pageParams, ToolDTO query);

    ToolConfig getById(Long id);

    ToolConfig getByToolId(String toolId);

    List<ToolConfig> listByIds(List<Long> ids);

    List<ToolConfig> listEnabledBriefByIds(List<Long> ids);

    boolean save(ToolConfig entity);

    Boolean deleteTools(List<Long> ids);

    void SyncConfigToDatabase(List<ToolInfoWrapper> toolInfos);

    List<Object> usedWithAgent(List<Long> ids);

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<String> listCategories();

    Boolean doUpdate(ToolConfig toolConfig);
}
