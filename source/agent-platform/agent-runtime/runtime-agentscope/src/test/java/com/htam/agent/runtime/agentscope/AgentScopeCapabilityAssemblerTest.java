package com.htam.agent.runtime.agentscope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.runtime.RuntimeCapabilityAssembly;
import com.htam.agent.runtime.RuntimeCapabilityAssemblyRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentScopeCapabilityAssemblerTest {

    @Test
    void assemblesAgentScopeRuntimeCapabilitiesThroughSpi() {
        RuntimeCapabilityAssembly assembly = new AgentScopeCapabilityAssembler().assemble(
                new RuntimeCapabilityAssemblyRequest(
                        1L,
                        "plan-1",
                        "agentscope",
                        Map.of("toolCount", 2, "mcpCount", 1, "workspaceEnabled", true)));

        assertEquals("agentscope", assembly.runtimeType());
        assertEquals(2, assembly.toolCount());
        assertEquals(1, assembly.mcpCount());
        assertTrue(assembly.workspaceEnabled());
    }
}
