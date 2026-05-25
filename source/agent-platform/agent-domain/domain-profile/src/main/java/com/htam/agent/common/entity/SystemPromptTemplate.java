package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.consts.TableConst;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统提示词模板
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.PROMPT)
public class SystemPromptTemplate extends BaseEntity {

    /**
     * 分类
     */
    private String category;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 使用次数统计
     */
    private Integer usageCount;
}
