package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.repo.mybatis.agent.mapper.AgentChatKeyMapper;
import com.htam.agent.common.entity.AgentChatKey;
import com.htam.agent.repo.agent.AgentChatKeyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentChatKeyMybatisRepository implements AgentChatKeyRepository {
    private final AgentChatKeyMapper agentChatKeyMapper;

    @Override
    public List<AgentChatKey> list() {
        return agentChatKeyMapper.selectList(null);
    }

    @Override
    public AgentChatKey getByAgentCode(String agentCode) {
        return agentChatKeyMapper.selectOne(Wrappers.<AgentChatKey>lambdaQuery()
                .eq(AgentChatKey::getAgentCode, agentCode));
    }

    @Override
    public AgentChatKey getByChatKey(String chatKey) {
        return agentChatKeyMapper.selectOne(Wrappers.<AgentChatKey>lambdaQuery()
                .eq(AgentChatKey::getChatKey, chatKey));
    }

    @Override
    public boolean save(AgentChatKey entity) {
        return agentChatKeyMapper.insert(entity) > 0;
    }

    @Override
    public boolean deleteByAgentCode(String agentCode) {
        return agentChatKeyMapper.delete(Wrappers.<AgentChatKey>lambdaQuery()
                .eq(AgentChatKey::getAgentCode, agentCode)) >= 0;
    }
}
