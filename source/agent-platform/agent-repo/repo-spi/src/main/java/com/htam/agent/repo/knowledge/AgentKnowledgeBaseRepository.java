package com.htam.agent.repo.knowledge;

import com.htam.agent.common.entity.AgentKnowledgeBase;
import java.util.List;

public interface AgentKnowledgeBaseRepository {
    List<AgentKnowledgeBase> listByKnowledgeIds(List<Long> knowledgeIds);

    List<AgentKnowledgeBase> listByAgentId(Long agentDefinitionId);

    boolean save(AgentKnowledgeBase entity);

    boolean deleteByAgentIds(List<Long> agentIds);

    boolean deleteByKnowledgeIds(List<Long> knowledgeIds);
}
