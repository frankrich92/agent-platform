package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.firstNonBlank;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.toolIdsAsPatterns;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.capability.mcp.service.AgentMcpServerService;
import com.htam.agent.capability.mcp.service.McpServerService;
import com.htam.agent.capability.mcp.service.McpToolService;
import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.entity.McpTool;
import com.htam.agent.common.enums.McpActivationStatus;
import com.htam.agent.common.enums.McpToolExposureMode;
import com.htam.agent.common.vo.AgentMcpBindingVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(CapabilityPlanContributor.ORDER_MCP)
@RequiredArgsConstructor
public class McpCapabilityContributor implements CapabilityPlanContributor {

    private final AgentMcpServerService agentMcpServerService;
    private final McpServerService mcpServerService;
    private final McpToolService mcpToolService;

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        for (AgentMcpBindingVO binding : agentMcpServerService.getBindings(context.agentId())) {
            if (binding.getMcpServerId() == null) {
                continue;
            }
            McpServer server = mcpServerService.getById(binding.getMcpServerId());
            if (server == null) {
                continue;
            }

            McpToolExposureMode exposureMode = binding.getExposureMode() == null
                    ? McpToolExposureMode.ALL_GLOBAL
                    : binding.getExposureMode();
            List<McpTool> runtimeTools = mcpToolService.listRuntimeTools(server.getId());
            List<McpTool> allTools = mcpToolService.listByServerIds(List.of(server.getId()));
            List<Long> runtimeToolIds = runtimeTools.stream().map(McpTool::getId).toList();
            List<String> includePatterns = exposureMode == McpToolExposureMode.SELECTED_ONLY
                    ? selectedAvailableToolPatterns(binding.getMcpToolIds(), runtimeToolIds)
                    : runtimeTools.isEmpty() ? List.of("*") : toolIdsAsPatterns(runtimeToolIds);
            List<Long> excludedToolIds = allTools.stream()
                    .filter(tool -> !Boolean.TRUE.equals(tool.getEnabled()) || Boolean.TRUE.equals(tool.getMissing()))
                    .map(McpTool::getId)
                    .toList();
            List<Long> selectedUnavailableToolIds = selectedUnavailableToolIds(binding.getMcpToolIds(), runtimeToolIds);
            boolean active = server.getActivationStatus() == null
                    || server.getActivationStatus() == McpActivationStatus.ACTIVE;

            items.add(new CapabilityItem(
                    CapabilityKind.MCP,
                    String.valueOf(server.getId()),
                    firstNonBlank(server.getName(), server.getId()),
                    "mcp:" + firstNonBlank(server.getName(), server.getId()),
                    Boolean.TRUE.equals(server.getEnabled()) && active,
                    false,
                    CapabilityRiskLevel.MEDIUM,
                    CapabilityRiskPolicy.ASK,
                    "mcp-server:" + server.getId() + ":protocol-config",
                    includePatterns,
                    toolIdsAsPatterns(excludedToolIds),
                    attributes(
                            "protocol", server.getProtocol(),
                            "mode", server.getMode(),
                            "exposureMode", exposureMode,
                            "activationStatus", server.getActivationStatus(),
                            "healthStatus", server.getHealthStatus(),
                            "toolCount", server.getToolCount(),
                            "needsSync", server.getNeedsSync(),
                            "configHash", server.getConfigHash(),
                            "runtimeToolIds", runtimeToolIds,
                            "runtimeToolNames", runtimeTools.stream().map(McpTool::getToolName).toList(),
                            "runtimeToolSchemaHashes", runtimeTools.stream()
                                    .map(McpTool::getSchemaHash)
                                    .filter(hash -> hash != null && !hash.isBlank())
                                    .toList(),
                            "excludedToolIds", excludedToolIds,
                            "selectedUnavailableToolIds", selectedUnavailableToolIds)));
        }
    }

    private static List<String> selectedAvailableToolPatterns(List<Long> selectedToolIds, List<Long> runtimeToolIds) {
        if (selectedToolIds == null || selectedToolIds.isEmpty()) {
            return List.of();
        }
        List<Long> runtimeIds = runtimeToolIds == null ? List.of() : runtimeToolIds;
        return toolIdsAsPatterns(selectedToolIds.stream()
                .filter(runtimeIds::contains)
                .toList());
    }

    private static List<Long> selectedUnavailableToolIds(List<Long> selectedToolIds, List<Long> runtimeToolIds) {
        if (selectedToolIds == null || selectedToolIds.isEmpty()) {
            return List.of();
        }
        List<Long> runtimeIds = runtimeToolIds == null ? List.of() : runtimeToolIds;
        return selectedToolIds.stream()
                .filter(toolId -> !runtimeIds.contains(toolId))
                .toList();
    }
}
