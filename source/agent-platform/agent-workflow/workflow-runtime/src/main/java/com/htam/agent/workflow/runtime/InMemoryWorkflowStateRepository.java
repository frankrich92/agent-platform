package com.htam.agent.workflow.runtime;

import com.htam.agent.workflow.definition.WorkflowDefinition;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryWorkflowStateRepository implements WorkflowStateRepository {

    private final ConcurrentMap<String, WorkflowRun> runs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, WorkflowNodeExecution>> nodes =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<String, WorkflowHumanTaskBinding> humanTasks = new ConcurrentHashMap<>();

    @Override
    public WorkflowRun saveRun(WorkflowRun run) {
        runs.put(run.runId(), run);
        return run;
    }

    @Override
    public Optional<WorkflowRun> findRun(String runId) {
        return Optional.ofNullable(runs.get(runId));
    }

    @Override
    public List<WorkflowRun> listRunsByStatus(WorkflowRunStatus status) {
        return runs.values().stream()
                .filter(run -> status == null || run.status() == status)
                .toList();
    }

    @Override
    public WorkflowDefinition saveDefinition(String runId, WorkflowDefinition definition) {
        definitions.put(runId, definition);
        return definition;
    }

    @Override
    public Optional<WorkflowDefinition> findDefinition(String runId) {
        return Optional.ofNullable(definitions.get(runId));
    }

    @Override
    public WorkflowNodeExecution saveNode(String runId, WorkflowNodeExecution execution) {
        nodes.computeIfAbsent(runId, ignored -> new ConcurrentHashMap<>())
                .put(execution.nodeId(), execution);
        return execution;
    }

    @Override
    public Optional<WorkflowNodeExecution> findNode(String runId, String nodeId) {
        ConcurrentMap<String, WorkflowNodeExecution> runNodes = nodes.get(runId);
        return runNodes == null ? Optional.empty() : Optional.ofNullable(runNodes.get(nodeId));
    }

    @Override
    public List<WorkflowNodeExecution> listNodes(String runId) {
        ConcurrentMap<String, WorkflowNodeExecution> runNodes = nodes.get(runId);
        return runNodes == null ? List.of() : List.copyOf(runNodes.values());
    }

    @Override
    public WorkflowHumanTaskBinding bindHumanTask(String taskId, String runId, String nodeId) {
        WorkflowHumanTaskBinding binding = new WorkflowHumanTaskBinding(taskId, runId, nodeId);
        humanTasks.put(taskId, binding);
        return binding;
    }

    @Override
    public Optional<WorkflowHumanTaskBinding> findHumanTaskBinding(String taskId) {
        return Optional.ofNullable(humanTasks.get(taskId));
    }
}
