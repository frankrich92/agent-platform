package com.htam.agent.capability.skill.service;

import com.htam.agent.common.entity.SkillTool;

import java.util.List;

/**
 * 技能工具关联Service
 *
 * @author huxuehao
 */
public interface SkillToolService {
    /**
     * 获取技能关联的工具ID列表
     *
     * @param skillId 技能ID
     * @return 工具ID列表
     */
    List<Long> getToolIds(Long skillId);

    /**
     * 获取使用指定工具的技能ID列表
     *
     * @param toolIds 工具ID列表
     * @return 技能ID列表
     */
    List<Long> getSkillIds(List<Long> toolIds);

    /**
     * 保存技能工具关联（先删后增）
     *
     * @param skillId 技能ID
     * @param toolIds 工具ID列表
     * @return 是否成功
     */
    Boolean saveSkillTool(Long skillId, List<Long> toolIds);

    /**
     * 删除技能工具关联
     *
     * @param skillIds 技能ID列表
     * @return 是否成功
     */
    Boolean deleteSkillTool(List<Long> skillIds);

    /**
     * 删除指定工具的技能工具关联
     *
     * @param toolIds 工具ID列表
     * @return 是否成功
     */
    Boolean deleteByToolIds(List<Long> toolIds);
}
