package com.htam.agent.profile.version;

public record ProfileReleaseGate(
        int minEvalSamples,
        double minPassRate,
        boolean replayRequired,
        boolean grayPolicyRequired) {

    public ProfileReleaseGate {
        minEvalSamples = Math.max(0, minEvalSamples);
        minPassRate = Math.max(0.0, Math.min(1.0, minPassRate));
    }

    public static ProfileReleaseGate enterpriseDefault() {
        return new ProfileReleaseGate(1, 0.8, true, true);
    }
}
