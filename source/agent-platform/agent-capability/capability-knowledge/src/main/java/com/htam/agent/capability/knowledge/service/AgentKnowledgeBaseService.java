package com.htam.agent.capability.knowledge.service;

import com.htam.agent.common.entity.AgentKnowledgeBase;

import java.util.List;

/**
 * 智能体知识库关联Service
 *
 * @author huxuehao
 */
public interface AgentKnowledgeBaseService {
    List<Long> getAgentIds(List<Long> knowledgeIds);
    List<Long> getKnowledgeIds(Long agentDefinitionId);
    Boolean insertAgentKnowledge(Long agentDefinitionId, List<Long> knowledgeIds);
    Boolean deleteAgentKnowledge(List<Long> agentIds);
    Boolean deleteByKnowledgeIds(List<Long> knowledgeIds);
    Boolean saveAgentKnowledge(Long agentDefinitionId, List<Long> knowledgeIds);
}
