package com.htam.agent.mcp.service;

import com.htam.agent.common.entity.AgentMcpServer;
import com.htam.agent.common.vo.AgentMcpBindingVO;

import java.util.List;

/**
 * 智能体MCP服务器关联Service
 *
 * @author huxuehao
 */
public interface AgentMcpServerService {
    List<Long> getAgentIds(List<Long> mcpIds);

    List<Long> getMcpIds(Long agentDefinitionId);

    List<AgentMcpServer> listByAgentDefinitionId(Long agentDefinitionId);

    List<AgentMcpBindingVO> getBindings(Long agentDefinitionId);

    Boolean insertAgentMcpServer(Long agentDefinitionId, List<Long> mcpIds);

    Boolean deleteAgentMcpServer(List<Long> agentIds);

    Boolean deleteByMcpServerIds(List<Long> mcpServerIds);

    Boolean saveAgentMcpServer(Long agentDefinitionId, List<Long> mcpIds, List<AgentMcpBindingVO> bindings);
}
