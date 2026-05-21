package com.htam.agent.mcp.service.impl;

import com.htam.agent.common.entity.AgentMcpTool;
import com.htam.agent.mcp.service.AgentMcpToolService;
import com.htam.agent.repo.capability.AgentMcpToolRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Agent MCP 工具关联 Service 实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AgentMcpToolServiceImpl implements AgentMcpToolService {
    private final AgentMcpToolRepository agentMcpToolRepository;

    @Override
    public List<Long> getToolIds(Long agentDefinitionId) {
        return agentMcpToolRepository.listByAgentDefinitionId(agentDefinitionId)
                .stream()
                .map(AgentMcpTool::getMcpToolId)
                .toList();
    }

    @Override
    public Boolean replaceAgentMcpTools(Long agentDefinitionId, List<Long> mcpToolIds) {
        deleteAgentMcpToolByAgentIds(List.of(agentDefinitionId));
        if (mcpToolIds == null || mcpToolIds.isEmpty()) {
            return Boolean.TRUE;
        }

        Set<Long> distinctIds = new LinkedHashSet<>(mcpToolIds);
        distinctIds.forEach(toolId -> agentMcpToolRepository.save(new AgentMcpTool(null, agentDefinitionId, toolId)));
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteAgentMcpToolByAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return Boolean.TRUE;
        }
        return agentMcpToolRepository.deleteByAgentDefinitionIds(agentIds);
    }

    @Override
    public Boolean deleteByMcpToolIds(List<Long> mcpToolIds) {
        if (mcpToolIds == null || mcpToolIds.isEmpty()) {
            return Boolean.TRUE;
        }
        return agentMcpToolRepository.deleteByMcpToolIds(mcpToolIds);
    }
}
