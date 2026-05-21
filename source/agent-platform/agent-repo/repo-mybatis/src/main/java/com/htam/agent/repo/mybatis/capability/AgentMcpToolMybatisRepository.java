package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AgentMcpTool;
import com.htam.agent.mcp.mapper.AgentMcpToolMapper;
import com.htam.agent.repo.capability.AgentMcpToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AgentMcpToolMybatisRepository implements AgentMcpToolRepository {
    private final AgentMcpToolMapper agentMcpToolMapper;

    @Override
    public List<AgentMcpTool> listByAgentDefinitionId(Long agentDefinitionId) {
        return agentMcpToolMapper.selectList(Wrappers.<AgentMcpTool>lambdaQuery()
                .eq(AgentMcpTool::getAgentDefinitionId, agentDefinitionId));
    }

    @Override
    public boolean save(AgentMcpTool entity) {
        return agentMcpToolMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByAgentDefinitionIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentMcpToolMapper.delete(Wrappers.<AgentMcpTool>lambdaQuery()
                .in(AgentMcpTool::getAgentDefinitionId, agentIds)) >= 0;
    }

    @Override
    public boolean deleteByMcpToolIds(List<Long> mcpToolIds) {
        if (mcpToolIds == null || mcpToolIds.isEmpty()) {
            return true;
        }
        return agentMcpToolMapper.delete(Wrappers.<AgentMcpTool>lambdaQuery()
                .in(AgentMcpTool::getMcpToolId, mcpToolIds)) >= 0;
    }
}
