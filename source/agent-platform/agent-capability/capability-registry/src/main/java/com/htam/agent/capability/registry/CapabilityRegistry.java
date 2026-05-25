package com.htam.agent.capability.registry;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityKind;
import java.util.List;
import java.util.Optional;

public interface CapabilityRegistry {

    CapabilityItem register(CapabilityItem item);

    Optional<CapabilityItem> find(CapabilityKind kind, String capabilityId);

    List<CapabilityItem> list(CapabilityKind kind);
}
