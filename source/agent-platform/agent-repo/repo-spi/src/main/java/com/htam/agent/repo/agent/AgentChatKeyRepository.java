package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.AgentChatKey;
import java.util.List;

public interface AgentChatKeyRepository {
    List<AgentChatKey> list();

    AgentChatKey getByAgentCode(String agentCode);

    AgentChatKey getByChatKey(String chatKey);

    boolean save(AgentChatKey entity);

    boolean deleteByAgentCode(String agentCode);
}
