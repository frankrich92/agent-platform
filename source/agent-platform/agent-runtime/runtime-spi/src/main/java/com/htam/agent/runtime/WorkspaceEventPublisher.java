package com.htam.agent.runtime;

public interface WorkspaceEventPublisher {

    void publishFileChanged(String sessionId, String fileName);
}
