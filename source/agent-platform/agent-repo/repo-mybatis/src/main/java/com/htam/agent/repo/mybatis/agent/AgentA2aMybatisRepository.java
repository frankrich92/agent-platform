package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.repo.mybatis.agent.mapper.AgentA2aMapper;
import com.htam.agent.common.entity.AgentA2A;
import com.htam.agent.repo.agent.AgentA2aRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentA2aMybatisRepository implements AgentA2aRepository {
    private final AgentA2aMapper agentA2aMapper;

    @Override
    public AgentA2A getByAgentId(Long agentId) {
        return agentA2aMapper.selectOne(Wrappers.<AgentA2A>lambdaQuery()
                .eq(AgentA2A::getAgentDefinitionId, agentId));
    }

    @Override
    public boolean save(AgentA2A entity) {
        return agentA2aMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentA2aMapper.delete(Wrappers.<AgentA2A>lambdaQuery()
                .in(AgentA2A::getAgentDefinitionId, agentIds)) >= 0;
    }
}
