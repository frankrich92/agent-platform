package com.htam.agent.repo.mybatis.capability;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htam.agent.common.entity.McpTool;
import com.htam.agent.mcp.mapper.McpToolMapper;
import com.htam.agent.repo.capability.McpToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class McpToolMybatisRepository implements McpToolRepository {
    private final McpToolMapper mcpToolMapper;

    @Override
    public List<McpTool> listByServerId(Long mcpServerId) {
        return mcpToolMapper.selectList(Wrappers.<McpTool>lambdaQuery()
                .eq(McpTool::getMcpServerId, mcpServerId));
    }

    @Override
    public List<McpTool> listByServerIdOrdered(Long mcpServerId) {
        return mcpToolMapper.selectList(Wrappers.<McpTool>lambdaQuery()
                .eq(McpTool::getMcpServerId, mcpServerId)
                .orderByAsc(McpTool::getSort)
                .orderByAsc(McpTool::getToolName));
    }

    @Override
    public long countByServerId(Long mcpServerId) {
        return mcpToolMapper.selectCount(Wrappers.<McpTool>lambdaQuery()
                .eq(McpTool::getMcpServerId, mcpServerId));
    }

    @Override
    public boolean save(McpTool entity) {
        return mcpToolMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(McpTool entity) {
        return mcpToolMapper.updateById(entity) >= 0;
    }

    @Override
    public boolean updateEnabledByIds(List<Long> ids, Boolean enabled) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return mcpToolMapper.update(null, Wrappers.<McpTool>lambdaUpdate()
                .in(McpTool::getId, ids)
                .set(McpTool::getEnabled, enabled)) >= 0;
    }

    @Override
    public List<McpTool> listRuntimeTools(Long mcpServerId) {
        return mcpToolMapper.selectList(Wrappers.<McpTool>lambdaQuery()
                .eq(McpTool::getMcpServerId, mcpServerId)
                .eq(McpTool::getEnabled, true)
                .eq(McpTool::getMissing, false)
                .orderByAsc(McpTool::getSort)
                .orderByAsc(McpTool::getToolName));
    }

    @Override
    public List<McpTool> listByServerIds(List<Long> mcpServerIds) {
        if (mcpServerIds == null || mcpServerIds.isEmpty()) {
            return List.of();
        }
        return mcpToolMapper.selectList(Wrappers.<McpTool>lambdaQuery()
                .in(McpTool::getMcpServerId, mcpServerIds));
    }

    @Override
    public List<McpTool> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mcpToolMapper.selectBatchIds(ids);
    }

    @Override
    public Map<Long, Integer> countAvailableTools(List<Long> mcpServerIds) {
        if (mcpServerIds == null || mcpServerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return mcpToolMapper.selectList(Wrappers.<McpTool>lambdaQuery()
                        .in(McpTool::getMcpServerId, mcpServerIds)
                        .eq(McpTool::getEnabled, true)
                        .eq(McpTool::getMissing, false))
                .stream()
                .collect(Collectors.toMap(
                        McpTool::getMcpServerId,
                        item -> 1,
                        Integer::sum));
    }

    @Override
    public boolean deleteByMcpServerIds(List<Long> mcpServerIds) {
        if (mcpServerIds == null || mcpServerIds.isEmpty()) {
            return true;
        }
        return mcpToolMapper.delete(Wrappers.<McpTool>lambdaQuery()
                .in(McpTool::getMcpServerId, mcpServerIds)) >= 0;
    }
}
