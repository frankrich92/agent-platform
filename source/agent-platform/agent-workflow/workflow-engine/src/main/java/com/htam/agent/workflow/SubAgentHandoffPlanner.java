package com.htam.agent.workflow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SubAgentHandoffPlanner {

    public SubAgentHandoffPlan plan(SubAgentHandoffRequest request) {
        if (request == null || request.parentAgentId() == null || request.subAgentId() == null) {
            throw new IllegalArgumentException("parentAgentId and subAgentId are required");
        }
        if (!request.recursionAllowed()) {
            throw new IllegalStateException("sub-agent recursion depth exceeded");
        }
        String input = request.input();
        boolean truncated = input.length() > request.maxInputChars();
        String boundedInput = truncated ? input.substring(0, request.maxInputChars()) : input;
        Map<String, Object> handoff = new LinkedHashMap<>(request.metadata());
        handoff.put("isolatedContext", true);
        handoff.put("summaryRequired", true);
        handoff.put("inputTruncated", truncated);
        handoff.put("sourceInputChars", input.length());
        handoff.put("currentDepth", request.currentDepth());
        handoff.put("nextDepth", request.currentDepth() + 1);
        handoff.put("maxDepth", request.maxDepth());
        return new SubAgentHandoffPlan(
                request.parentAgentId(),
                request.subAgentId(),
                request.parentRunId(),
                isolatedContextRef(request),
                boundedInput,
                true,
                request.currentDepth() + 1,
                request.maxDepth(),
                handoff);
    }

    private static String isolatedContextRef(SubAgentHandoffRequest request) {
        String runPrefix = request.parentRunId() == null || request.parentRunId().isBlank()
                ? "run"
                : request.parentRunId();
        return runPrefix + ":sub-agent:" + request.subAgentId() + ":" + UUID.randomUUID();
    }
}
