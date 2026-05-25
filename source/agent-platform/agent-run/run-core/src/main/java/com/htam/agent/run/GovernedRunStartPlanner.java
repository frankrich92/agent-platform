package com.htam.agent.run;

import com.htam.agent.governance.risk.GovernanceActionRequest;
import com.htam.agent.governance.risk.GovernanceDecision;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicy;
import java.util.LinkedHashMap;
import java.util.Map;

public class GovernedRunStartPlanner {

    private final GovernanceGuard governanceGuard;

    public GovernedRunStartPlanner(GovernanceGuard governanceGuard) {
        if (governanceGuard == null) {
            throw new IllegalArgumentException("governanceGuard 不能为空");
        }
        this.governanceGuard = governanceGuard;
    }

    public GovernedRunStartDecision evaluate(
            AgentRunCommand command,
            String actor,
            LayeredRiskPolicy riskPolicy) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("agentId", command.agentId());
        context.put("sessionId", command.sessionId());
        context.put("recordMessages", command.recordMessages());
        GovernanceDecision decision = governanceGuard.evaluate(new GovernanceActionRequest(
                command.runId(),
                null,
                actor,
                "run.start",
                riskPolicy,
                context));
        return new GovernedRunStartDecision(command, decision);
    }
}
