package com.htam.agent.runtime;

import java.util.Map;

public record RuntimeCapabilityAssemblyRequest(
        Long agentId,
        String capabilityPlanId,
        String runtimeType,
        Map<String, Object> capabilityContext) {

    public RuntimeCapabilityAssemblyRequest {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        runtimeType = runtimeType == null || runtimeType.isBlank() ? "default" : runtimeType;
        capabilityContext = capabilityContext == null ? Map.of() : Map.copyOf(capabilityContext);
    }
}
