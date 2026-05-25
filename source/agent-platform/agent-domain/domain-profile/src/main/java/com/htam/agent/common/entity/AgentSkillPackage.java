package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.consts.TableConst;
import lombok.*;

/**
 * 智能体与技能包关联
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.AGENT_SKILL)
@AllArgsConstructor
@NoArgsConstructor
public class AgentSkillPackage implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private Long skillPackageId;
}
