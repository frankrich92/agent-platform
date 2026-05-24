package com.htam.agent.capability.skill;

import java.util.ArrayList;
import java.util.List;

public class SkillReleasePlanner {

    public SkillReviewDecision assess(
            SkillIndexEntry entry,
            SkillPackageSignature signature,
            List<SkillDependency> dependencies,
            boolean humanReviewed,
            SkillMarketPolicy policy) {
        if (entry == null) {
            return new SkillReviewDecision(null, SkillReleaseStatus.REJECTED,
                    false, false, humanReviewed, false, List.of("skill entry is required"));
        }
        SkillMarketPolicy marketPolicy = policy == null ? SkillMarketPolicy.enterpriseDefault() : policy;
        List<SkillDependency> dependencyList = dependencies == null ? List.of() : List.copyOf(dependencies);
        List<String> blockers = new ArrayList<>();
        boolean signatureAccepted = !marketPolicy.signatureRequired()
                || (signature != null && signature.verified());
        boolean dependenciesAccepted = dependencyList.stream()
                .filter(dependency -> !dependency.optional())
                .allMatch(dependency -> marketPolicy.allowedVersionRanges().isEmpty()
                        || marketPolicy.allowedVersionRanges().contains(dependency.versionRange()));
        if (!signatureAccepted) {
            blockers.add("skill package signature is required");
        }
        if (!dependenciesAccepted) {
            blockers.add("skill dependency version range is not allowed");
        }
        if (marketPolicy.humanReviewRequired() && !humanReviewed) {
            blockers.add("human review is required");
        }
        boolean publishable = blockers.isEmpty();
        SkillReleaseStatus targetStatus = publishable ? SkillReleaseStatus.APPROVED : SkillReleaseStatus.REVIEWING;
        return new SkillReviewDecision(entry.skillId(), targetStatus, signatureAccepted,
                dependenciesAccepted, humanReviewed, publishable, blockers);
    }
}
