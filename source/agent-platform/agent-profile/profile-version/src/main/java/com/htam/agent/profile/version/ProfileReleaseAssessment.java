package com.htam.agent.profile.version;

import java.util.List;

public record ProfileReleaseAssessment(
        Long agentId,
        String candidateVersion,
        boolean promotable,
        int totalSamples,
        int passedSamples,
        double passRate,
        List<String> blockers) {

    public ProfileReleaseAssessment {
        blockers = blockers == null ? List.of() : List.copyOf(blockers);
    }
}
