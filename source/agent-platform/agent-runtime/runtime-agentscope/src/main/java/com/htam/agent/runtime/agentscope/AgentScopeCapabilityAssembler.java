package com.htam.agent.runtime.agentscope;

import com.htam.agent.runtime.RuntimeCapabilityAssembler;
import com.htam.agent.runtime.RuntimeCapabilityAssembly;
import com.htam.agent.runtime.RuntimeCapabilityAssemblyRequest;
import java.util.Map;

public class AgentScopeCapabilityAssembler implements RuntimeCapabilityAssembler {

    @Override
    public RuntimeCapabilityAssembly assemble(RuntimeCapabilityAssemblyRequest request) {
        Map<String, Object> context = request.capabilityContext();
        return new RuntimeCapabilityAssembly(
                request.agentId(),
                "agentscope",
                intValue(context.get("toolCount")),
                intValue(context.get("mcpCount")),
                intValue(context.get("skillCount")),
                intValue(context.get("knowledgeCount")),
                Boolean.TRUE.equals(context.get("workspaceEnabled")),
                Map.of(
                        "capabilityPlanId", request.capabilityPlanId() == null ? "" : request.capabilityPlanId(),
                        "assembler", "runtime-agentscope"));
    }

    private static int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return 0;
    }
}
