package com.htam.agent.profile.agent.service.impl;

import com.htam.agent.capability.CapabilityRiskLevel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CapabilityPlanItemSupport {

    private CapabilityPlanItemSupport() {
    }

    static Map<String, Object> attributes(Object... entries) {
        if (entries == null || entries.length == 0) {
            return new LinkedHashMap<>();
        }
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("Capability attributes must be provided as key-value pairs");
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            if (!(entries[i] instanceof String key) || key.isBlank()) {
                throw new IllegalArgumentException("Capability attribute key must be a non-blank string");
            }
            Object value = entries[i + 1];
            if (value != null) {
                attributes.put(key, value);
            }
        }
        return attributes;
    }

    static String firstNonBlank(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    static String namespace(String prefix, String suffix) {
        String normalizedPrefix = firstNonBlank(prefix);
        String normalizedSuffix = firstNonBlank(suffix);
        if (normalizedPrefix == null) {
            return normalizedSuffix;
        }
        if (normalizedSuffix == null) {
            return normalizedPrefix;
        }
        return normalizedPrefix + ":" + normalizedSuffix;
    }

    static List<String> toolIdsAsPatterns(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return toolIds.stream()
                .map(toolId -> "mcp-tool:" + toolId)
                .toList();
    }

    static CapabilityRiskLevel skillRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) {
            return CapabilityRiskLevel.LOW;
        }
        try {
            return CapabilityRiskLevel.valueOf(riskLevel.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return CapabilityRiskLevel.LOW;
        }
    }
}
