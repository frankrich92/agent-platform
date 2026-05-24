package com.htam.agent.run.replay;

import java.time.Instant;
import java.util.Map;

public record RunReplayFrame(
        long sequence,
        String frameType,
        String sourceId,
        Instant timestamp,
        Map<String, Object> payload) {

    public RunReplayFrame {
        if (frameType == null || frameType.isBlank()) {
            throw new IllegalArgumentException("frameType 不能为空");
        }
        timestamp = timestamp == null ? Instant.now() : timestamp;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
