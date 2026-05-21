package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.AgentHook;
import com.htam.agent.hook.mapper.AgentHookMapper;
import com.htam.agent.repo.capability.AgentHookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AgentHookMybatisRepository implements AgentHookRepository {
    private final AgentHookMapper agentHookMapper;

    @Override
    public List<AgentHook> listByHookConfigIds(List<Long> hookIds) {
        if (hookIds == null || hookIds.isEmpty()) {
            return List.of();
        }
        return agentHookMapper.selectList(Wrappers.<AgentHook>lambdaQuery()
                .in(AgentHook::getHookConfigId, hookIds));
    }

    @Override
    public List<AgentHook> listByAgentDefinitionId(Long agentDefinitionId) {
        return agentHookMapper.selectList(Wrappers.<AgentHook>lambdaQuery()
                .eq(AgentHook::getAgentDefinitionId, agentDefinitionId));
    }

    @Override
    public boolean save(AgentHook agentHook) {
        return agentHookMapper.insert(agentHook) > 0;
    }

    @Override
    public boolean deleteByAgentDefinitionIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return true;
        }
        return agentHookMapper.delete(Wrappers.<AgentHook>lambdaQuery()
                .in(AgentHook::getAgentDefinitionId, agentIds)) >= 0;
    }

    @Override
    public boolean deleteByHookConfigIds(List<Long> hookIds) {
        if (hookIds == null || hookIds.isEmpty()) {
            return true;
        }
        return agentHookMapper.delete(Wrappers.<AgentHook>lambdaQuery()
                .in(AgentHook::getHookConfigId, hookIds)) >= 0;
    }
}
