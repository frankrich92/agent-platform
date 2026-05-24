package com.htam.agent.capability.skill;

import java.util.List;

public record SkillReviewDecision(
        String skillId,
        SkillReleaseStatus targetStatus,
        boolean signatureAccepted,
        boolean dependenciesAccepted,
        boolean humanReviewed,
        boolean publishable,
        List<String> blockers) {

    public SkillReviewDecision {
        targetStatus = targetStatus == null ? SkillReleaseStatus.REVIEWING : targetStatus;
        blockers = blockers == null ? List.of() : List.copyOf(blockers);
    }
}
