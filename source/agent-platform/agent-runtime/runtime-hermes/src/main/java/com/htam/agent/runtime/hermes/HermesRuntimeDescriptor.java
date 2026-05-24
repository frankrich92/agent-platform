package com.htam.agent.runtime.hermes;

import java.util.Map;

public record HermesRuntimeDescriptor(
        String runtimeId,
        String endpoint,
        boolean enabled,
        Map<String, Object> metadata) {

    public HermesRuntimeDescriptor {
        runtimeId = runtimeId == null || runtimeId.isBlank() ? "hermes" : runtimeId;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
