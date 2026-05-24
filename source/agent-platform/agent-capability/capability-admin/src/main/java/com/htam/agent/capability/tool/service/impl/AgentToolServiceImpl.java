package com.htam.agent.capability.tool.service.impl;

import com.htam.agent.common.entity.AgentTool;
import com.htam.agent.repo.capability.AgentToolRepository;
import com.htam.agent.capability.tool.service.AgentToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体工具关联Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AgentToolServiceImpl implements AgentToolService {
    private final AgentToolRepository agentToolRepository;

    @Override
    public List<Long> getAgentIds(List<Long> tools) {
        if (tools == null || tools.isEmpty()) {
            return List.of();
        }
        return agentToolRepository.listByToolIds(tools)
                .stream()
                .map(AgentTool::getAgentDefinitionId)
                .distinct()
                .toList();
    }

    @Override
    public List<Long> getToolIds(Long agentDefinitionId) {
        return agentToolRepository.listByAgentDefinitionId(agentDefinitionId)
                .stream()
                .map(AgentTool::getToolId)
                .toList();
    }

    @Override
    public Boolean insertAgentTool(Long agentDefinitionId, List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return true;
        }
        toolIds.forEach(toolId -> {
            agentToolRepository.save(new AgentTool(null, agentDefinitionId, toolId));
        });
        return true;
    }

    @Override
    public Boolean deleteAgentTool(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentToolRepository.deleteByAgentDefinitionIds(agentIds);
    }

    @Override
    public Boolean deleteByToolIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return true;
        }
        return agentToolRepository.deleteByToolIds(toolIds);
    }

    @Override
    public Boolean saveAgentTool(Long agentDefinitionId, List<Long> toolIds) {
        deleteAgentTool(List.of(agentDefinitionId));
        insertAgentTool(agentDefinitionId, toolIds);

        return Boolean.TRUE;
    }
}
