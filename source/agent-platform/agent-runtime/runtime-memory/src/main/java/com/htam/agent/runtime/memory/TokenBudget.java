package com.htam.agent.runtime.memory;

public record TokenBudget(
        int maxInputTokens,
        int reservedOutputTokens,
        int memoryTokens,
        int toolResultTokens) {

    public TokenBudget {
        if (maxInputTokens <= 0) {
            maxInputTokens = 8192;
        }
        if (reservedOutputTokens < 0) {
            reservedOutputTokens = 0;
        }
        if (memoryTokens < 0) {
            memoryTokens = 0;
        }
        if (toolResultTokens < 0) {
            toolResultTokens = 0;
        }
    }

    public int contextTokens() {
        return Math.max(0, maxInputTokens - reservedOutputTokens - memoryTokens - toolResultTokens);
    }
}
