package com.htam.agent.capability.tool;

public record ToolResultSummary(
        String summary,
        boolean truncated,
        int originalChars,
        int maxChars) {

    public ToolResultSummary {
        summary = summary == null ? "" : summary;
        originalChars = Math.max(0, originalChars);
        maxChars = maxChars <= 0 ? 8192 : maxChars;
    }
}
