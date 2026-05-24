package com.htam.agent.run.replay;

import java.util.List;

public record RunReplayPlan(
        String replayId,
        String runId,
        List<RunReplayFrame> frames) {

    public RunReplayPlan {
        if (replayId == null || replayId.isBlank()) {
            throw new IllegalArgumentException("replayId 不能为空");
        }
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        frames = frames == null ? List.of() : List.copyOf(frames);
    }
}
