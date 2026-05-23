package com.htam.agent.profile;

import java.time.Instant;
import java.util.Map;

public record ProfileVersion(
        Long agentId,
        String version,
        ProfileVersionStatus status,
        String promptHash,
        String capabilityPlanId,
        Instant createdAt,
        Map<String, Object> metadata) {

    public ProfileVersion {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId 不能为空");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version 不能为空");
        }
        status = status == null ? ProfileVersionStatus.DRAFT : status;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
