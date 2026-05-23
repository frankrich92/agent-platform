package com.htam.agent.capability;

import org.springframework.stereotype.Service;

@Service
public class DefaultCapabilityPlanService implements CapabilityPlanService {

    @Override
    public CapabilityPlan resolvePlan(Long agentId) {
        return CapabilityPlan.empty(agentId);
    }
}
