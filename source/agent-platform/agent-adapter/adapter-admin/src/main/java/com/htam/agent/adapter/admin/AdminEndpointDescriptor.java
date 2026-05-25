package com.htam.agent.adapter.admin;

import java.util.Map;

public record AdminEndpointDescriptor(
        String endpointId,
        String resource,
        String action,
        Map<String, Object> metadata) {

    public AdminEndpointDescriptor {
        if (endpointId == null || endpointId.isBlank()) {
            throw new IllegalArgumentException("endpointId 不能为空");
        }
        resource = resource == null ? "" : resource;
        action = action == null ? "read" : action;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
