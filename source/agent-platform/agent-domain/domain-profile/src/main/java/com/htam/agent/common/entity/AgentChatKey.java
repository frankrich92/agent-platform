package com.htam.agent.common.entity;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.consts.TableConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 智能体对话Key
 *
 * @author huxuehao
 */
@Getter
@Setter
@DomainTable(TableConst.AGENT_CHAT_KEY)
@AllArgsConstructor
@NoArgsConstructor
public class AgentChatKey implements SerializableEnable {
    private String agentCode;
    private String chatKey;
}
