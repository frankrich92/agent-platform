package com.htam.agent.boot.starter;

import java.util.List;

public final class AgentPlatformStarter {

    private AgentPlatformStarter() {
    }

    public static List<String> starterModules() {
        return List.of("boot-app", "boot-autoconfigure", "boot-starter");
    }
}
