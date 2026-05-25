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
@DomainTable(TableConst.AGENT_TOOL_CALL)
public class AgentToolCall implements SerializableEnable {

    @DomainId(value = "tool_call_id", type = DomainIdType.INPUT)
    private String toolCallId;

    private String runId;

    private String toolName;

    private String policy;

    private Boolean readOnly;

    private Long durationMillis;

    private Long costMicros;

    private String parameterSummary;

    private String resultSummary;

    private String errorCode;

    private String errorMessage;

    private String auditTagsJson;

    private LocalDateTime createdAt;
}
