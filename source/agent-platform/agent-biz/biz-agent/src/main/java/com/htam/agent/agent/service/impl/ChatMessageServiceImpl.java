package com.htam.agent.agent.service.impl;

import com.htam.agent.agent.service.ChatMessageService;
import com.htam.agent.common.entity.ChatMessage;
import com.htam.agent.repo.agent.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天消息 Service 实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage getById(Integer id) {
        return chatMessageRepository.getById(id);
    }

    @Override
    public boolean save(ChatMessage entity) {
        return chatMessageRepository.save(entity);
    }

    @Override
    public boolean updateById(ChatMessage entity) {
        return chatMessageRepository.updateById(entity);
    }

    @Override
    public boolean deleteBySessionId(Long sessionId) {
        return chatMessageRepository.deleteBySessionId(sessionId);
    }

    @Override
    public List<ChatMessage> listByIdsOrderByDepth(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return chatMessageRepository.listByIdsOrderByDepth(ids);
    }
}
