package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htam.agent.common.consts.TableConst;
import lombok.*;

/**
 * 智能体与子智能体关联
 *
 * @author huxuehao
 */
@Getter
@Setter
@TableName(TableConst.AGENT_AGENT)
@AllArgsConstructor
@NoArgsConstructor
public class AgentSubAgent implements SerializableEnable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long parentAgentId;
    private Long subAgentId;
}
