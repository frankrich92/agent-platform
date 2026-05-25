package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.fasterxml.jackson.databind.JsonNode;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.A2aType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：Agent和A2A的关联关系
 *
 * @author huxuehao
 **/
@Getter
@Setter
@NoArgsConstructor
@DomainTable(value = TableConst.AGENT_A2A, autoResultMap = true)
public class AgentA2A implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private A2aType a2aType;
    @DomainField(json = true)
    private JsonNode a2aConfig;

    public AgentA2A(Long agentDefinitionId, A2aType a2aType, JsonNode a2aConfig) {
        this.agentDefinitionId = agentDefinitionId;
        this.a2aType = a2aType;
        this.a2aConfig = a2aConfig;
    }
}
