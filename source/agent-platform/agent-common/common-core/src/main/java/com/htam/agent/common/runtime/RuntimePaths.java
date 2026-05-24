package com.htam.agent.common.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Centralized runtime directory resolver.
 */
public final class RuntimePaths {
    private static final Path ROOT = resolveRoot();

    private RuntimePaths() {
    }

    public static Path root() {
        return ROOT;
    }

    public static String rootString() {
        return root().toString();
    }

    public static Path workspaces() {
        return root().resolve(AgentRuntimeProperties.WORKSPACES_DIR);
    }

    public static String workspacesString() {
        return workspaces().toString();
    }

    public static Path skills() {
        return root().resolve(AgentRuntimeProperties.SKILLS_DIR);
    }

    public static String skillsString() {
        return skills().toString();
    }

    public static Path temp() {
        return root().resolve(AgentRuntimeProperties.TEMP_DIR);
    }

    public static Path createTempDir() throws IOException {
        Files.createDirectories(temp());
        return Files.createDirectories(temp().resolve(UUID.randomUUID().toString()));
    }

    private static Path resolveRoot() {
        String propertyValue = System.getProperty(AgentRuntimeProperties.ROOT_SYSTEM_PROPERTY);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return Path.of(propertyValue);
        }
        String envValue = System.getenv(AgentRuntimeProperties.ROOT_ENV);
        if (envValue != null && !envValue.isBlank()) {
            return Path.of(envValue);
        }
        return Path.of(AgentRuntimeProperties.DEFAULT_ROOT);
    }
}
