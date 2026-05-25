package com.htam.agent.common.entity;

import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainFieldFill;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 描述：AgentscopeSession
 *
 * @author huxuehao
 **/
@Getter
@Setter
@DomainTable(TableConst.AGENT_SCOPE_SESSIONS)
@AllArgsConstructor
@NoArgsConstructor
public class AgentScopeSession implements SerializableEnable {
    @DomainId
    private String sessionId;
    private String stateKey;
    private Integer itemIndex;
    private String stateData;
    @DomainField(fill = DomainFieldFill.INSERT)
    private LocalDateTime createdAt;

    @DomainField(fill = DomainFieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
