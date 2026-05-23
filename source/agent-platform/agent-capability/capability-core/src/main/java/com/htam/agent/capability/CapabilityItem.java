package com.htam.agent.capability;

import java.util.List;
import java.util.Map;

public record CapabilityItem(
        CapabilityKind kind,
        String capabilityId,
        String name,
        String namespace,
        boolean enabled,
        boolean readOnly,
        CapabilityRiskLevel riskLevel,
        CapabilityRiskPolicy riskPolicy,
        String secretRef,
        List<String> includePatterns,
        List<String> excludePatterns,
        Map<String, Object> attributes) {

    public CapabilityItem {
        if (capabilityId == null || capabilityId.isBlank()) {
            throw new IllegalArgumentException("capabilityId 不能为空");
        }
        kind = kind == null ? CapabilityKind.TOOL : kind;
        namespace = namespace == null || namespace.isBlank() ? "default" : namespace;
        riskLevel = riskLevel == null ? CapabilityRiskLevel.LOW : riskLevel;
        riskPolicy = riskPolicy == null
                ? (riskLevel == CapabilityRiskLevel.HIGH ? CapabilityRiskPolicy.ASK : CapabilityRiskPolicy.ALLOW)
                : riskPolicy;
        includePatterns = includePatterns == null ? List.of() : List.copyOf(includePatterns);
        excludePatterns = excludePatterns == null ? List.of() : List.copyOf(excludePatterns);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public boolean executableWithoutApproval() {
        return enabled && riskPolicy == CapabilityRiskPolicy.ALLOW;
    }
}
