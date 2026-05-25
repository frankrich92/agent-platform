package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.McpToolExposureMode;
import lombok.*;

/**
 * 智能体与MCP服务器关联
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.AGENT_MCP)
@AllArgsConstructor
@NoArgsConstructor
public class AgentMcpServer implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private Long mcpServerId;
    private McpToolExposureMode exposureMode;
}
