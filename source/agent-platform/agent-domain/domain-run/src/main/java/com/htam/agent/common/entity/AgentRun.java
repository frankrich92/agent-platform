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
@DomainTable(TableConst.AGENT_RUN)
public class AgentRun implements SerializableEnable {

    @DomainId(value = "run_id", type = DomainIdType.INPUT)
    private String runId;

    private Long agentId;

    private String sessionId;

    private String status;

    private String input;

    private String output;

    private String traceId;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private String metadataJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
