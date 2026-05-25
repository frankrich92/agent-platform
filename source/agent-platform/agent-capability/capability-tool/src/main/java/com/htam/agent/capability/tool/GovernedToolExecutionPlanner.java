package com.htam.agent.capability.tool;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.risk.GovernanceActionRequest;
import com.htam.agent.governance.risk.GovernanceDecision;
import com.htam.agent.governance.risk.GovernanceDecisionStatus;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicy;
import java.util.LinkedHashMap;
import java.util.Map;

public class GovernedToolExecutionPlanner {

    private final ToolExecutionPlanner delegate;
    private final GovernanceGuard governanceGuard;

    public GovernedToolExecutionPlanner(ToolExecutionPlanner delegate, GovernanceGuard governanceGuard) {
        this.delegate = delegate == null ? new ToolExecutionPlanner() : delegate;
        this.governanceGuard = governanceGuard;
    }

    public GovernedToolExecutionDecision decide(
            CapabilityItem item,
            ToolExecutionOptions options,
            String runId,
            String stepId,
            Map<String, Object> context) {
        ToolExecutionDecision toolDecision = delegate.decide(item, options);
        GovernanceDecision governanceDecision = evaluate(toolDecision, runId, stepId, context);
        return new GovernedToolExecutionDecision(toolDecision, governanceDecision);
    }

    private GovernanceDecision evaluate(
            ToolExecutionDecision decision,
            String runId,
            String stepId,
            Map<String, Object> context) {
        if (governanceGuard == null) {
            return switch (decision.riskPolicy()) {
                case DENY -> new GovernanceDecision(GovernanceDecisionStatus.DENIED, null, "tool policy denied");
                case ASK -> new GovernanceDecision(GovernanceDecisionStatus.REQUIRES_APPROVAL, null, "approval required");
                case ALLOW -> new GovernanceDecision(GovernanceDecisionStatus.ALLOWED, null, "allowed");
            };
        }
        Map<String, Object> requestContext = new LinkedHashMap<>(context == null ? Map.of() : context);
        requestContext.put("toolName", decision.toolName());
        requestContext.put("readOnly", decision.readOnly());
        requestContext.put("riskLevel", decision.riskLevel().name());
        return governanceGuard.evaluate(new GovernanceActionRequest(
                runId,
                stepId,
                "agent-capability",
                "capability.tool.execute",
                layeredPolicy(decision.riskPolicy()),
                requestContext));
    }

    private static LayeredRiskPolicy layeredPolicy(CapabilityRiskPolicy policy) {
        return new LayeredRiskPolicy(policy, policy, null);
    }
}
