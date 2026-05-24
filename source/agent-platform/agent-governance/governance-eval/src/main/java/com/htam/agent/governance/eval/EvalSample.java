package com.htam.agent.governance.eval;

import java.util.List;
import java.util.Map;

public record EvalSample(
        String sampleId,
        String profileVersion,
        String input,
        List<String> expectedSignals,
        Map<String, Object> labels) {

    public EvalSample {
        if (sampleId == null || sampleId.isBlank()) {
            throw new IllegalArgumentException("sampleId 不能为空");
        }
        expectedSignals = expectedSignals == null ? List.of() : List.copyOf(expectedSignals);
        labels = labels == null ? Map.of() : Map.copyOf(labels);
    }
}
