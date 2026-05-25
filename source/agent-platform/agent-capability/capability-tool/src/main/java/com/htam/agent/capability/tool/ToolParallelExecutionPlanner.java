package com.htam.agent.capability.tool;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityRiskPolicy;
import java.util.ArrayList;
import java.util.List;

public class ToolParallelExecutionPlanner {

    private final ToolExecutionPlanner toolExecutionPlanner;

    public ToolParallelExecutionPlanner() {
        this(new ToolExecutionPlanner());
    }

    public ToolParallelExecutionPlanner(ToolExecutionPlanner toolExecutionPlanner) {
        this.toolExecutionPlanner = toolExecutionPlanner == null ? new ToolExecutionPlanner() : toolExecutionPlanner;
    }

    public ToolParallelExecutionPlan plan(List<CapabilityItem> items, ToolExecutionOptions options) {
        List<CapabilityItem> capabilityItems = items == null ? List.of() : List.copyOf(items);
        List<ToolExecutionDecision> parallel = new ArrayList<>();
        List<ToolExecutionDecision> serial = new ArrayList<>();
        List<ToolExecutionDecision> denied = new ArrayList<>();
        for (CapabilityItem item : capabilityItems) {
            ToolExecutionDecision decision = toolExecutionPlanner.decide(item, options);
            if (decision.riskPolicy() == CapabilityRiskPolicy.DENY || !decision.enabled()) {
                denied.add(decision);
            } else if (decision.parallelAllowed()) {
                parallel.add(decision);
            } else {
                serial.add(decision);
            }
        }
        return new ToolParallelExecutionPlan(parallel, serial, denied);
    }
}
