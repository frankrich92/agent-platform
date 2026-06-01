package com.htam.agent.capability.skill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.common.dto.SkillPackageDTO;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.vo.SkillPackageVO;

import java.util.List;

/**
 * 技能包Service
 *
 * @author huxuehao
 */
public interface SkillPackageService {
    IPage<SkillPackage> page(PageParams pageParams, SkillPackageDTO query);

    SkillPackage getById(Long id);

    SkillPackage getByName(String name);

    List<SkillPackage> listByIds(List<Long> ids);

    List<SkillPackage> listAll();

    List<SkillPackage> listEnabledBriefByIds(List<Long> ids);

    List<SkillPackage> listWithScripts();

    boolean save(SkillPackage entity);

    boolean updateById(SkillPackage entity);

    List<Object> usedWithAgent(List<Long> ids);

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<String> listCategories();

    boolean deleteByIds(List<Long> ids);

    /**
     * 更新技能包并触发关联智能体重新注册
     *
     * @param entity 技能包
     * @return 是否成功
     */
    boolean doUpdate(SkillPackage entity);

    /**
     * 获取技能包详情（包含关联的工具ID列表）
     *
     * @param id 技能包ID
     * @return 技能包VO
     */
    SkillPackageVO getDetail(Long id);
}
