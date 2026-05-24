package com.htam.agent.runtime.context;

public record ContextSegmentRef(
        String segmentId,
        String source,
        int estimatedTokens,
        boolean compressible) {

    public ContextSegmentRef {
        if (segmentId == null || segmentId.isBlank()) {
            throw new IllegalArgumentException("segmentId 不能为空");
        }
        source = source == null || source.isBlank() ? "runtime" : source;
        estimatedTokens = Math.max(0, estimatedTokens);
    }
}
