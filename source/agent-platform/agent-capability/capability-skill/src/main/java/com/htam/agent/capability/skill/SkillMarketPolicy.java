package com.htam.agent.capability.skill;

import java.util.List;

public record SkillMarketPolicy(
        boolean hotReloadEnabled,
        boolean signatureRequired,
        boolean humanReviewRequired,
        List<String> allowedVersionRanges) {

    public SkillMarketPolicy {
        allowedVersionRanges = allowedVersionRanges == null ? List.of() : List.copyOf(allowedVersionRanges);
    }

    public static SkillMarketPolicy enterpriseDefault() {
        return new SkillMarketPolicy(false, true, true, List.of());
    }
}
