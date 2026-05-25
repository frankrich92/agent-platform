package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.persistence.DomainFieldFill;
import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainId;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * @author huxuehao
 */
@Getter
@Setter
public abstract class BaseEntity implements SerializableEnable {

    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;

    /**
     * 是否可用
     */
    @DomainField(fill = DomainFieldFill.INSERT)
    private Boolean enabled;

    /**
     * 创建时间
     */
    @DomainField(fill = DomainFieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @DomainField(fill = DomainFieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @DomainField(fill = DomainFieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人
     */
    @DomainField(fill = DomainFieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
