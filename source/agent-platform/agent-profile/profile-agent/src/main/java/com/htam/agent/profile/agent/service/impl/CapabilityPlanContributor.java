package com.htam.agent.profile.agent.service.impl;

import com.htam.agent.capability.CapabilityItem;
import java.util.List;

public interface CapabilityPlanContributor {

    int ORDER_MODEL_POLICY = 10;
    int ORDER_TOOL = 20;
    int ORDER_SKILL = 30;
    int ORDER_MCP = 40;
    int ORDER_KNOWLEDGE = 50;
    int ORDER_HOOK = 60;
    int ORDER_SUB_AGENT = 70;
    int ORDER_WORKER = 80;

    void contribute(CapabilityPlanContext context, List<CapabilityItem> items);
}
