package com.htam.agent.capability.mcp.service;

import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.entity.McpTool;
import com.htam.agent.common.vo.McpToolVO;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具目录 Service
 *
 * @author huxuehao
 */
public interface McpToolService {
    List<McpToolVO> listToolVos(Long mcpServerId);

    void ensureBackfilledFromCache(McpServer mcpServer);

    void syncServerTools(McpServer mcpServer, List<McpSchema.Tool> tools);

    void updateGlobalEnabled(Long mcpServerId, List<Long> toolIds, Boolean enabled);

    List<McpTool> listRuntimeTools(Long mcpServerId);

    List<McpTool> listByServerIds(List<Long> mcpServerIds);

    List<McpTool> listByIdsPreserveOrder(List<Long> ids);

    Map<Long, Integer> countAvailableTools(List<Long> mcpServerIds);

    void deleteByMcpServerIds(List<Long> mcpServerIds);
}
