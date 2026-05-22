package com.htam.agent.a2a.service;

import com.htam.agent.common.entity.AgentA2A;
import com.htam.agent.repo.agent.AgentA2aRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 描述：AgentA2aServiceImpl
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class AgentA2aServiceImpl implements AgentA2aService {
    private final AgentA2aRepository agentA2aRepository;

    @Override
    public AgentA2A getA2aConfigByAgentId(Long agentId) {
        return agentA2aRepository.getByAgentId(agentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveA2aConfig(AgentA2A agentA2A) {
        agentA2aRepository.deleteByAgentIds(List.of(agentA2A.getAgentDefinitionId()));

        return agentA2aRepository.save(agentA2A);
    }

    @Override
    public boolean deleteA2aConfig(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentA2aRepository.deleteByAgentIds(agentIds);
    }
}
