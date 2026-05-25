package com.htam.agent.run;

import com.htam.agent.runtime.AgentRunStatus;
import java.time.Instant;
import java.util.Map;

public record AgentRunRecord(
        String runId,
        Long agentId,
        String sessionId,
        AgentRunStatus status,
        String input,
        String output,
        String traceId,
        Instant startedAt,
        Instant endedAt,
        Map<String, Object> metadata) {

    public AgentRunRecord {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId 不能为空");
        }
        status = status == null ? AgentRunStatus.ACCEPTED : status;
        startedAt = startedAt == null ? Instant.now() : startedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public AgentRunRecord withStatus(AgentRunStatus nextStatus, String nextOutput, Instant nextEndedAt) {
        return new AgentRunRecord(
                runId,
                agentId,
                sessionId,
                nextStatus,
                input,
                nextOutput,
                traceId,
                startedAt,
                nextEndedAt == null ? Instant.now() : nextEndedAt,
                metadata);
    }
}
