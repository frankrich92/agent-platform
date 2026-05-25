package com.htam.agent.profile.binding;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileBindingCatalog {

    private final List<ProfileCapabilityBinding> bindings;

    public ProfileBindingCatalog(List<ProfileCapabilityBinding> bindings) {
        this.bindings = bindings == null ? List.of() : List.copyOf(bindings);
    }

    public List<ProfileCapabilityBinding> enabledBindings(Long agentId, ProfileBindingType type) {
        return bindings.stream()
                .filter(ProfileCapabilityBinding::enabled)
                .filter(binding -> agentId == null || agentId.equals(binding.agentId()))
                .filter(binding -> type == null || type == binding.bindingType())
                .sorted(Comparator.comparingInt(ProfileCapabilityBinding::priority))
                .toList();
    }

    public Map<ProfileBindingType, List<ProfileCapabilityBinding>> groupEnabledByType(Long agentId) {
        return enabledBindings(agentId, null).stream()
                .collect(Collectors.groupingBy(ProfileCapabilityBinding::bindingType));
    }
}
