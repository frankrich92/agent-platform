package com.htam.agent.repo.mybatis.agent;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htam.agent.repo.mybatis.agent.mapper.AgentScopeSessionMapper;
import com.htam.agent.common.consts.DataSourceConst;
import com.htam.agent.repo.agent.AgentScopeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@DS(DataSourceConst.CONVERSATION)
@RequiredArgsConstructor
public class AgentScopeSessionMybatisRepository implements AgentScopeSessionRepository {
    private final AgentScopeSessionMapper agentScopeSessionMapper;

    @Override
    public boolean deleteById(String id) {
        return agentScopeSessionMapper.deleteById(id) >= 0;
    }
}
