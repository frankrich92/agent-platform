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
 * AgentStudio
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.AGENT_STUDIO)
@AllArgsConstructor
@NoArgsConstructor
public class AgentStudio implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private Long studioId;
}
