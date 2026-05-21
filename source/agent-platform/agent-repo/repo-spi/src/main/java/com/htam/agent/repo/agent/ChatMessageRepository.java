package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.ChatMessage;
import java.util.List;
import java.util.Map;

public interface ChatMessageRepository {
    ChatMessage getById(Integer id);

    boolean save(ChatMessage entity);

    boolean updateById(ChatMessage entity);

    boolean deleteBySessionId(Long sessionId);

    List<ChatMessage> listByIdsOrderByDepth(List<Integer> ids);

    List<Map<String, Object>> countMessagesByDay(Long agentId, String startDate);

    List<Map<String, Object>> avgRoundsByDay(Long agentId, String startDate);
}
