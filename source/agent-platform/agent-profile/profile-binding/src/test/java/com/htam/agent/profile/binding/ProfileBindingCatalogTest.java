package com.htam.agent.profile.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.htam.agent.capability.CapabilityKind;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProfileBindingCatalogTest {

    @Test
    void groupsEnabledBindingsByTypeAndPriority() {
        ProfileBindingCatalog catalog = new ProfileBindingCatalog(List.of(
                new ProfileCapabilityBinding(1L, ProfileBindingType.TOOL, CapabilityKind.TOOL,
                        "tool-2", "tool", true, 20, Map.of()),
                new ProfileCapabilityBinding(1L, ProfileBindingType.TOOL, CapabilityKind.TOOL,
                        "tool-1", "tool", true, 10, Map.of()),
                new ProfileCapabilityBinding(1L, ProfileBindingType.MCP, CapabilityKind.MCP,
                        "mcp-1", "mcp", false, 1, Map.of())));

        List<ProfileCapabilityBinding> tools = catalog.enabledBindings(1L, ProfileBindingType.TOOL);

        assertEquals(List.of("tool-1", "tool-2"), tools.stream().map(ProfileCapabilityBinding::targetId).toList());
        assertEquals(1, catalog.groupEnabledByType(1L).size());
    }
}
