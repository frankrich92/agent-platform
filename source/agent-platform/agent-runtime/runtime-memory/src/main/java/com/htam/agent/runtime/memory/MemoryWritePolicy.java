package com.htam.agent.runtime.memory;

public record MemoryWritePolicy(
        boolean flushBeforeCompression,
        boolean downgradeOnCompressionFailure,
        int maxMemoryChars) {

    public MemoryWritePolicy {
        maxMemoryChars = maxMemoryChars <= 0 ? 4000 : maxMemoryChars;
    }

    public static MemoryWritePolicy enterpriseDefault() {
        return new MemoryWritePolicy(true, true, 4000);
    }
}
