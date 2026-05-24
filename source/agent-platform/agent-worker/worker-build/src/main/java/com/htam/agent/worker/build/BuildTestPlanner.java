package com.htam.agent.worker.build;

import java.nio.file.Path;
import java.util.List;

public class BuildTestPlanner {

    public BuildTestPlan maven(Path workspace) {
        return new BuildTestPlan(workspace, List.of("mvn test"), true);
    }

    public BuildTestPlan pnpm(Path workspace) {
        return new BuildTestPlan(workspace, List.of("pnpm type-check", "pnpm build"), true);
    }
}
