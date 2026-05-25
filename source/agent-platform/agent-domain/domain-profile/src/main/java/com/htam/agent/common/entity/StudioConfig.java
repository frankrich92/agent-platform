package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Studio配置
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.STUDIO_CONFIG)
@AllArgsConstructor
@NoArgsConstructor
public class StudioConfig implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private String url;
    private String project;
}
