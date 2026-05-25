package com.htam.agent.adapter.internal;

import java.util.Map;

public record InternalInvocationEnvelope(
        String invocationId,
        String targetService,
        String operation,
        Map<String, Object> payload) {

    public InternalInvocationEnvelope {
        if (invocationId == null || invocationId.isBlank()) {
            throw new IllegalArgumentException("invocationId 不能为空");
        }
        targetService = targetService == null ? "" : targetService;
        operation = operation == null ? "" : operation;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
