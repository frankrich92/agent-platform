package com.htam.agent.common.vo;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.enums.McpToolExposureMode;
import java.util.List;
import lombok.Data;

/**
 * Agent 与 MCP 的绑定信息
 *
 * @author huxuehao
 */
@Data
public class AgentMcpBindingVO implements SerializableEnable {
    private Long mcpServerId;
    private McpToolExposureMode exposureMode;
    private List<Long> mcpToolIds;
}
