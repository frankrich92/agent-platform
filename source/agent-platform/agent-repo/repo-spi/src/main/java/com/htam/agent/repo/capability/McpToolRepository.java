package com.htam.agent.repo.capability;

import com.htam.agent.common.entity.McpTool;

import java.util.List;
import java.util.Map;

public interface McpToolRepository {
    List<McpTool> listByServerId(Long mcpServerId);

    List<McpTool> listByServerIdOrdered(Long mcpServerId);

    long countByServerId(Long mcpServerId);

    boolean save(McpTool entity);

    boolean updateById(McpTool entity);

    boolean updateEnabledByIds(List<Long> ids, Boolean enabled);

    List<McpTool> listRuntimeTools(Long mcpServerId);

    List<McpTool> listByServerIds(List<Long> mcpServerIds);

    List<McpTool> listByIds(List<Long> ids);

    Map<Long, Integer> countAvailableTools(List<Long> mcpServerIds);

    boolean deleteByMcpServerIds(List<Long> mcpServerIds);
}
