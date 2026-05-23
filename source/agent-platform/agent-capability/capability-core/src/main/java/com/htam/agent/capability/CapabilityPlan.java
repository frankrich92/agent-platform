package com.htam.agent.capability;

import java.util.List;

public record CapabilityPlan(
        String planId,
        Long agentId,
        String modelPolicyId,
        CapabilityRiskPolicy highRiskDefaultPolicy,
        List<CapabilityItem> items) {

    public CapabilityPlan {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        planId = planId == null || planId.isBlank() ? "agent-" + agentId + "-default" : planId;
        highRiskDefaultPolicy = highRiskDefaultPolicy == null ? CapabilityRiskPolicy.ASK : highRiskDefaultPolicy;
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static CapabilityPlan empty(Long agentId) {
        return new CapabilityPlan(null, agentId, null, CapabilityRiskPolicy.ASK, List.of());
    }

    public List<CapabilityItem> enabledItems() {
        return items.stream().filter(CapabilityItem::enabled).toList();
    }

    public List<CapabilityItem> executableItems() {
        return items.stream().filter(CapabilityItem::executableWithoutApproval).toList();
    }
}
