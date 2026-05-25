package com.htam.agent.run;

import com.htam.agent.runtime.AgentRunStatus;

public record AgentRunSearchQuery(
        String sessionId,
        AgentRunStatus status,
        String keyword,
        int limit) {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    public AgentRunSearchQuery {
        sessionId = blankToNull(sessionId);
        keyword = blankToNull(keyword);
        limit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
    }

    public boolean empty() {
        return sessionId == null && status == null && keyword == null;
    }

    public static AgentRunSearchQuery bySessionId(String sessionId) {
        return new AgentRunSearchQuery(sessionId, null, null, DEFAULT_LIMIT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
