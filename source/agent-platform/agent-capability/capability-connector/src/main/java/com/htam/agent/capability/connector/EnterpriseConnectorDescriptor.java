package com.htam.agent.capability.connector;

import java.util.Map;

public record EnterpriseConnectorDescriptor(
        String connectorId,
        String name,
        String protocol,
        boolean enabled,
        Map<String, Object> metadata) {

    public EnterpriseConnectorDescriptor {
        if (connectorId == null || connectorId.isBlank()) {
            throw new IllegalArgumentException("connectorId 不能为空");
        }
        name = name == null ? connectorId : name;
        protocol = protocol == null || protocol.isBlank() ? "custom" : protocol;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
