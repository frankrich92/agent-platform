package com.htam.agent.repo.mybatis.agent;

import com.htam.agent.agent.mapper.AgentScopeSessionMapper;
import com.htam.agent.repo.agent.AgentScopeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentScopeSessionMybatisRepository implements AgentScopeSessionRepository {
    private final AgentScopeSessionMapper agentScopeSessionMapper;

    @Override
    public boolean deleteById(String id) {
        return agentScopeSessionMapper.deleteById(id) >= 0;
    }
}
