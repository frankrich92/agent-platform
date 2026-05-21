package com.htam.agent.repo.mybatis.knowledge;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AgentKnowledgeBase;
import com.htam.agent.knowledge.mapper.AgentKnowledgeBaseMapper;
import com.htam.agent.repo.knowledge.AgentKnowledgeBaseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentKnowledgeBaseMybatisRepository implements AgentKnowledgeBaseRepository {
    private final AgentKnowledgeBaseMapper agentKnowledgeBaseMapper;

    @Override
    public List<AgentKnowledgeBase> listByKnowledgeIds(List<Long> knowledgeIds) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return List.of();
        }
        return agentKnowledgeBaseMapper.selectList(Wrappers.<AgentKnowledgeBase>lambdaQuery()
                .in(AgentKnowledgeBase::getKnowledgeBaseConfigId, knowledgeIds));
    }

    @Override
    public List<AgentKnowledgeBase> listByAgentId(Long agentDefinitionId) {
        return agentKnowledgeBaseMapper.selectList(Wrappers.<AgentKnowledgeBase>lambdaQuery()
                .eq(AgentKnowledgeBase::getAgentDefinitionId, agentDefinitionId));
    }

    @Override
    public boolean save(AgentKnowledgeBase entity) {
        return agentKnowledgeBaseMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentKnowledgeBaseMapper.delete(Wrappers.<AgentKnowledgeBase>lambdaQuery()
                .in(AgentKnowledgeBase::getAgentDefinitionId, agentIds)) >= 0;
    }

    @Override
    public boolean deleteByKnowledgeIds(List<Long> knowledgeIds) {
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return true;
        }
        return agentKnowledgeBaseMapper.delete(Wrappers.<AgentKnowledgeBase>lambdaQuery()
                .in(AgentKnowledgeBase::getKnowledgeBaseConfigId, knowledgeIds)) >= 0;
    }
}
