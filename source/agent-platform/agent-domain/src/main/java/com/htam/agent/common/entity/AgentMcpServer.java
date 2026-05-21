package com.htam.agent.common.entity;

import com.htam.agent.common.config.SerializableEnable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.enums.McpToolExposureMode;
import lombok.*;

/**
 * 智能体与MCP服务器关联
 *
 * @author huxuehao
 */
@Getter
@Setter
@TableName(TableConst.AGENT_MCP)
@AllArgsConstructor
@NoArgsConstructor
public class AgentMcpServer implements SerializableEnable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long agentDefinitionId;
    private Long mcpServerId;
    private McpToolExposureMode exposureMode;
}
