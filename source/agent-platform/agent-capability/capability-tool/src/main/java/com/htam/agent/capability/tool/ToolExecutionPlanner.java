package com.htam.agent.capability.tool;

import com.htam.agent.capability.CapabilityItem;
import com.htam.agent.capability.CapabilityRiskLevel;
import com.htam.agent.capability.CapabilityRiskPolicy;
import java.util.Map;
import java.util.stream.Collectors;

public class ToolExecutionPlanner {

    public ToolExecutionDecision decide(CapabilityItem item, ToolExecutionOptions options) {
        ToolExecutionOptions executionOptions = options == null
                ? ToolExecutionOptions.enterpriseDefault()
                : options;
        CapabilityRiskPolicy riskPolicy = riskPolicy(item, executionOptions);
        boolean parallelAllowed = item != null
                && item.enabled()
                && item.readOnly()
                && riskPolicy == CapabilityRiskPolicy.ALLOW
                && executionOptions.parallelReadOnlyEnabled();
        return new ToolExecutionDecision(
                item == null ? null : item.name(),
                item != null && item.enabled(),
                item != null && item.readOnly(),
                parallelAllowed,
                item == null ? CapabilityRiskLevel.HIGH : item.riskLevel(),
                riskPolicy,
                executionOptions.schemaCacheTtl(),
                executionOptions.maxResultChars());
    }

    public ToolResultSummary summarizeResult(String rawResult, int maxChars) {
        String result = rawResult == null ? "" : rawResult;
        int limit = maxChars <= 0 ? ToolExecutionOptions.enterpriseDefault().maxResultChars() : maxChars;
        if (result.length() <= limit) {
            return new ToolResultSummary(result, false, result.length(), limit);
        }
        return new ToolResultSummary(result.substring(0, limit), true, result.length(), limit);
    }

    public String summarizeParameters(Map<String, ?> parameters, int maxChars) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        String summary = parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.valueOf(entry.getValue()))
                .collect(Collectors.joining(", "));
        return summarizeResult(summary, maxChars).summary();
    }

    private static CapabilityRiskPolicy riskPolicy(CapabilityItem item, ToolExecutionOptions options) {
        if (item == null || !item.enabled()) {
            return CapabilityRiskPolicy.DENY;
        }
        if (item.riskPolicy() == CapabilityRiskPolicy.DENY) {
            return CapabilityRiskPolicy.DENY;
        }
        if (item.riskLevel() == CapabilityRiskLevel.HIGH) {
            return options.highRiskFallbackPolicy();
        }
        return item.riskPolicy();
    }
}
