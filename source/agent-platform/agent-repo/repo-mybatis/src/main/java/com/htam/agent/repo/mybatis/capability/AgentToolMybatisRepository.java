package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AgentTool;
import com.htam.agent.repo.capability.AgentToolRepository;
import com.htam.agent.repo.mybatis.capability.mapper.AgentToolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AgentToolMybatisRepository implements AgentToolRepository {
    private final AgentToolMapper agentToolMapper;

    @Override
    public List<AgentTool> listByToolIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return agentToolMapper.selectList(Wrappers.<AgentTool>lambdaQuery()
                .in(AgentTool::getToolId, toolIds));
    }

    @Override
    public List<AgentTool> listByAgentDefinitionId(Long agentDefinitionId) {
        return agentToolMapper.selectList(Wrappers.<AgentTool>lambdaQuery()
                .eq(AgentTool::getAgentDefinitionId, agentDefinitionId));
    }

    @Override
    public boolean save(AgentTool agentTool) {
        return agentToolMapper.insert(agentTool) > 0;
    }

    @Override
    public boolean deleteByAgentDefinitionIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentToolMapper.delete(Wrappers.<AgentTool>lambdaQuery()
                .in(AgentTool::getAgentDefinitionId, agentIds)) >= 0;
    }

    @Override
    public boolean deleteByToolIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return true;
        }
        return agentToolMapper.delete(Wrappers.<AgentTool>lambdaQuery()
                .in(AgentTool::getToolId, toolIds)) >= 0;
    }
}
