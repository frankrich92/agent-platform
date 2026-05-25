package com.htam.agent.capability.registry;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryCapabilityRegistry implements CapabilityRegistry {

    private final ConcurrentMap<String, CapabilityItem> items = new ConcurrentHashMap<>();

    @Override
    public CapabilityItem register(CapabilityItem item) {
        items.put(key(item.kind(), item.capabilityId()), item);
        return item;
    }

    @Override
    public Optional<CapabilityItem> find(CapabilityKind kind, String capabilityId) {
        return Optional.ofNullable(items.get(key(kind, capabilityId)));
    }

    @Override
    public List<CapabilityItem> list(CapabilityKind kind) {
        return items.values().stream()
                .filter(item -> kind == null || item.kind() == kind)
                .toList();
    }

    private static String key(CapabilityKind kind, String capabilityId) {
        return (kind == null ? CapabilityKind.TOOL : kind).name() + ":" + capabilityId;
    }
}
