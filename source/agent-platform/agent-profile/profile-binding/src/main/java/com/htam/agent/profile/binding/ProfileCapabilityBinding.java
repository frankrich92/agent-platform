package com.htam.agent.profile.binding;

import com.htam.agent.capability.CapabilityKind;
import java.util.Map;

public record ProfileCapabilityBinding(
        Long agentId,
        ProfileBindingType bindingType,
        CapabilityKind capabilityKind,
        String targetId,
        String namespace,
        boolean enabled,
        int priority,
        Map<String, Object> metadata) {

    public ProfileCapabilityBinding {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId 不能为空");
        }
        bindingType = bindingType == null ? ProfileBindingType.TOOL : bindingType;
        capabilityKind = capabilityKind == null ? CapabilityKind.TOOL : capabilityKind;
        namespace = namespace == null || namespace.isBlank() ? "default" : namespace;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
