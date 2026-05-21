package com.htam.agent.common.dto;

import com.htam.agent.common.enums.HookType;
import com.htam.agent.common.mp.annotation.QueryDefine;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.mp.support.QueryCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Hook配置查询DTO
 *
 * @author huxuehao
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HookConfigDTO extends PageParams {

    @QueryDefine(value = "Hook名称", condition = QueryCondition.LIKE)
    private String name;

    @QueryDefine(value = "Hook类型", condition = QueryCondition.EQ)
    private HookType hookType;

    @QueryDefine(value = "是否可用", condition = QueryCondition.EQ)
    private Boolean enabled;
}
