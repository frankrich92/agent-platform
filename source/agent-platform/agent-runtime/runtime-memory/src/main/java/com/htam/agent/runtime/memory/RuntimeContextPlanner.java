package com.htam.agent.runtime.memory;

import java.util.List;

public interface RuntimeContextPlanner {

    RuntimeContextPlan plan(
            String runId,
            TokenBudget tokenBudget,
            MemoryWritePolicy memoryWritePolicy,
            List<RuntimeContextSegment> segments);
}
