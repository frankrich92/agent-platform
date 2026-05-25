package com.htam.agent.worker.code.service.impl;

import com.htam.agent.worker.code.service.AgentCodeExecutionService;
import com.htam.agent.common.entity.AgentCodeExecution;
import com.htam.agent.repo.agent.AgentCodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：AgentCodeExecutionServiceImpl
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class AgentCodeExecutionServiceImpl implements AgentCodeExecutionService {
    private final AgentCodeExecutionRepository agentCodeExecutionRepository;

    @Override
    public List<Long> getAgentIds(List<Long> codeExecutionIds) {
        if (codeExecutionIds == null || codeExecutionIds.isEmpty()) {
            return List.of();
        }
        return agentCodeExecutionRepository.listByCodeExecutionIds(codeExecutionIds)
                .stream()
                .map(AgentCodeExecution::getAgentDefinitionId)
                .distinct()
                .toList();
    }

    @Override
    public Long getCodeExecutionIdByAgentId(Long agentId) {
        AgentCodeExecution item = agentCodeExecutionRepository.getByAgentId(agentId);
        return item == null ? null : item.getCodeExecutionId();
    }

    @Override
    public Boolean insertAgentCodeExecution(Long agentDefinitionId, List<Long> codeExecutionIds) {
        if (codeExecutionIds == null || codeExecutionIds.isEmpty()) {
            return false;
        }
        codeExecutionIds.forEach(codeExecutionId -> {
            agentCodeExecutionRepository.save(new AgentCodeExecution(null, agentDefinitionId, codeExecutionId));
        });

        return true;
    }

    @Override
    public Boolean deleteAgentCodeExecution(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentCodeExecutionRepository.deleteByAgentIds(agentIds);
    }

    @Override
    public Boolean saveAgentCodeExecution(Long agentDefinitionId, List<Long> codeExecutionIds) {
        deleteAgentCodeExecution(List.of(agentDefinitionId));
        insertAgentCodeExecution(agentDefinitionId, codeExecutionIds);

        return Boolean.TRUE;
    }
}
