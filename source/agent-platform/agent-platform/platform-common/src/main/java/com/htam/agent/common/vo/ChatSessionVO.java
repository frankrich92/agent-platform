package com.htam.agent.common.vo;

import com.htam.agent.common.config.SerializableEnable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 聊天会话 VO
 *
 * @author huxuehao
 */
@Getter
@Setter
public class ChatSessionVO implements SerializableEnable {
    private Long id;
    private Long userId;
    private Long agentId;
    private Long currentMessageId;
    private String title;
    private Boolean isPinned;
    private LocalDateTime pinTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
