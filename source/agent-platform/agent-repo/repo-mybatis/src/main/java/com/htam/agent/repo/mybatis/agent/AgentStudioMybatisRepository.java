package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AgentStudio;
import com.htam.agent.repo.agent.AgentStudioRepository;
import com.htam.agent.studio.mapper.AgentStudioMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentStudioMybatisRepository implements AgentStudioRepository {
    private final AgentStudioMapper agentStudioMapper;

    @Override
    public List<AgentStudio> listByStudioIds(List<Long> studioIds) {
        if (studioIds == null || studioIds.isEmpty()) {
            return List.of();
        }
        return agentStudioMapper.selectList(Wrappers.<AgentStudio>lambdaQuery()
                .in(AgentStudio::getStudioId, studioIds));
    }

    @Override
    public AgentStudio getFirstByAgentId(Long agentId) {
        return agentStudioMapper.selectOne(Wrappers.<AgentStudio>lambdaQuery()
                .eq(AgentStudio::getAgentDefinitionId, agentId)
                .last("LIMIT 1"));
    }

    @Override
    public List<AgentStudio> listByAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return List.of();
        }
        return agentStudioMapper.selectList(Wrappers.<AgentStudio>lambdaQuery()
                .in(AgentStudio::getAgentDefinitionId, agentIds));
    }

    @Override
    public boolean save(AgentStudio entity) {
        return agentStudioMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentStudioMapper.delete(Wrappers.<AgentStudio>lambdaQuery()
                .in(AgentStudio::getAgentDefinitionId, agentIds)) >= 0;
    }
}
