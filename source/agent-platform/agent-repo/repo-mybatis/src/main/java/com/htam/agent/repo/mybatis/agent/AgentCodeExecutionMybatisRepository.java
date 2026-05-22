package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.agent.mapper.AgentCodeExecutionMapper;
import com.htam.agent.common.entity.AgentCodeExecution;
import com.htam.agent.repo.agent.AgentCodeExecutionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentCodeExecutionMybatisRepository implements AgentCodeExecutionRepository {
    private final AgentCodeExecutionMapper agentCodeExecutionMapper;

    @Override
    public List<AgentCodeExecution> listByCodeExecutionIds(List<Long> codeExecutionIds) {
        if (codeExecutionIds == null || codeExecutionIds.isEmpty()) {
            return List.of();
        }
        return agentCodeExecutionMapper.selectList(Wrappers.<AgentCodeExecution>lambdaQuery()
                .in(AgentCodeExecution::getCodeExecutionId, codeExecutionIds));
    }

    @Override
    public AgentCodeExecution getByAgentId(Long agentId) {
        return agentCodeExecutionMapper.selectOne(Wrappers.<AgentCodeExecution>lambdaQuery()
                .eq(AgentCodeExecution::getAgentDefinitionId, agentId));
    }

    @Override
    public boolean save(AgentCodeExecution entity) {
        return agentCodeExecutionMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByAgentIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentCodeExecutionMapper.delete(Wrappers.<AgentCodeExecution>lambdaQuery()
                .in(AgentCodeExecution::getAgentDefinitionId, agentIds)) >= 0;
    }
}
