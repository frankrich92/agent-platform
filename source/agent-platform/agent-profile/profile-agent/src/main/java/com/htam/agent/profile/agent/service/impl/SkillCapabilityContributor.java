package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.firstNonBlank;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.namespace;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.skillRiskLevel;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.common.entity.SkillPackage;
import com.htam.agent.common.skill.SkillMetadata;
import com.htam.agent.profile.agent.service.AgentDefinitionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(CapabilityPlanContributor.ORDER_SKILL)
@RequiredArgsConstructor
public class SkillCapabilityContributor implements CapabilityPlanContributor {

    private final AgentDefinitionService agentDefinitionService;

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        for (SkillPackage skill : agentDefinitionService.getEnabledSkillsOfAgent(context.agentId())) {
            SkillMetadata metadata = SkillMetadata.from(skill);
            CapabilityRiskLevel riskLevel = skillRiskLevel(metadata.riskLevel());
            Map<String, Object> attributes = attributes(
                    "category", skill.getCategory(),
                    "description", skill.getDescription(),
                    "contentRef", "skill:" + skill.getId());
            attributes.putAll(metadata.asAttributes());
            items.add(new CapabilityItem(
                    CapabilityKind.SKILL,
                    String.valueOf(skill.getId()),
                    firstNonBlank(skill.getName(), skill.getId()),
                    namespace("skill", skill.getCategory()),
                    Boolean.TRUE.equals(skill.getEnabled()),
                    true,
                    riskLevel,
                    riskLevel == CapabilityRiskLevel.HIGH ? CapabilityRiskPolicy.ASK : CapabilityRiskPolicy.ALLOW,
                    null,
                    List.of(),
                    List.of(),
                    attributes));
        }
    }
}
