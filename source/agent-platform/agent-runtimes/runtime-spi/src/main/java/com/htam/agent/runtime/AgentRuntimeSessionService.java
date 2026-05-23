package com.htam.agent.runtime;

/**
 * Agent 运行时会话生命周期接口。
 */
public interface AgentRuntimeSessionService {

    void deleteSession(String sessionId);
}
