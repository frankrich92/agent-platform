package com.htam.agent.common.dto;

import com.htam.agent.common.config.SerializableEnable;
import java.util.List;
import lombok.Data;

/**
 * MCP 工具全局启停请求
 *
 * @author huxuehao
 */
@Data
public class McpToolEnabledDTO implements SerializableEnable {
    private List<Long> toolIds;
    private Boolean enabled;
}
