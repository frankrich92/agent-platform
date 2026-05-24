package com.htam.agent.capability.knowledge.service.impl;

import com.htam.agent.common.entity.AgentKnowledgeBase;
import com.htam.agent.capability.knowledge.service.AgentKnowledgeBaseService;
import com.htam.agent.repo.knowledge.AgentKnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体知识库关联Service实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class AgentKnowledgeBaseServiceImpl implements AgentKnowledgeBaseService {
    private final AgentKnowledgeBaseRepository agentKnowledgeBaseRepository;

    @Override
    public List<Long> getAgentIds(List<Long> knowledgeIds) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return List.of();
        }
        return agentKnowledgeBaseRepository.listByKnowledgeIds(knowledgeIds)
                .stream()
                .map(AgentKnowledgeBase::getAgentDefinitionId)
                .distinct()
                .toList();
    }

    @Override
    public List<Long> getKnowledgeIds(Long agentDefinitionId) {
        return agentKnowledgeBaseRepository.listByAgentId(agentDefinitionId)
                .stream()
                .map(AgentKnowledgeBase::getKnowledgeBaseConfigId)
                .toList();
    }

    @Override
    public Boolean insertAgentKnowledge(Long agentDefinitionId, List<Long> knowledgeIds) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return Boolean.TRUE;
        }
        knowledgeIds.forEach(knowledgeId -> {
            agentKnowledgeBaseRepository.save(new AgentKnowledgeBase(null, agentDefinitionId, knowledgeId));
        });

        return true;
    }

    @Override
    public Boolean deleteAgentKnowledge(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentKnowledgeBaseRepository.deleteByAgentIds(agentIds);
    }

    @Override
    public Boolean deleteByKnowledgeIds(List<Long> knowledgeIds) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return true;
        }
        return agentKnowledgeBaseRepository.deleteByKnowledgeIds(knowledgeIds);
    }

    @Override
    public Boolean saveAgentKnowledge(Long agentDefinitionId, List<Long> knowledgeIds) {
        deleteAgentKnowledge(List.of(agentDefinitionId));
        insertAgentKnowledge(agentDefinitionId, knowledgeIds);

        return Boolean.TRUE;
    }
}
