package com.htam.agent.capability.mcp;

import java.util.List;

public record McpPromptDescriptor(
        String namespace,
        String promptName,
        String promptRef,
        List<String> allowedArgumentNames) {

    public McpPromptDescriptor {
        if (promptName == null || promptName.isBlank()) {
            throw new IllegalArgumentException("promptName 不能为空");
        }
        namespace = namespace == null || namespace.isBlank() ? "mcp:default" : namespace;
        allowedArgumentNames = allowedArgumentNames == null ? List.of() : List.copyOf(allowedArgumentNames);
    }
}
