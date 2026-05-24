package com.htam.agent.profile.agent.service;

import com.htam.agent.common.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息 Service
 *
 * @author huxuehao
 */
public interface ChatMessageService {
    ChatMessage getById(Integer id);

    boolean save(ChatMessage entity);

    boolean updateById(ChatMessage entity);

    boolean deleteBySessionId(Long sessionId);

    /**
     * 根据路径上的 id 列表查询消息，按 depth 升序（回显当前完整对话，O(depth)）
     *
     * @param ids 路径拆分出的消息 id 列表
     * @return 按深度排序的消息列表
     */
    List<ChatMessage> listByIdsOrderByDepth(List<Integer> ids);
}
