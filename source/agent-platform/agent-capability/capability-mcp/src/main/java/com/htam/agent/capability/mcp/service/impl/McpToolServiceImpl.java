package com.htam.agent.capability.mcp.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.entity.McpTool;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.util.CryptoUtils;
import com.htam.agent.common.vo.McpToolVO;
import com.htam.agent.capability.mcp.service.McpToolService;
import com.htam.agent.repo.capability.McpToolRepository;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * MCP 工具目录 Service 实现
 *
 * @author huxuehao
 */
@Service
@RequiredArgsConstructor
public class McpToolServiceImpl implements McpToolService {
    private static final String SCHEMA_HASH_SALT = "MCP_TOOL_SCHEMA_HASH";

    private final McpToolRepository mcpToolRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<McpToolVO> listToolVos(Long mcpServerId) {
        McpServer mcpServer = BeanUtils.getBean(com.htam.agent.capability.mcp.service.McpServerService.class)
                .getById(mcpServerId);
        ensureBackfilledFromCache(mcpServer);
        return mcpToolRepository.listByServerIdOrdered(mcpServerId)
                .stream()
                .map(item -> BeanUtils.copy(item, McpToolVO.class))
                .toList();
    }

    @Override
    public void ensureBackfilledFromCache(McpServer mcpServer) {
        if (mcpServer == null || mcpServer.getId() == null) {
            return;
        }
        long count = mcpToolRepository.countByServerId(mcpServer.getId());
        if (count > 0) {
            return;
        }

        List<McpSchema.Tool> cachedTools = parseCachedTools(mcpServer.getToolSchemas());
        if (cachedTools.isEmpty()) {
            return;
        }
        syncServerTools(mcpServer, cachedTools);
    }

    @Override
    public void syncServerTools(McpServer mcpServer, List<McpSchema.Tool> tools) {
        if (mcpServer == null || mcpServer.getId() == null) {
            return;
        }

        List<McpTool> existing = mcpToolRepository.listByServerId(mcpServer.getId());
        Map<String, McpTool> existingMap = existing.stream().collect(Collectors.toMap(
                McpTool::getToolName,
                Function.identity(),
                (left, right) -> left,
                LinkedHashMap::new));

        LocalDateTime now = LocalDateTime.now();
        Set<String> currentNames = new LinkedHashSet<>();
        List<McpSchema.Tool> toolList = tools == null ? List.of() : tools;

        for (int i = 0; i < toolList.size(); i++) {
            McpSchema.Tool tool = toolList.get(i);
            if (tool == null || tool.name() == null || tool.name().isBlank()) {
                continue;
            }

            currentNames.add(tool.name());
            McpTool existingTool = existingMap.get(tool.name());
            McpTool entity = new McpTool();
            entity.setMcpServerId(mcpServer.getId());
            entity.setToolName(tool.name());
            entity.setDescription(tool.description());
            entity.setInputSchema(toJsonNode(tool.inputSchema()));
            entity.setOutputSchema(toJsonNode(tool.outputSchema()));
            entity.setRawSchema(toJsonNode(tool));
            entity.setSchemaHash(buildSchemaHash(tool));
            entity.setMissing(false);
            entity.setSort(i + 1);
            entity.setLastSeenAt(now);
            entity.setLastDiscoveredAt(existingTool == null ? now : existingTool.getLastDiscoveredAt());
            entity.setEnabled(existingTool == null || existingTool.getEnabled() == null
                    ? Boolean.TRUE
                    : existingTool.getEnabled());

            if (existingTool == null) {
                mcpToolRepository.save(entity);
            } else {
                entity.setId(existingTool.getId());
                mcpToolRepository.updateById(entity);
            }
        }

        List<McpTool> disappearedTools = existing.stream()
                .filter(item -> !currentNames.contains(item.getToolName()))
                .toList();
        disappearedTools.forEach(item -> {
            McpTool update = new McpTool();
            update.setId(item.getId());
            update.setMissing(true);
            mcpToolRepository.updateById(update);
        });
    }

    @Override
    public void updateGlobalEnabled(Long mcpServerId, List<Long> toolIds, Boolean enabled) {
        if (toolIds == null || toolIds.isEmpty()) {
            return;
        }

        List<McpTool> tools = listByIdsPreserveOrder(toolIds);
        if (tools.size() != new LinkedHashSet<>(toolIds).size()) {
            throw new RuntimeException("存在无效的 MCP 工具选择");
        }
        boolean mismatch = tools.stream().anyMatch(item -> !Objects.equals(item.getMcpServerId(), mcpServerId));
        if (mismatch) {
            throw new RuntimeException("存在不属于当前 MCP 的工具");
        }

        mcpToolRepository.updateEnabledByIds(toolIds, enabled);
    }

    @Override
    public List<McpTool> listRuntimeTools(Long mcpServerId) {
        return mcpToolRepository.listRuntimeTools(mcpServerId);
    }

    @Override
    public List<McpTool> listByServerIds(List<Long> mcpServerIds) {
        return mcpToolRepository.listByServerIds(mcpServerIds);
    }

    @Override
    public List<McpTool> listByIdsPreserveOrder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<McpTool> tools = mcpToolRepository.listByIds(ids);
        Map<Long, McpTool> toolMap = tools.stream().collect(Collectors.toMap(McpTool::getId, Function.identity()));
        List<McpTool> ordered = new ArrayList<>();
        for (Long id : ids) {
            McpTool item = toolMap.get(id);
            if (item != null) {
                ordered.add(item);
            }
        }
        return ordered;
    }

    @Override
    public Map<Long, Integer> countAvailableTools(List<Long> mcpServerIds) {
        return mcpToolRepository.countAvailableTools(mcpServerIds);
    }

    @Override
    public void deleteByMcpServerIds(List<Long> mcpServerIds) {
        mcpToolRepository.deleteByMcpServerIds(mcpServerIds);
    }

    private List<McpSchema.Tool> parseCachedTools(String toolSchemasJson) {
        if (toolSchemasJson == null || toolSchemasJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(toolSchemasJson, new TypeReference<List<McpSchema.Tool>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private JsonNode toJsonNode(Object value) {
        return value == null ? null : objectMapper.valueToTree(value);
    }

    private String buildSchemaHash(McpSchema.Tool tool) {
        return CryptoUtils.md5(toJsonNode(tool).toString(), SCHEMA_HASH_SALT);
    }
}
