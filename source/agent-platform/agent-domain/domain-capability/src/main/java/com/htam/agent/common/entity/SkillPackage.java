package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainTable;
import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.consts.TableConst;
import lombok.Getter;
import lombok.Setter;

/**
 * 技能包
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(value = TableConst.SKILL, autoResultMap = true)
public class SkillPackage extends BaseEntity {

    /**
     * 技能包名称
     */
    private String name;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 技能内容（概述）
     */
    private String skillContent;

    /**
     * 技能分类
     */
    private String category;

    /**
     * 资源列表
     */
    @DomainField(value = "\"references\"", json = true)
    private JsonNode references;

    /**
     * 示例列表
     */
    @DomainField(json = true)
    private JsonNode examples;

    /**
     * 脚本列表
     */
    @DomainField(json = true)
    private JsonNode scripts;
}
