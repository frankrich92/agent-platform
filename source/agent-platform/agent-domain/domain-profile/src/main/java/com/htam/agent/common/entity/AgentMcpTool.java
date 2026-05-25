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
 * Agent 与 MCP 工具局部选择关联
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.AGENT_MCP_TOOL)
@AllArgsConstructor
@NoArgsConstructor
public class AgentMcpTool implements SerializableEnable {
    @DomainId(type = DomainIdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private Long mcpToolId;
}
