package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.consts.TableConst;
import lombok.*;

/**
 * 智能体与Hook关联
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.AGENT_HOOKS)
@AllArgsConstructor
@NoArgsConstructor
public class AgentHook implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private Long hookConfigId;
}
