package com.htam.agent.profile;

import java.util.List;

public record ProfileEvalSample(
        String sampleId,
        Long agentId,
        String input,
        List<String> expectedSignals,
        String riskLabel) {

    public ProfileEvalSample {
        if (sampleId == null || sampleId.isBlank()) {
            throw new IllegalArgumentException("sampleId 不能为空");
        }
        expectedSignals = expectedSignals == null ? List.of() : List.copyOf(expectedSignals);
    }
}
