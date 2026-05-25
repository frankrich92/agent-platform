package com.htam.agent.adapter.openapi;

import java.util.Map;

public record OpenApiEndpointDescriptor(
        String operationId,
        String path,
        String method,
        Map<String, Object> metadata) {

    public OpenApiEndpointDescriptor {
        if (operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("operationId 不能为空");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path 不能为空");
        }
        method = method == null || method.isBlank() ? "GET" : method.toUpperCase();
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
