package com.htam.agent.profile;

import java.util.List;

public record ProfileGrayPolicy(
        Long agentId,
        String version,
        int percent,
        List<Long> includeUserIds,
        List<Long> excludeUserIds) {

    public ProfileGrayPolicy {
        percent = Math.max(0, Math.min(100, percent));
        includeUserIds = includeUserIds == null ? List.of() : List.copyOf(includeUserIds);
        excludeUserIds = excludeUserIds == null ? List.of() : List.copyOf(excludeUserIds);
    }
}
