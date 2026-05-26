package com.htam.agent.profile.agent.service.impl;

import com.htam.agent.capability.CapabilityItem;
import java.util.List;

public interface CapabilityPlanContributor {

    void contribute(CapabilityPlanContext context, List<CapabilityItem> items);
}
