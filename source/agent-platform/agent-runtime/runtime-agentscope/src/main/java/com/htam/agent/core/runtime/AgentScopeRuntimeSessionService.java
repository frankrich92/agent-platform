package com.htam.agent.core.runtime;

import com.htam.agent.repo.agent.AgentScopeSessionRepository;
import com.htam.agent.runtime.AgentRuntimeSessionService;
import io.agentscope.spring.boot.agui.common.ThreadSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * AgentScope 会话清理适配器。
 */
@Component
@RequiredArgsConstructor
public class AgentScopeRuntimeSessionService implements AgentRuntimeSessionService {

    private final AgentScopeSessionRepository agentScopeSessionRepository;
    private final ObjectProvider<ThreadSessionManager> sessionManager;

    @Override
    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        agentScopeSessionRepository.deleteById(sessionId);
        ThreadSessionManager manager = sessionManager.getIfAvailable();
        if (manager != null) {
            manager.removeSession(sessionId);
        }
    }
}
