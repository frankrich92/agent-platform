package com.htam.agent.profile.agent.service.impl;

import com.htam.agent.capability.CapabilityRiskLevel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CapabilityPlanItemSupport {

    private CapabilityPlanItemSupport() {
    }

    static Map<String, Object> attributes(Object... entries) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            Object value = entries[i + 1];
            if (value != null) {
                attributes.put(String.valueOf(entries[i]), value);
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
