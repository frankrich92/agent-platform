package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.agent.mapper.AgentSubAgentMapper;
import com.htam.agent.common.entity.AgentSubAgent;
import com.htam.agent.repo.agent.AgentSubAgentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentSubAgentMybatisRepository implements AgentSubAgentRepository {
    private final AgentSubAgentMapper agentSubAgentMapper;

    @Override
    public List<AgentSubAgent> listByParentAgentId(Long agentDefinitionId) {
        return agentSubAgentMapper.selectList(Wrappers.<AgentSubAgent>lambdaQuery()
                .eq(AgentSubAgent::getParentAgentId, agentDefinitionId));
    }

    @Override
    public boolean save(AgentSubAgent entity) {
        return agentSubAgentMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByParentAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentSubAgentMapper.delete(Wrappers.<AgentSubAgent>lambdaQuery()
                .in(AgentSubAgent::getParentAgentId, agentIds)) >= 0;
    }
}
