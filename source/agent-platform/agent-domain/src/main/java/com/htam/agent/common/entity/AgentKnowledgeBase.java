package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htam.agent.common.consts.TableConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 智能体与知识库关联
 *
 * @author huxuehao
 */
@Getter
@Setter
@TableName(TableConst.AGENT_KNOWLEDGE)
@AllArgsConstructor
public class AgentKnowledgeBase implements SerializableEnable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private Long knowledgeBaseConfigId;
}
