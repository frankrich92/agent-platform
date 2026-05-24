package com.htam.agent.profile.version;

import java.util.ArrayList;
import java.util.List;

public class ProfileReleasePlanner {

    public ProfileReleaseAssessment assess(
            ProfileVersion candidate,
            List<ProfileEvalSample> evalSamples,
            ProfileReplayComparison replayComparison,
            ProfileReleaseGate releaseGate) {
        if (candidate == null) {
            return new ProfileReleaseAssessment(null, null, false, 0, 0, 0.0,
                    List.of("candidate profile version is required"));
        }
        ProfileReleaseGate gate = releaseGate == null ? ProfileReleaseGate.enterpriseDefault() : releaseGate;
        List<ProfileEvalSample> samples = evalSamples == null ? List.of() : List.copyOf(evalSamples);
        List<String> blockers = new ArrayList<>();
        if (samples.size() < gate.minEvalSamples()) {
            blockers.add("eval sample count is lower than release gate");
        }
        if (gate.replayRequired() && replayComparison == null) {
            blockers.add("replay comparison is required");
        }
        if (gate.grayPolicyRequired() && candidate.status() != ProfileVersionStatus.GRAY) {
            blockers.add("candidate profile version must enter gray status before promotion");
        }

        int totalSamples = replayComparison == null ? samples.size() : replayComparison.totalSamples();
        int passedSamples = replayComparison == null ? 0 : replayComparison.passedSamples();
        double passRate = totalSamples <= 0 ? 0.0 : (double) passedSamples / totalSamples;
        if (totalSamples > 0 && passRate < gate.minPassRate()) {
            blockers.add("replay pass rate is lower than release gate");
        }
        return new ProfileReleaseAssessment(candidate.agentId(), candidate.version(), blockers.isEmpty(),
                totalSamples, passedSamples, passRate, blockers);
    }
}
