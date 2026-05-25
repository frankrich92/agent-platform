package com.htam.agent.runtime.agentscope.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentMcpServer;
import com.htam.agent.common.entity.AgentMcpTool;
import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.entity.McpTool;
import com.htam.agent.common.enums.McpActivationStatus;
import com.htam.agent.common.enums.McpProtocol;
import com.htam.agent.common.enums.McpToolExposureMode;
import com.htam.agent.common.mcp.McpRuntimeDegradeRecorder;
import com.htam.agent.common.util.CryptoUtils;
import com.htam.agent.common.vo.AgentMcpBindingVO;
import com.htam.agent.repo.capability.AgentMcpServerRepository;
import com.htam.agent.repo.capability.AgentMcpToolRepository;
import com.htam.agent.repo.capability.McpServerRepository;
import com.htam.agent.repo.capability.McpToolRepository;
import com.htam.agent.runtime.agentscope.mcp.impl.HttpMcpClientConfig;
import com.htam.agent.runtime.agentscope.mcp.impl.SseMcpClientConfig;
import com.htam.agent.runtime.agentscope.mcp.impl.StdioMcpClientConfig;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MCP 客户端工厂
 *
 * @author huxuehao
 */
@Component
@RequiredArgsConstructor
public class McpClientFactory {
    private static final String SCHEMA_HASH_SALT = "MCP_TOOL_SCHEMA_HASH";

    private static final Map<McpProtocol, McpClientConfig> INSTANCE = Map.of(
            McpProtocol.STDIO, new StdioMcpClientConfig(),
            McpProtocol.HTTP, new HttpMcpClientConfig(),
            McpProtocol.SSE, new SseMcpClientConfig()
    );

    private static final Logger log = LoggerFactory.getLogger(McpClientFactory.class);

    private final McpServerRepository mcpServerRepository;
    private final AgentMcpServerRepository agentMcpServerRepository;
    private final AgentMcpToolRepository agentMcpToolRepository;
    private final McpToolRepository mcpToolRepository;
    private final McpRuntimeDegradeRecorder mcpRuntimeDegradeRecorder;
    private final ObjectMapper objectMapper;
    private final Map<Long, SharedMcpClientContext> sharedContexts = new ConcurrentHashMap<>();

    public List<McpClientWrapper> getMcpClient(AgentDefinition agentDefinition) {
        List<McpClientWrapper> mcpClients = new ArrayList<>();

        List<Long> mcpIds = getMcpIds(agentDefinition.getId());
        for (Long mcpId : mcpIds) {
            McpServer mcpServer = mcpServerRepository.getById(mcpId);
            if (!isRuntimeAvailable(mcpServer)) {
                closeStaleContext(mcpId);
                continue;
            }

            mcpClients.add(INSTANCE.get(mcpServer.getProtocol()).getMcpClient(mcpServer));
        }

        return mcpClients;
    }

    /**
     * 为单个 MCP 服务创建客户端包装器，供刷新工具目录时使用。
     */
    public McpClientWrapper getMcpClientForServer(McpServer mcpServer) {
        return INSTANCE.get(mcpServer.getProtocol()).getMcpClient(mcpServer);
    }

    /**
     * 根据落库的工具目录构建 MCP 工具，并且在创建智能体期间不连接 MCP 服务。
     */
    public List<AgentTool> getLazyMcpTools(AgentDefinition agentDefinition) {
        List<AgentTool> result = new ArrayList<>();
        List<AgentMcpBindingVO> bindings = getBindings(agentDefinition.getId());

        for (AgentMcpBindingVO binding : bindings) {
            Long mcpId = binding.getMcpServerId();
            McpServer mcpServer = mcpServerRepository.getById(mcpId);
            if (!isRuntimeAvailable(mcpServer)) {
                closeStaleContext(mcpId);
                continue;
            }

            ensureBackfilledFromCache(mcpServer);
            List<McpTool> runtimeTools = mcpToolRepository.listRuntimeTools(mcpId);
            if (binding.getExposureMode() == McpToolExposureMode.SELECTED_ONLY) {
                Set<Long> selectedIds = new HashSet<>(binding.getMcpToolIds() == null
                        ? List.of()
                        : binding.getMcpToolIds());
                runtimeTools = runtimeTools.stream()
                        .filter(tool -> selectedIds.contains(tool.getId()))
                        .toList();
            }

            if (runtimeTools.isEmpty()) {
                closeStaleContext(mcpId);
                log.warn("MCP '{}' has no runtime tools after governance filtering; skip lazy registration",
                        mcpServer.getName());
                continue;
            }

            LazyMcpAgentTool.RuntimeDegradeContext degradeContext = new LazyMcpAgentTool.RuntimeDegradeContext(
                    mcpServer.getId(),
                    mcpServer.getName(),
                    mcpServer.getActivationRevision(),
                    mcpServer.getConfigHash(),
                    mcpServer.getRuntimeFailThreshold());

            runtimeTools.forEach(tool -> {
                McpSchema.Tool toolSchema = parseToolSchema(tool);
                if (toolSchema == null) {
                    return;
                }
                result.add(new LazyMcpAgentTool(
                        degradeContext,
                        toolSchema,
                        () -> getInitializedClient(mcpServer.getId()),
                        mcpRuntimeDegradeRecorder));
            });
        }
        return result;
    }

