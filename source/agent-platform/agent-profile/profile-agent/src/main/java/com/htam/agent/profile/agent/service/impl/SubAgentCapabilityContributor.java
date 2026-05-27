package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.firstNonBlank;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.namespace;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.profile.agent.service.AgentDefinitionService;
import com.htam.agent.profile.agent.service.AgentSubAgentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(CapabilityPlanContributor.ORDER_SUB_AGENT)
@RequiredArgsConstructor
public class SubAgentCapabilityContributor implements CapabilityPlanContributor {

    private final AgentSubAgentService agentSubAgentService;
    private final AgentDefinitionService agentDefinitionService;

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        AgentDefinition parentAgent = context.agent();
        for (Long subAgentId : agentSubAgentService.getSubAgentIds(parentAgent.getId())) {
            AgentDefinition subAgent = agentDefinitionService.getById(subAgentId);
            if (subAgent == null) {
                continue;
            }
            items.add(new CapabilityItem(
                    CapabilityKind.SUB_AGENT,
                    String.valueOf(subAgent.getId()),
                    firstNonBlank(subAgent.getName(), subAgent.getId()),
                    namespace("sub-agent", subAgent.getAgentType() == null ? null : subAgent.getAgentType().name().toLowerCase()),
                    Boolean.TRUE.equals(subAgent.getEnabled()),
                    false,
                    CapabilityRiskLevel.MEDIUM,
                    CapabilityRiskPolicy.ASK,
                    null,
                    List.of(),
                    List.of(),
                    attributes(
                            "agentCode", subAgent.getAgentCode(),
                            "agentType", subAgent.getAgentType(),
                            "version", subAgent.getVersion(),
                            "isolatedContext", true,
                            "summaryRequired", true,
                            "parentMaxSubtasks", parentAgent.getMaxSubtasks())));
        }
    }
}
