package com.htam.agent.common.entity;

import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.HookType;
import com.htam.agent.common.persistence.DomainTable;
import lombok.Getter;
import lombok.Setter;

/**
 * Hook配置表
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(value = TableConst.HOOK)
public class HookConfig extends BaseEntity {

    /**
     * Hook名称
     */
    private String name;

    /**
     * Hook类型
     */
    private HookType hookType;

    /**
     * 描述
     */
    private String description;

    /**
     * hook路径（tool_type为SYSTEM时使用）
     */
    private String classPath;

    /**
     * hook内容（tool_type为CUSTOM时使用）
     */
    private String code;

    /**
     * 执行优先级
     */
    private Integer priority;
}