    public Mono<McpClientWrapper> getInitializedClient(Long mcpServerId) {
        McpServer current = mcpServerRepository.getById(mcpServerId);
        if (!isRuntimeAvailable(current)) {
            closeStaleContext(mcpServerId);
            return Mono.error(new IllegalStateException("MCP 当前不可用"));
        }

        String contextKey = buildContextKey(current);
        SharedMcpClientContext context = sharedContexts.compute(mcpServerId, (id, existing) -> {
            if (existing != null && Objects.equals(existing.contextKey, contextKey)) {
                return existing;
            }

            SharedMcpClientContext created = createContext(current, contextKey);
            if (existing != null) {
                closeContext(existing);
            }
            return created;
        });

        return context.initializedClient;
    }

    private SharedMcpClientContext createContext(McpServer mcpServer, String contextKey) {
        SharedMcpClientContext context = new SharedMcpClientContext(contextKey);
        context.initializedClient = Mono.defer(() -> {
                    McpClientWrapper client = getMcpClientForServer(mcpServer);
                    context.clientRef.set(client);
                    return client.initialize().thenReturn(client);
                })
                .doOnError(e -> {
                    SharedMcpClientContext current = sharedContexts.get(mcpServer.getId());
                    if (current == context) {
                        sharedContexts.remove(mcpServer.getId());
                    }
                    closeContext(context);
                })
                .cache();
        return context;
    }

    private void closeContext(SharedMcpClientContext context) {
        McpClientWrapper client = context.clientRef.getAndSet(null);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.debug("Close MCP client context failed: {}", e.getMessage());
            }
        }
    }

    private void closeStaleContext(Long mcpServerId) {
        SharedMcpClientContext context = sharedContexts.remove(mcpServerId);
        if (context != null) {
            closeContext(context);
        }
    }

    private List<Long> getMcpIds(Long agentDefinitionId) {
        return agentMcpServerRepository.listByAgentDefinitionId(agentDefinitionId)
                .stream()
                .map(AgentMcpServer::getMcpServerId)
                .toList();
    }

    private List<AgentMcpBindingVO> getBindings(Long agentDefinitionId) {
        List<AgentMcpServer> bindings = agentMcpServerRepository.listByAgentDefinitionId(agentDefinitionId);
        if (bindings.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Long>> toolIdsByServerId = new LinkedHashMap<>();
        List<Long> selectedToolIds = agentMcpToolRepository.listByAgentDefinitionId(agentDefinitionId)
                .stream()
                .map(AgentMcpTool::getMcpToolId)
                .toList();
        if (!selectedToolIds.isEmpty()) {
            listByIdsPreserveOrder(selectedToolIds).forEach(tool -> toolIdsByServerId
                    .computeIfAbsent(tool.getMcpServerId(), key -> new ArrayList<>())
                    .add(tool.getId()));
        }

        return bindings.stream().map(binding -> {
            AgentMcpBindingVO vo = new AgentMcpBindingVO();
            vo.setMcpServerId(binding.getMcpServerId());
            vo.setExposureMode(binding.getExposureMode() == null
                    ? McpToolExposureMode.ALL_GLOBAL
                    : binding.getExposureMode());
            vo.setMcpToolIds(toolIdsByServerId.getOrDefault(binding.getMcpServerId(), List.of()));
            return vo;
        }).toList();
    }

    private List<McpTool> listByIdsPreserveOrder(List<Long> ids) {
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

    private void ensureBackfilledFromCache(McpServer mcpServer) {
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

    private void syncServerTools(McpServer mcpServer, List<McpSchema.Tool> tools) {
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
        Set<String> currentNames = new HashSet<>();
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

        existing.stream()
                .filter(item -> !currentNames.contains(item.getToolName()))
                .forEach(item -> {
                    McpTool update = new McpTool();
                    update.setId(item.getId());
                    update.setMissing(true);
                    mcpToolRepository.updateById(update);
                });
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

    private boolean isRuntimeAvailable(McpServer mcpServer) {
        return mcpServer != null
                && Boolean.TRUE.equals(mcpServer.getEnabled())
                && mcpServer.getActivationStatus() == McpActivationStatus.ACTIVE;
    }

    private String buildContextKey(McpServer mcpServer) {
        return mcpServer.getId()
                + ":"
                + (mcpServer.getActivationRevision() == null ? 0L : mcpServer.getActivationRevision())
                + ":"
                + (mcpServer.getConfigHash() == null ? "" : mcpServer.getConfigHash());
    }

    private McpSchema.Tool parseToolSchema(McpTool tool) {
        if (tool.getRawSchema() == null) {
            log.warn("MCP tool '{}' raw schema is empty", tool.getToolName());
            return null;
        }
        try {
            return objectMapper.treeToValue(tool.getRawSchema(), McpSchema.Tool.class);
        } catch (Exception e) {
            log.warn("Failed to parse MCP tool schema '{}': {}", tool.getToolName(), e.getMessage());
            return null;
        }
    }

    private static final class SharedMcpClientContext {
        private final String contextKey;
        private final AtomicReference<McpClientWrapper> clientRef = new AtomicReference<>();
        private Mono<McpClientWrapper> initializedClient;

        private SharedMcpClientContext(String contextKey) {
            this.contextKey = contextKey;
        }
    }
}
