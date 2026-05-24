package com.htam.agent.profile.agent.service.impl;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityPlanService;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.capability.knowledge.service.AgentKnowledgeBaseService;
import com.htam.agent.capability.knowledge.service.KnowledgeBaseConfigService;
import com.htam.agent.capability.mcp.service.AgentMcpServerService;
import com.htam.agent.capability.mcp.service.McpServerService;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import com.htam.agent.common.entity.McpServer;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.McpActivationStatus;
import com.htam.agent.common.enums.McpToolExposureMode;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.common.vo.AgentMcpBindingVO;
import com.htam.agent.profile.agent.service.AgentDefinitionService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class ProfileCapabilityPlanService implements CapabilityPlanService {

    private final AgentDefinitionService agentDefinitionService;
    private final AgentMcpServerService agentMcpServerService;
    private final McpServerService mcpServerService;
    private final AgentKnowledgeBaseService agentKnowledgeBaseService;
    private final KnowledgeBaseConfigService knowledgeBaseConfigService;

    @Override
    public CapabilityPlan resolvePlan(Long agentId) {
        AgentDefinition agent = agentDefinitionService.getById(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("AgentDefinition 不存在: " + agentId);
        }

        List<CapabilityItem> items = new ArrayList<>();
        addModelPolicy(items, agent);
        addTools(items, agentId);
        addSkills(items, agentId);
        addMcpServers(items, agentId);
        addKnowledgeBases(items, agentId);

        return new CapabilityPlan(
                "agent-" + agentId + "-profile-plan",
                agentId,
                agent.getModelConfigId() == null ? null : String.valueOf(agent.getModelConfigId()),
                CapabilityRiskPolicy.ASK,
                items);
    }

    private void addModelPolicy(List<CapabilityItem> items, AgentDefinition agent) {
        if (agent.getModelConfigId() == null) {
            return;
        }
        items.add(new CapabilityItem(
                CapabilityKind.MODEL_POLICY,
                String.valueOf(agent.getModelConfigId()),
                "model-config-" + agent.getModelConfigId(),
                "model",
                Boolean.TRUE.equals(agent.getEnabled()),
                true,
                CapabilityRiskLevel.LOW,
                CapabilityRiskPolicy.ALLOW,
                null,
                List.of(),
                List.of(),
                attributes(
                        "toolChoiceStrategy", agent.getToolChoiceStrategy(),
                        "specificToolName", agent.getSpecificToolName(),
                        "maxIterations", agent.getMaxIterations(),
                        "enablePlanning", agent.getEnablePlanning(),
                        "requirePlanConfirmation", agent.getRequirePlanConfirmation())));
    }

    private void addTools(List<CapabilityItem> items, Long agentId) {
        for (ToolConfig tool : agentDefinitionService.getEnabledToolsOfAgent(agentId)) {
            boolean customTool = tool.getToolType() == ToolType.CUSTOM;
            boolean needConfirm = Boolean.TRUE.equals(tool.getNeedConfirm());
            CapabilityRiskLevel riskLevel = customTool || needConfirm
                    ? CapabilityRiskLevel.HIGH
                    : CapabilityRiskLevel.LOW;
            CapabilityRiskPolicy riskPolicy = customTool || needConfirm
                    ? CapabilityRiskPolicy.ASK
                    : CapabilityRiskPolicy.ALLOW;
            items.add(new CapabilityItem(
                    CapabilityKind.TOOL,
                    String.valueOf(tool.getId()),
                    firstNonBlank(tool.getToolId(), tool.getName(), String.valueOf(tool.getId())),
                    namespace("tool", tool.getCategory()),
                    Boolean.TRUE.equals(tool.getEnabled()),
                    false,
                    riskLevel,
                    riskPolicy,
                    null,
                    List.of(),
                    List.of(),
                    attributes(
                            "toolId", tool.getToolId(),
                            "toolType", tool.getToolType(),
                            "category", tool.getCategory(),
                            "language", tool.getLanguage(),
                            "version", tool.getVersion(),
                            "needConfirm", tool.getNeedConfirm())));
        }
    }

    private void addSkills(List<CapabilityItem> items, Long agentId) {
        for (SkillPackage skill : agentDefinitionService.getEnabledSkillsOfAgent(agentId)) {
            items.add(new CapabilityItem(
                    CapabilityKind.SKILL,
                    String.valueOf(skill.getId()),
                    firstNonBlank(skill.getName(), String.valueOf(skill.getId())),
                    namespace("skill", skill.getCategory()),
                    Boolean.TRUE.equals(skill.getEnabled()),
                    true,
                    CapabilityRiskLevel.LOW,
                    CapabilityRiskPolicy.ALLOW,
                    null,
                    List.of(),
                    List.of(),
                    attributes(
                            "category", skill.getCategory(),
                            "description", skill.getDescription(),
                            "contentRef", "skill:" + skill.getId())));
        }
    }

    private void addMcpServers(List<CapabilityItem> items, Long agentId) {
        for (AgentMcpBindingVO binding : agentMcpServerService.getBindings(agentId)) {
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
            List<String> includePatterns = exposureMode == McpToolExposureMode.SELECTED_ONLY
                    ? toolIdsAsPatterns(binding.getMcpToolIds())
                    : List.of("*");
            boolean active = server.getActivationStatus() == null
                    || server.getActivationStatus() == McpActivationStatus.ACTIVE;

            items.add(new CapabilityItem(
                    CapabilityKind.MCP,
                    String.valueOf(server.getId()),
                    firstNonBlank(server.getName(), String.valueOf(server.getId())),
                    "mcp:" + firstNonBlank(server.getName(), String.valueOf(server.getId())),
                    Boolean.TRUE.equals(server.getEnabled()) && active,
                    false,
                    CapabilityRiskLevel.MEDIUM,
                    CapabilityRiskPolicy.ASK,
                    "mcp-server:" + server.getId() + ":protocol-config",
                    includePatterns,
                    List.of(),
                    attributes(
                            "protocol", server.getProtocol(),
                            "mode", server.getMode(),
                            "exposureMode", exposureMode,
                            "activationStatus", server.getActivationStatus(),
                            "healthStatus", server.getHealthStatus(),
                            "toolCount", server.getToolCount(),
                            "needsSync", server.getNeedsSync(),
                            "configHash", server.getConfigHash())));
        }
    }

    private void addKnowledgeBases(List<CapabilityItem> items, Long agentId) {
        for (Long knowledgeId : agentKnowledgeBaseService.getKnowledgeIds(agentId)) {
            KnowledgeBaseConfig knowledge = knowledgeBaseConfigService.getById(knowledgeId);
            if (knowledge == null) {
                continue;
            }
            items.add(new CapabilityItem(
                    CapabilityKind.KNOWLEDGE,
                    String.valueOf(knowledge.getId()),
                    firstNonBlank(knowledge.getName(), String.valueOf(knowledge.getId())),
                    "knowledge",
                    Boolean.TRUE.equals(knowledge.getEnabled()),
                    true,
                    CapabilityRiskLevel.LOW,
                    CapabilityRiskPolicy.ALLOW,
                    null,
                    List.of(),
                    List.of(),
                    attributes(
                            "kbType", knowledge.getKbType(),
                            "ragMode", knowledge.getRagMode(),
                            "healthStatus", knowledge.getHealthStatus(),
                            "lastSyncTime", knowledge.getLastSyncTime())));
        }
    }

    private static List<String> toolIdsAsPatterns(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return toolIds.stream()
                .map(toolId -> "mcp-tool:" + toolId)
                .toList();
    }

    private static Map<String, Object> attributes(Object... entries) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            Object value = entries[i + 1];
            if (value != null) {
                attributes.put(String.valueOf(entries[i]), value);
            }
        }
        return attributes;
    }

    private static String namespace(String prefix, String value) {
        return prefix + ":" + firstNonBlank(value, "default");
    }

    private static String firstNonBlank(String first, String second) {
        return firstNonBlank(first, second, null);
    }

    private static String firstNonBlank(String first, String second, String third) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return third == null || third.isBlank() ? "default" : third;
    }
}
