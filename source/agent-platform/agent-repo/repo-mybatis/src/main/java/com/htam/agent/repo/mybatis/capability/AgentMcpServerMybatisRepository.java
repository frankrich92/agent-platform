package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AgentMcpServer;
import com.htam.agent.repo.mybatis.capability.mapper.AgentMcpServerMapper;
import com.htam.agent.repo.capability.AgentMcpServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AgentMcpServerMybatisRepository implements AgentMcpServerRepository {
    private final AgentMcpServerMapper agentMcpServerMapper;

    @Override
    public List<AgentMcpServer> listByMcpServerIds(List<Long> mcpServerIds) {
        if (mcpServerIds == null || mcpServerIds.isEmpty()) {
            return List.of();
        }
        return agentMcpServerMapper.selectList(Wrappers.<AgentMcpServer>lambdaQuery()
                .in(AgentMcpServer::getMcpServerId, mcpServerIds));
    }

    @Override
    public List<AgentMcpServer> listByAgentDefinitionId(Long agentDefinitionId) {
        return agentMcpServerMapper.selectList(Wrappers.<AgentMcpServer>lambdaQuery()
                .eq(AgentMcpServer::getAgentDefinitionId, agentDefinitionId));
    }

    @Override
    public boolean save(AgentMcpServer entity) {
        return agentMcpServerMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByAgentDefinitionIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentMcpServerMapper.delete(Wrappers.<AgentMcpServer>lambdaQuery()
                .in(AgentMcpServer::getAgentDefinitionId, agentIds)) >= 0;
    }

    @Override
    public boolean deleteByMcpServerIds(List<Long> mcpServerIds) {
        if (mcpServerIds == null || mcpServerIds.isEmpty()) {
            return true;
        }
        return agentMcpServerMapper.delete(Wrappers.<AgentMcpServer>lambdaQuery()
                .in(AgentMcpServer::getMcpServerId, mcpServerIds)) >= 0;
    }
}
