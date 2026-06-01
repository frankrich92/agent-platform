package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.firstNonBlank;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.namespace;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.profile.agent.service.AgentDefinitionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(CapabilityPlanContributor.ORDER_TOOL)
@RequiredArgsConstructor
public class ToolCapabilityContributor implements CapabilityPlanContributor {

    private final AgentDefinitionService agentDefinitionService;

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        for (ToolConfig tool : agentDefinitionService.getEnabledToolsOfAgent(context.agentId())) {
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
                    firstNonBlank(tool.getToolId(), tool.getName(), tool.getId()),
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
}
