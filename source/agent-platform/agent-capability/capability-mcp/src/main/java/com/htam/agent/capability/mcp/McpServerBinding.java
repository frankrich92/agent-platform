package com.htam.agent.capability.mcp;

import java.util.List;
import java.util.Map;

public record McpServerBinding(
        String namespace,
        String serverId,
        boolean enabled,
        String secretRef,
        List<String> includeTools,
        List<String> excludeTools,
        Map<String, Object> connection) {

    public McpServerBinding {
        if (serverId == null || serverId.isBlank()) {
            throw new IllegalArgumentException("serverId 不能为空");
        }
        namespace = namespace == null || namespace.isBlank() ? "default" : namespace;
        includeTools = includeTools == null ? List.of() : List.copyOf(includeTools);
        excludeTools = excludeTools == null ? List.of() : List.copyOf(excludeTools);
        connection = connection == null ? Map.of() : Map.copyOf(connection);
    }
}
