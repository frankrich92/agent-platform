package com.htam.agent.governance.eval;

import java.time.Instant;
import java.util.Map;

public record EvalResult(
        String evalId,
        String sampleId,
        String runId,
        boolean passed,
        Map<String, Object> metrics,
        Instant evaluatedAt) {

    public EvalResult {
        if (evalId == null || evalId.isBlank()) {
            throw new IllegalArgumentException("evalId 不能为空");
        }
        metrics = metrics == null ? Map.of() : Map.copyOf(metrics);
        evaluatedAt = evaluatedAt == null ? Instant.now() : evaluatedAt;
    }
}
