package com.htam.agent.profile.agent.service.impl;

import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.attributes;
import static com.htam.agent.profile.agent.service.impl.CapabilityPlanItemSupport.firstNonBlank;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.capability.knowledge.service.AgentKnowledgeBaseService;
import com.htam.agent.capability.knowledge.service.KnowledgeBaseConfigService;
import com.htam.agent.common.entity.KnowledgeBaseConfig;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(50)
@RequiredArgsConstructor
public class KnowledgeCapabilityContributor implements CapabilityPlanContributor {

    private final AgentKnowledgeBaseService agentKnowledgeBaseService;
    private final KnowledgeBaseConfigService knowledgeBaseConfigService;

    @Override
    public void contribute(CapabilityPlanContext context, List<CapabilityItem> items) {
        for (Long knowledgeId : agentKnowledgeBaseService.getKnowledgeIds(context.agentId())) {
            KnowledgeBaseConfig knowledge = knowledgeBaseConfigService.getById(knowledgeId);
            if (knowledge == null) {
                continue;
            }
            items.add(new CapabilityItem(
                    CapabilityKind.KNOWLEDGE,
                    String.valueOf(knowledge.getId()),
                    firstNonBlank(knowledge.getName(), knowledge.getId()),
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
}
