package com.htam.agent.repo.agent;

import com.htam.agent.common.entity.ChatSession;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.support.RepoPage;
import java.util.List;
import java.util.Map;

public interface ChatSessionRepository {
    ChatSession getById(Long id);

    boolean save(ChatSession entity);

    boolean updateById(ChatSession entity);

    boolean deleteById(Long id);

    List<ChatSession> listSessions(Long userId, Long agentId);

    RepoPage<ChatSession> pageSessions(PageParams pageParams, Long userId, Long agentId, Boolean isPinned);

    List<Map<String, Object>> countSessionsByDay(Long agentId, String startDate);

    List<Map<String, Object>> countActiveUsersByDay(Long agentId, String startDate);
}
