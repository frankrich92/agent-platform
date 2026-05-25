package com.htam.agent.common.runtime;

/**
 * Runtime directory property names and defaults.
 */
public final class AgentRuntimeProperties {
    public static final String ROOT_SYSTEM_PROPERTY = "agent.runtime.root";
    public static final String ROOT_ENV = "AGENT_RUNTIME_ROOT";
    public static final String DEFAULT_ROOT = ".agent-platform";
    public static final String WORKSPACES_DIR = "workspaces";
    public static final String SKILLS_DIR = "skills";
    public static final String TEMP_DIR = "temp";

    private AgentRuntimeProperties() {
    }
}
