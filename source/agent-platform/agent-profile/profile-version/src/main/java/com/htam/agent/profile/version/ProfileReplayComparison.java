package com.htam.agent.profile.version;

import java.util.Map;

public record ProfileReplayComparison(
        String replayId,
        Long agentId,
        String baselineVersion,
        String candidateVersion,
        int totalSamples,
        int passedSamples,
        Map<String, Object> metrics) {

    public ProfileReplayComparison {
        metrics = metrics == null ? Map.of() : Map.copyOf(metrics);
    }
}
