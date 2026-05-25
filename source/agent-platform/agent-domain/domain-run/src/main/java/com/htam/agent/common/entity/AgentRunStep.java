package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainTable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DomainTable(TableConst.AGENT_RUN_STEP)
public class AgentRunStep implements SerializableEnable {

    @DomainId(value = "step_id", type = DomainIdType.INPUT)
    private String stepId;

    private String runId;

    private String stepType;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private String summaryJson;
}
