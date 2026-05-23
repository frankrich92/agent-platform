package com.htam.agent.runtime.memory;

public record CompressionSpan(
        String spanId,
        String runId,
        int fromMessageDepth,
        int toMessageDepth,
        int originalTokens,
        int compressedTokens,
        CompressionStatus status,
        String failureReason) {
}
