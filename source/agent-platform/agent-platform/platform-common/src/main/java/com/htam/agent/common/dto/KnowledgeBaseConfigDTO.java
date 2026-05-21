package com.htam.agent.common.dto;

import com.htam.agent.common.enums.KbType;
import com.htam.agent.common.mp.annotation.QueryDefine;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.mp.support.QueryCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库查询DTO
 *
 * @author huxuehao
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseConfigDTO extends PageParams {

    @QueryDefine(value = "知识库名称", condition = QueryCondition.LIKE)
    private String name;

    @QueryDefine(value = "知识库类型", condition = QueryCondition.EQ)
    private KbType kbType;

    @QueryDefine(value = "是否可用", condition = QueryCondition.EQ)
    private Boolean enabled;
}
