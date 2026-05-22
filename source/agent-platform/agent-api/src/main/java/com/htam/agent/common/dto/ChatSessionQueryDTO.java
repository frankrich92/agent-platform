package com.htam.agent.common.dto;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.mp.support.PageParams;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 会话列表查询 DTO（按用户、智能体等筛选，支持分页）
 *
 * @author huxuehao
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ChatSessionQueryDTO extends PageParams implements SerializableEnable {
    private Long userId;
    private Long agentId;
    private Boolean isPinned;
}
