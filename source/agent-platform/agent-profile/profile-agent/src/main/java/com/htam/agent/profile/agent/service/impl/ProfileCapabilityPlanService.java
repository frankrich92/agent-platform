package com.htam.agent.profile.agent.service.impl;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityPlan;
import com.htam.agent.capability.CapabilityPlanService;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.profile.agent.service.AgentDefinitionService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 基于 Agent Profile 配置生成能力计划的默认实现。
 * 各类 CapabilityPlanContributor 只负责贡献自身能力项，本类负责聚合并输出统一计划。
 */
@Service
@Primary
public class ProfileCapabilityPlanService implements CapabilityPlanService {

    private final AgentDefinitionService agentDefinitionService;
    private final List<CapabilityPlanContributor> contributors;

    public ProfileCapabilityPlanService(
            AgentDefinitionService agentDefinitionService,
            List<CapabilityPlanContributor> contributors) {
        this.agentDefinitionService = agentDefinitionService;
        this.contributors = List.copyOf(contributors);
    }

    @Override
    public CapabilityPlan resolvePlan(Long agentId) {
        AgentDefinition agent = agentDefinitionService.getById(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("AgentDefinition 不存在: " + agentId);
        }

        CapabilityPlanContext context = new CapabilityPlanContext(agentId, agent);
        List<CapabilityItem> items = new ArrayList<>();
        contributors.forEach(contributor -> contributor.contribute(context, items));

        return new CapabilityPlan(
                "agent-" + agentId + "-profile-plan",
                agentId,
                agent.getModelConfigId() == null ? null : String.valueOf(agent.getModelConfigId()),
                CapabilityRiskPolicy.ASK,
                items);
    }
}
