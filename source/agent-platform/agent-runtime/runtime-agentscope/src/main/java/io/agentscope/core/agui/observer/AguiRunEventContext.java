package io.agentscope.core.agui.observer;

import io.agentscope.core.agui.model.RunAgentInput;

public record AguiRunEventContext(
        RunAgentInput input,
        String resolvedAgentId) {

    public String runId() {
        return input == null ? null : input.getRunId();
    }

    public String threadId() {
        return input == null ? null : input.getThreadId();
    }
}
