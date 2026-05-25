package com.htam.agent.runtime.core;

import com.htam.agent.capability.CapabilityRiskPolicy;
import com.htam.agent.governance.risk.GovernanceActionRequest;
import com.htam.agent.governance.risk.GovernanceDecision;
import com.htam.agent.governance.risk.GovernanceDecisionStatus;
import com.htam.agent.governance.risk.GovernanceGuard;
import com.htam.agent.governance.risk.LayeredRiskPolicy;
import com.htam.agent.runtime.AgentRunRequest;
import com.htam.agent.runtime.AgentRunResult;
import com.htam.agent.runtime.AgentRunStatus;
import com.htam.agent.runtime.AgentRuntimeRunner;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GovernedRuntimeRunner implements AgentRuntimeRunner {

    private final AgentRuntimeRunner delegate;
    private final GovernanceGuard governanceGuard;

    public GovernedRuntimeRunner(AgentRuntimeRunner delegate, GovernanceGuard governanceGuard) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate 不能为空");
        }
        this.delegate = delegate;
        this.governanceGuard = governanceGuard;
    }

    @Override
    public AgentRunResult run(AgentRunRequest request) {
        GovernanceDecision decision = evaluate(request);
        if (decision.status() == GovernanceDecisionStatus.DENIED) {
            return new AgentRunResult(
                    request.agentId(),
                    request.runId(),
                    AgentRunStatus.FAILED,
                    decision.reason(),
                    List.of(),
                    List.of(),
                    List.of());
        }
        if (decision.status() == GovernanceDecisionStatus.REQUIRES_APPROVAL) {
            return new AgentRunResult(
                    request.agentId(),
                    request.runId(),
                    AgentRunStatus.WAITING_APPROVAL,
                    decision.reason(),
                    List.of(),
                    List.of(),
                    List.of());
        }
        return delegate.run(request);
    }

    private GovernanceDecision evaluate(AgentRunRequest request) {
        CapabilityRiskPolicy policy = runtimePolicy(request.metadata());
        if (governanceGuard == null) {
            return switch (policy) {
                case DENY -> new GovernanceDecision(GovernanceDecisionStatus.DENIED, null, "runtime policy denied");
                case ASK -> new GovernanceDecision(GovernanceDecisionStatus.REQUIRES_APPROVAL, null, "approval required");
                case ALLOW -> new GovernanceDecision(GovernanceDecisionStatus.ALLOWED, null, "allowed");
            };
        }
        Map<String, Object> context = new LinkedHashMap<>(request.metadata());
        context.put("agentId", request.agentId());
        context.put("capabilityPlanId", request.capabilityPlanId());
        return governanceGuard.evaluate(new GovernanceActionRequest(
                request.runId(),
                null,
                "agent-runtime",
                "runtime.run",
                new LayeredRiskPolicy(policy, policy, null),
                context));
    }

    private static CapabilityRiskPolicy runtimePolicy(Map<String, Object> metadata) {
        Object value = metadata == null ? null : metadata.get("runtimePolicy");
        if (value instanceof CapabilityRiskPolicy policy) {
            return policy;
        }
        if (value instanceof String text && !text.isBlank()) {
            return CapabilityRiskPolicy.valueOf(text.trim().toUpperCase());
        }
        return CapabilityRiskPolicy.ALLOW;
    }
}
