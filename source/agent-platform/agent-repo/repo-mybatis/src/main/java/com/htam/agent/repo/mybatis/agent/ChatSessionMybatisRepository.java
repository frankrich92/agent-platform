package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.htam.agent.agent.mapper.ChatSessionMapper;
import com.htam.agent.common.entity.ChatSession;
import com.htam.agent.common.mp.support.MP;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.agent.ChatSessionRepository;
import com.htam.agent.repo.support.RepoPage;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatSessionMybatisRepository implements ChatSessionRepository {
    private final ChatSessionMapper chatSessionMapper;

    @Override
    public ChatSession getById(Long id) {
        return chatSessionMapper.selectById(id);
    }

    @Override
    public boolean save(ChatSession entity) {
        return chatSessionMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(ChatSession entity) {
        return chatSessionMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return chatSessionMapper.deleteById(id) >= 0;
    }

    @Override
    public List<ChatSession> listSessions(Long userId, Long agentId) {
        return chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(userId != null, ChatSession::getUserId, userId)
                .eq(agentId != null, ChatSession::getAgentId, agentId)
                .orderByDesc(ChatSession::getIsPinned)
                .orderByDesc(ChatSession::getUpdatedAt));
    }

    @Override
    public RepoPage<ChatSession> pageSessions(PageParams pageParams, Long userId, Long agentId, Boolean isPinned) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(userId != null, ChatSession::getUserId, userId)
                .eq(agentId != null, ChatSession::getAgentId, agentId)
                .eq(isPinned != null, ChatSession::getIsPinned, isPinned)
                .orderByDesc(ChatSession::getUpdatedAt);
        IPage<ChatSession> page = chatSessionMapper.selectPage(MP.getPage(pageParams), wrapper);
        return new RepoPage<>(page.getRecords(), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public List<Map<String, Object>> countSessionsByDay(Long agentId, String startDate) {
        return chatSessionMapper.countSessionsByDay(agentId, startDate);
    }

    @Override
    public List<Map<String, Object>> countActiveUsersByDay(Long agentId, String startDate) {
        return chatSessionMapper.countActiveUsersByDay(agentId, startDate);
    }
}
