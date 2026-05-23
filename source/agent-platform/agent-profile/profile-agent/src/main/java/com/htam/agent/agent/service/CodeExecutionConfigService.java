package com.htam.agent.agent.service;

import com.htam.agent.common.entity.CodeExecutionConfig;

import java.util.List;

/**
 * 描述：CodeExecutionConfigService
 *
 * @author huxuehao
 **/
public interface CodeExecutionConfigService {
    List<CodeExecutionConfig> list();

    CodeExecutionConfig getById(Long id);

    boolean save(CodeExecutionConfig entity);

    boolean updateById(CodeExecutionConfig entity);

    boolean removeByIds(List<Long> ids);

    /**
     * 查询被哪些Agent使用
     *
     * @param ids 配置ID列表
     * @return Agent名称列表
     */
    List<Object> usedWithAgent(List<Long> ids);
}
