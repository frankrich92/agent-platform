package com.htam.agent.runtime;

import java.time.Instant;
import java.util.Map;

public record RunStep(
        String stepId,
        String runId,
        RunStepType stepType,
        AgentRunStatus status,
        Instant startedAt,
        Instant endedAt,
        Map<String, Object> summary) {

    public RunStep {
        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException("stepId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        stepType = stepType == null ? RunStepType.OTHER : stepType;
        status = status == null ? AgentRunStatus.ACCEPTED : status;
        summary = summary == null ? Map.of() : Map.copyOf(summary);
    }
}
