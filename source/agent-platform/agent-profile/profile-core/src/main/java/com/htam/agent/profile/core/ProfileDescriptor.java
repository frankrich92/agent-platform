package com.htam.agent.profile.core;

import java.util.List;
import java.util.Map;

public record ProfileDescriptor(
        Long agentId,
        String agentCode,
        String name,
        String version,
        boolean enabled,
        List<String> tags,
        Map<String, Object> metadata) {

    public ProfileDescriptor {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        agentCode = agentCode == null ? "" : agentCode;
        name = name == null ? "" : name;
        version = version == null || version.isBlank() ? "draft" : version;
        tags = tags == null ? List.of() : List.copyOf(tags);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
