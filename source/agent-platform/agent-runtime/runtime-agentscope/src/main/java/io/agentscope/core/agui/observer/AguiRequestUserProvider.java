package io.agentscope.core.agui.observer;

/**
 * Resolves the authenticated user on the request thread before an AG-UI run
 * continues on the async SSE executor.
 */
@FunctionalInterface
public interface AguiRequestUserProvider {

    Long currentUserId();
}
