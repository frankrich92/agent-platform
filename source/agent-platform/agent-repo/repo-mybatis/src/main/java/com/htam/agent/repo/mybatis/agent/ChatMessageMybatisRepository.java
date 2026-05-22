package com.htam.agent.repo.mybatis.agent;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.agent.mapper.ChatMessageMapper;
import com.htam.agent.common.entity.ChatMessage;
import com.htam.agent.repo.agent.ChatMessageRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageMybatisRepository implements ChatMessageRepository {
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage getById(Integer id) {
        return chatMessageMapper.selectById(id);
    }

    @Override
    public boolean save(ChatMessage entity) {
        return chatMessageMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(ChatMessage entity) {
        return chatMessageMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteBySessionId(Long sessionId) {
        return chatMessageMapper.delete(Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getSessionId, sessionId)) >= 0;
    }

    @Override
    public List<ChatMessage> listByIdsOrderByDepth(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return chatMessageMapper.selectList(Wrappers.<ChatMessage>lambdaQuery()
                .in(ChatMessage::getId, ids)
                .orderByAsc(ChatMessage::getDepth));
    }

    @Override
    public List<Map<String, Object>> countMessagesByDay(Long agentId, String startDate) {
        return chatMessageMapper.countMessagesByDay(agentId, startDate);
    }

    @Override
    public List<Map<String, Object>> avgRoundsByDay(Long agentId, String startDate) {
        return chatMessageMapper.avgRoundsByDay(agentId, startDate);
    }
}
