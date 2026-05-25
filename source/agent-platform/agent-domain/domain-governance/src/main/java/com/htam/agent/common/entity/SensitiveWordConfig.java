package com.htam.agent.common.entity;

import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.SensitiveWordAction;
import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainTable;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * 敏感词配置
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(value = TableConst.SENSITIVE_WORD, autoResultMap = true)
public class SensitiveWordConfig extends BaseEntity {

    /**
     * 分类
     */
    private String category;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 敏感词列表
     */
    @DomainField(json = true)
    private JsonNode words;

    /**
     * 处理动作
     */
    private SensitiveWordAction action;

    /**
     * 替换文本
     */
    private String replacement;
}
