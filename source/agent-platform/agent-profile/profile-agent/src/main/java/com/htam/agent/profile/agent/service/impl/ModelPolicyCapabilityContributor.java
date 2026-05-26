package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.common.entity.AgentDefinition;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class ModelPolicyCapabilityContributor implements CapabilityPlanContributor {

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        AgentDefinition agent = context.agent();
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
                        "requirePlanConfirmation", agent.getRequirePlanConfirmation(),
                        "maxSubtasks", agent.getMaxSubtasks(),
                        "enableMemory", agent.getEnableMemory(),
                        "enableMemoryCompression", agent.getEnableMemoryCompression(),
                        "memoryCompressionConfig", agent.getMemoryCompressionConfig())));
    }
}
