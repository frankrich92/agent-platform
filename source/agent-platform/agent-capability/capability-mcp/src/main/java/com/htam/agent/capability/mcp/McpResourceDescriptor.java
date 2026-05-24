package com.htam.agent.capability.mcp;

import java.util.Map;

public record McpResourceDescriptor(
        String namespace,
        String resourceName,
        String uriPattern,
        boolean readOnly,
        Map<String, Object> metadata) {

    public McpResourceDescriptor {
        if (resourceName == null || resourceName.isBlank()) {
            throw new IllegalArgumentException("resourceName 不能为空");
        }
        namespace = namespace == null || namespace.isBlank() ? "mcp:default" : namespace;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
