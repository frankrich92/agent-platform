package com.htam.agent.worker.coding;

import com.htam.agent.worker.spi.WorkerRiskPolicy;
import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskPlanner;
import com.htam.agent.worker.spi.WorkerTaskType;
import java.util.LinkedHashMap;
import java.util.Map;

public class CliCodingAgentPlanner {

    private final WorkerTaskPlanner taskPlanner = new WorkerTaskPlanner();

    public WorkerTask plan(CliCodingAgentSpec spec, String workspaceRef, String instruction) {
        if (spec == null || spec.id() == null || spec.id().isBlank()) {
            throw new IllegalArgumentException("CLI coding agent spec is required");
        }
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("cliAgentId", spec.id());
        input.put("displayName", spec.displayName());
        input.put("instruction", instruction == null ? "" : instruction);
        input.put("supportedSkills", spec.supportedSkills());
        input.put("sandboxPolicy", spec.sandboxPolicy());
        return taskPlanner.plan(WorkerTaskType.CLI_CODING_AGENT, workspaceRef, spec.command(),
                WorkerRiskPolicy.ASK, input);
    }
}
