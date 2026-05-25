package com.htam.agent;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ArchitectureBoundaryTest {

    private static final Set<String> BUSINESS_MODULES = Set.of(
            "agent-profile",
            "agent-run",
            "agent-workflow",
            "agent-capability",
            "agent-worker",
            "agent-governance");

    private final Path platformRoot = findPlatformRoot();

    @Test
    void targetModuleSkeletonIsPresent() {
        List<String> expected = List.of(
                "agent-domain/domain-common",
                "agent-domain/domain-profile",
                "agent-domain/domain-run",
                "agent-domain/domain-capability",
                "agent-domain/domain-workflow",
                "agent-domain/domain-worker",
                "agent-domain/domain-governance",
                "agent-api/api-common",
                "agent-api/api-profile",
                "agent-api/api-run",
                "agent-api/api-capability",
                "agent-api/api-workflow",
                "agent-api/api-worker",
                "agent-api/api-governance",
                "agent-boot/boot-app",
                "agent-boot/boot-autoconfigure",
                "agent-boot/boot-starter");

        expected.forEach(module -> assertTrue(
                Files.isRegularFile(platformRoot.resolve(module).resolve("pom.xml")),
                "Missing target module: " + module));
    }

    @Test
    void noLegacyTechnicalLayerModuleNamesRemain() throws IOException {
        try (Stream<Path> paths = Files.walk(platformRoot, 2)) {
            List<Path> leftovers = paths
                    .filter(Files::isDirectory)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.startsWith("biz-")
                                || name.startsWith("admin-")
                                || name.startsWith("infra-")
                                || name.equals("agent-biz")
                                || name.equals("agent-admin")
                                || name.equals("agent-infra");
                    })
                    .toList();
            assertTrue(leftovers.isEmpty(), "Legacy module names remain: " + leftovers);
        }
    }

    @Test
    void domainModulesDoNotDependOnSpringMybatisOrRuntimeImplementations() throws IOException {
        try (Stream<Path> paths = Files.walk(platformRoot.resolve("agent-domain"))) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("pom.xml"))
                    .filter(path -> fileContains(path, "<groupId>org.springframework")
                            || fileContains(path, "<groupId>com.baomidou")
                            || fileContains(path, "<artifactId>mybatis-plus")
                            || fileContains(path, "<artifactId>runtime-agentscope</artifactId>"))
                    .toList();
            assertTrue(offenders.isEmpty(), "Domain modules depend on framework/runtime implementations: " + offenders);
        }
    }

    @Test
    void domainModulesDoNotImportSpringMybatisOrAgentScopeTypes() throws IOException {
        try (Stream<Path> paths = Files.walk(platformRoot.resolve("agent-domain"))) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("/target/"))
                    .filter(path -> fileContains(path, "org.springframework")
                            || fileContains(path, "com.baomidou")
                            || fileContains(path, "io.agentscope")
                            || fileContains(path, "com.htam.agent.runtime.agentscope"))
                    .toList();
            assertTrue(offenders.isEmpty(), "Domain modules import framework/runtime implementations: " + offenders);
        }
    }

    @Test
    void apiModulesDoNotDependOnFrameworkRuntimeOrRepositoryImplementations() throws IOException {
        try (Stream<Path> paths = Files.walk(platformRoot.resolve("agent-api"))) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("pom.xml"))
                    .filter(path -> fileContains(path, "<groupId>org.springframework")
                            || fileContains(path, "<groupId>com.baomidou")
                            || fileContains(path, "<artifactId>runtime-agentscope</artifactId>")
                            || fileContains(path, "<artifactId>repo-mybatis</artifactId>")
                            || fileContains(path, "<artifactId>repo-vector</artifactId>")
                            || fileContains(path, "<artifactId>repo-cache</artifactId>")
                            || fileContains(path, "<artifactId>repo-migration</artifactId>"))
                    .toList();
            assertTrue(offenders.isEmpty(), "API modules depend on framework/runtime/repository implementations: "
                    + offenders);
        }
    }

    @Test
    void apiModulesDoNotImportFrameworkRuntimeOrRepositoryTypes() throws IOException {
        try (Stream<Path> paths = Files.walk(platformRoot.resolve("agent-api"))) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("/target/"))
                    .filter(path -> fileContains(path, "org.springframework")
                            || fileContains(path, "com.baomidou")
                            || fileContains(path, "io.agentscope")
                            || fileContains(path, "com.htam.agent.runtime.agentscope")
                            || fileContains(path, "com.htam.agent.repo"))
                    .toList();
            assertTrue(offenders.isEmpty(), "API modules import framework/runtime/repository types: " + offenders);
        }
    }

    @Test
    void restControllersOnlyLiveInAdapterRestForBusinessModules() throws IOException {
        List<Path> offenders = javaFilesUnderBusinessModules()
                .filter(path -> !path.startsWith(platformRoot.resolve("agent-adapter/adapter-rest")))
                .filter(path -> fileContains(path, "@RestController") || fileContains(path, "@Controller"))
                .toList();
        assertTrue(offenders.isEmpty(), "Controllers outside adapter-rest: " + offenders);
    }

    @Test
    void controllerClassesOnlyLiveUnderAdapterModules() throws IOException {
        try (Stream<Path> paths = Files.walk(platformRoot)) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("Controller.java"))
                    .filter(path -> !path.toString().contains("/target/"))
                    .filter(path -> !path.startsWith(platformRoot.resolve("agent-adapter")))
                    .toList();
            assertTrue(offenders.isEmpty(), "Controller classes outside adapter modules: " + offenders);
        }
    }

    @Test
    void businessModulesDoNotDependOnRuntimeImplementationOrRepoImplementation() throws IOException {
        List<Path> offenders = pomFilesUnderBusinessModules()
                .filter(path -> fileContains(path, "<artifactId>runtime-agentscope</artifactId>")
                        || fileContains(path, "<artifactId>repo-mybatis</artifactId>")
                        || fileContains(path, "<artifactId>repo-vector</artifactId>")
                        || fileContains(path, "<artifactId>repo-cache</artifactId>")
                        || fileContains(path, "<artifactId>repo-migration</artifactId>"))
                .toList();
        assertTrue(offenders.isEmpty(), "Invalid business module dependencies: " + offenders);
    }

    @Test
    void businessModulesDoNotImportAgentScopeTypes() throws IOException {
        List<Path> offenders = javaFilesUnderBusinessModules()
                .filter(path -> fileContains(path, "io.agentscope")
                        || fileContains(path, "com.htam.agent.runtime.agentscope"))
                .toList();
        assertTrue(offenders.isEmpty(), "Business modules import runtime implementation: " + offenders);
    }

    @Test
    void runtimeAgentScopeDoesNotDependOnBusinessCapabilityImplementations() throws IOException {
        Path pom = platformRoot.resolve("agent-runtime/runtime-agentscope/pom.xml");
        List<String> forbiddenArtifacts = List.of(
                "profile-agent",
                "profile-admin",
                "profile-prompt",
                "adapter-websocket",
                "worker-file",
                "capability-rag",
                "capability-mcp",
                "capability-core",
                "capability-provider",
                "capability-tool",
                "capability-skill",
                "capability-knowledge",
                "governance-sensitive");

        String text = Files.readString(pom);
        List<String> offenders = forbiddenArtifacts.stream()
                .filter(artifact -> text.contains("<artifactId>" + artifact + "</artifactId>"))
                .toList();
        assertTrue(offenders.isEmpty(), "runtime-agentscope has forbidden dependencies: " + offenders);
    }

    @Test
    void runtimeAgentScopeHasNoControllerClasses() throws IOException {
        Path root = platformRoot.resolve("agent-runtime/runtime-agentscope/src/main/java");
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("Controller.java"))
                    .toList();
            assertTrue(offenders.isEmpty(), "runtime-agentscope should expose endpoints through adapters: " + offenders);
        }
    }

    @Test
    void agentCommonTopLevelModuleIsRemoved() throws IOException {
        assertTrue(Files.notExists(platformRoot.resolve("agent-common")), "agent-common top-level module should not remain");
        String rootPom = Files.readString(platformRoot.resolve("pom.xml"));
        assertTrue(!rootPom.contains("<module>agent-common</module>"), "Root pom still declares agent-common");
        try (Stream<Path> paths = Files.walk(platformRoot)) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("pom.xml"))
                    .filter(path -> fileContains(path, "<artifactId>common-core</artifactId>"))
                    .toList();
            assertTrue(offenders.isEmpty(), "common-core dependencies remain: " + offenders);
        }
    }

    @Test
    void apboaCommonResponsibilitiesLiveInTargetModules() {
        List<String> expected = List.of(
                "agent-domain/domain-common/src/main/java/com/htam/agent/common/util/JsonUtils.java",
                "agent-domain/domain-capability/src/main/java/com/htam/agent/common/wrapper/ModelWrapper.java",
                "agent-api/api-common/src/main/java/com/htam/agent/common/r/R.java",
                "agent-api/api-common/src/main/java/com/htam/agent/common/exception/BusinessException.java",
                "agent-governance/governance-auth/src/main/java/com/htam/agent/common/config/auth/AuthInterceptor.java",
                "agent-governance/governance-auth/src/main/java/com/htam/agent/common/util/TokenUtils.java",
                "agent-repo/repo-mybatis/src/main/java/com/htam/agent/common/config/mybatis/MybatisPlusConfig.java",
                "agent-repo/repo-vector/src/main/java/com/htam/agent/common/config/db/QdrantConfig.java",
                "agent-repo/repo-cache/src/main/java/com/htam/agent/common/util/RedisUtils.java",
                "agent-boot/boot-autoconfigure/src/main/java/com/htam/agent/common/config/JacksonConfig.java");
        expected.forEach(relative -> assertTrue(
                Files.isRegularFile(platformRoot.resolve(relative)),
                "Missing split common responsibility: " + relative));
    }

    @Test
    void domainAndApiCommonNoLongerOwnRepresentativeBusinessContracts() {
        List<String> moved = List.of(
                "agent-domain/domain-common/src/main/java/com/htam/agent/common/entity/AgentDefinition.java",
                "agent-domain/domain-common/src/main/java/com/htam/agent/common/entity/ChatSession.java",
                "agent-domain/domain-common/src/main/java/com/htam/agent/common/entity/McpServer.java",
                "agent-domain/domain-common/src/main/java/com/htam/agent/common/entity/CodeExecutionConfig.java",
                "agent-domain/domain-common/src/main/java/com/htam/agent/common/entity/SensitiveWordConfig.java",
                "agent-api/api-common/src/main/java/com/htam/agent/common/dto/AgentDefinitionDTO.java",
                "agent-api/api-common/src/main/java/com/htam/agent/common/dto/ChatSessionCreateDTO.java",
                "agent-api/api-common/src/main/java/com/htam/agent/common/dto/McpServerDTO.java",
                "agent-api/api-common/src/main/java/com/htam/agent/common/dto/SensitiveWordConfigDTO.java");
        moved.forEach(relative -> assertTrue(Files.notExists(platformRoot.resolve(relative)),
                "Business contract still lives in common module: " + relative));

        List<String> expected = List.of(
                "agent-domain/domain-profile/src/main/java/com/htam/agent/common/entity/AgentDefinition.java",
                "agent-domain/domain-run/src/main/java/com/htam/agent/common/entity/ChatSession.java",
                "agent-domain/domain-capability/src/main/java/com/htam/agent/common/entity/McpServer.java",
                "agent-domain/domain-worker/src/main/java/com/htam/agent/common/entity/CodeExecutionConfig.java",
                "agent-domain/domain-governance/src/main/java/com/htam/agent/common/entity/SensitiveWordConfig.java",
                "agent-domain/domain-workflow/src/main/java/com/htam/agent/workflow/domain/WorkflowStateRef.java",
                "agent-api/api-profile/src/main/java/com/htam/agent/common/dto/AgentDefinitionDTO.java",
                "agent-api/api-run/src/main/java/com/htam/agent/common/dto/ChatSessionCreateDTO.java",
                "agent-api/api-capability/src/main/java/com/htam/agent/common/dto/McpServerDTO.java",
                "agent-api/api-governance/src/main/java/com/htam/agent/common/dto/SensitiveWordConfigDTO.java",
                "agent-api/api-workflow/src/main/java/com/htam/agent/workflow/api/WorkflowRunDTO.java");
        expected.forEach(relative -> assertTrue(Files.isRegularFile(platformRoot.resolve(relative)),
                "Moved business contract missing from subdomain: " + relative));
    }

    @Test
    void profileAgentDoesNotOwnWorkspaceOrCodeExecutionServices() throws IOException {
        Path root = platformRoot.resolve("agent-profile/profile-agent/src/main/java");
        List<String> forbiddenNames = List.of(
                "WorkspaceService.java",
                "WorkspaceServiceImpl.java",
                "AgentCodeExecutionService.java",
                "AgentCodeExecutionServiceImpl.java",
                "CodeExecutionConfigService.java",
                "CodeExecutionConfigServiceImpl.java");
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> offenders = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> forbiddenNames.contains(path.getFileName().toString()))
                    .toList();
            assertTrue(offenders.isEmpty(), "Profile still owns worker or code execution services: " + offenders);
        }
    }

    @Test
    void newCapabilityShellsHaveConcreteSources() throws IOException {
        List<String> modules = List.of(
                "agent-profile/profile-core",
                "agent-profile/profile-model-policy",
                "agent-profile/profile-binding",
                "agent-workflow/workflow-definition",
                "agent-workflow/workflow-node",
                "agent-workflow/workflow-human-task",
                "agent-workflow/workflow-event",
                "agent-workflow/workflow-runtime",
                "agent-capability/capability-connector",
                "agent-capability/capability-registry",
                "agent-repo/repo-vector",
                "agent-repo/repo-cache",
                "agent-repo/repo-migration",
                "agent-worker/worker-workspace",
                "agent-adapter/adapter-openapi",
                "agent-adapter/adapter-admin",
                "agent-adapter/adapter-internal",
                "agent-boot/boot-starter");
        for (String module : modules) {
            Path sourceRoot = platformRoot.resolve(module).resolve("src/main/java");
            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                boolean hasJavaSource = paths.anyMatch(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"));
                assertTrue(hasJavaSource, "Module has no concrete Java source: " + module);
            }
        }
    }

    @Test
    void remainingCapabilityDomainsHaveExecutableContracts() throws IOException {
        List<String> expected = List.of(
                "agent-worker/worker-core/src/main/java/com/htam/agent/worker/core/GovernedWorkerRuntime.java",
                "agent-worker/worker-core/src/main/java/com/htam/agent/worker/core/WorkerExecutionCoordinator.java",
                "agent-worker/worker-core/src/main/java/com/htam/agent/worker/core/RemoteWorkerDispatcher.java",
                "agent-worker/worker-core/src/main/java/com/htam/agent/worker/core/WorkerRegistry.java",
                "agent-worker/worker-coding-cli/src/main/java/com/htam/agent/worker/coding/CliCodingAgentLifecycle.java",
                "agent-profile/profile-core/src/main/java/com/htam/agent/profile/core/GovernedProfileChangePlanner.java",
                "agent-run/run-core/src/main/java/com/htam/agent/run/GovernedRunStartPlanner.java",
                "agent-capability/capability-tool/src/main/java/com/htam/agent/capability/tool/GovernedToolExecutionPlanner.java",
                "agent-governance/governance-approval/src/main/java/com/htam/agent/governance/approval/JdbcApprovalLedger.java",
                "agent-governance/governance-audit/src/main/java/com/htam/agent/governance/audit/JdbcAuditLedger.java",
                "agent-runtime/runtime-core/src/main/java/com/htam/agent/runtime/core/GovernedRuntimeRunner.java",
                "agent-runtime/runtime-spi/src/main/java/com/htam/agent/runtime/RuntimeCapabilityAssembler.java",
                "agent-runtime/runtime-agentscope/src/main/java/com/htam/agent/runtime/agentscope/AgentScopeCapabilityAssembler.java",
                "agent-profile/profile-binding/src/main/java/com/htam/agent/profile/binding/ProfileBindingCatalog.java",
                "agent-workflow/workflow-runtime/src/main/java/com/htam/agent/workflow/runtime/WorkflowNodeExecution.java",
                "agent-workflow/workflow-runtime/src/main/java/com/htam/agent/workflow/runtime/InMemoryWorkflowRuntime.java",
                "agent-workflow/workflow-runtime/src/main/java/com/htam/agent/workflow/runtime/WorkflowStateRepository.java",
                "agent-workflow/workflow-runtime/src/main/java/com/htam/agent/workflow/runtime/WorkflowRecoveryService.java",
                "agent-repo/repo-spi/src/main/java/com/htam/agent/repo/vector/VectorRepository.java",
                "agent-repo/repo-vector/src/main/java/com/htam/agent/repo/vector/PgVectorRepositoryAdapter.java",
                "agent-repo/repo-spi/src/main/java/com/htam/agent/repo/cache/CacheRepository.java",
                "agent-repo/repo-cache/src/main/java/com/htam/agent/repo/cache/RedisCacheRepository.java",
                "agent-repo/repo-migration/src/main/java/com/htam/agent/repo/migration/MigrationExecutor.java",
                "agent-repo/repo-migration/src/main/java/com/htam/agent/repo/migration/JdbcMigrationHistory.java",
                "agent-repo/repo-migration/src/main/java/com/htam/agent/repo/migration/SqlResourceMigrationStepHandler.java");
        expected.forEach(relative -> assertTrue(
                Files.isRegularFile(platformRoot.resolve(relative)),
                "Missing executable capability contract: " + relative));

        Path workerCorePom = platformRoot.resolve("agent-worker/worker-core/pom.xml");
        assertTrue(
                fileContains(workerCorePom, "<artifactId>governance-risk</artifactId>"),
                "worker-core should route worker policy through governance-risk");

        Path profileCorePom = platformRoot.resolve("agent-profile/profile-core/pom.xml");
        assertTrue(
                fileContains(profileCorePom, "<artifactId>governance-risk</artifactId>"),
                "profile-core should route profile changes through governance-risk");

        Path runCorePom = platformRoot.resolve("agent-run/run-core/pom.xml");
        assertTrue(
                fileContains(runCorePom, "<artifactId>governance-risk</artifactId>"),
                "run-core should route run start through governance-risk");

        Path capabilityToolPom = platformRoot.resolve("agent-capability/capability-tool/pom.xml");
        assertTrue(
                fileContains(capabilityToolPom, "<artifactId>governance-risk</artifactId>"),
                "capability-tool should expose governed execution planning");

        Path runtimeCorePom = platformRoot.resolve("agent-runtime/runtime-core/pom.xml");
        assertTrue(
                fileContains(runtimeCorePom, "<artifactId>governance-risk</artifactId>"),
                "runtime-core should expose governed runtime execution");

        Path approvalPom = platformRoot.resolve("agent-governance/governance-approval/pom.xml");
        assertTrue(
                fileContains(approvalPom, "<artifactId>jackson-databind</artifactId>"),
                "governance-approval should serialize persistent approval context");

        Path auditPom = platformRoot.resolve("agent-governance/governance-audit/pom.xml");
        assertTrue(
                fileContains(auditPom, "<artifactId>jackson-databind</artifactId>"),
                "governance-audit should serialize persistent audit attributes");

        Path workflowRuntime = platformRoot.resolve(
                "agent-workflow/workflow-runtime/src/main/java/com/htam/agent/workflow/runtime/InMemoryWorkflowRuntime.java");
        assertTrue(
                fileContains(workflowRuntime, "recordNodeResult")
                        && fileContains(workflowRuntime, "completeHumanTask"),
                "workflow-runtime should expose node progression and human-task completion");
    }

    private Stream<Path> pomFilesUnderBusinessModules() throws IOException {
        return Files.walk(platformRoot)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals("pom.xml"))
                .filter(this::isUnderBusinessModule);
    }

    private Stream<Path> javaFilesUnderBusinessModules() throws IOException {
        return Files.walk(platformRoot)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(this::isUnderBusinessModule)
                .filter(path -> !path.toString().contains("/target/"));
    }

    private boolean isUnderBusinessModule(Path path) {
        Path relative = platformRoot.relativize(path);
        return relative.getNameCount() > 0 && BUSINESS_MODULES.contains(relative.getName(0).toString());
    }

    private static boolean fileContains(Path path, String needle) {
        try {
            return Files.readString(path).contains(needle);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    private static Path findPlatformRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("agent-domain"))
                    && Files.isDirectory(current.resolve("agent-runtime"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate agent-platform root");
    }
}
