package com.htam.agent.workflow.runtime;

import com.htam.agent.workflow.definition.WorkflowDefinition;
import com.htam.agent.workflow.definition.WorkflowNodeDefinition;
import com.htam.agent.workflow.event.WorkflowEvent;
import com.htam.agent.workflow.event.WorkflowEventSink;
import com.htam.agent.workflow.event.WorkflowEventType;
import com.htam.agent.workflow.human.HumanTask;
import com.htam.agent.workflow.human.HumanTaskLedger;
import com.htam.agent.workflow.node.WorkflowNodeResult;
import com.htam.agent.workflow.node.WorkflowNodeStatus;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class InMemoryWorkflowRuntime implements WorkflowRuntime {

    private static final String HUMAN_NODE_TYPE = "human-task";

    private final WorkflowEventSink eventSink;
    private final HumanTaskLedger humanTaskLedger;
    private final WorkflowStateRepository stateRepository;

    public InMemoryWorkflowRuntime(WorkflowEventSink eventSink, HumanTaskLedger humanTaskLedger) {
        this(eventSink, humanTaskLedger, new InMemoryWorkflowStateRepository());
    }

    public InMemoryWorkflowRuntime(
            WorkflowEventSink eventSink,
            HumanTaskLedger humanTaskLedger,
            WorkflowStateRepository stateRepository) {
        this.eventSink = eventSink;
        this.humanTaskLedger = humanTaskLedger;
        this.stateRepository = stateRepository == null ? new InMemoryWorkflowStateRepository() : stateRepository;
    }

    @Override
    public WorkflowRun start(WorkflowDefinition definition, Map<String, Object> input) {
        String runId = UUID.randomUUID().toString();
        WorkflowRun run = new WorkflowRun(
                definition.workflowId(),
                runId,
                WorkflowRunStatus.RUNNING,
                input == null ? Map.of() : Map.copyOf(input));
        stateRepository.saveRun(run);
        stateRepository.saveDefinition(runId, definition);
        saveInitialNodeExecutions(runId, definition);
        publish(definition.workflowId(), runId, null, WorkflowEventType.RUN_STARTED, run.input());
        startReadyNodes(runId, entryNodes(definition));
        return requireRun(runId);
    }

    @Override
    public Optional<WorkflowRun> findById(String runId) {
        return stateRepository.findRun(runId);
    }

    @Override
    public Optional<WorkflowNodeExecution> findNode(String runId, String nodeId) {
        return stateRepository.findNode(runId, nodeId);
    }

    @Override
    public WorkflowRun recordNodeResult(String runId, WorkflowNodeResult result) {
        WorkflowRun run = requireRun(runId);
        requireDefinition(runId);
        if (stateRepository.findNode(runId, result.nodeId()).isEmpty()) {
            throw new IllegalArgumentException("WorkflowNode 不存在: " + result.nodeId());
        }

        WorkflowNodeExecution execution = new WorkflowNodeExecution(
                result.nodeId(),
                result.status(),
                result.output(),
                result.errorMessage());
        stateRepository.saveNode(runId, execution);
        if (result.status() == WorkflowNodeStatus.FAILED) {
            publish(run.workflowId(), runId, result.nodeId(), WorkflowEventType.NODE_FAILED, result.output());
            return failRun(run, result.errorMessage());
        }

        publish(run.workflowId(), runId, result.nodeId(), WorkflowEventType.NODE_COMPLETED, result.output());
        startReadyNodes(runId, nextNodes(runId, result.nodeId()));
        completeIfFinished(runId);
        return requireRun(runId);
    }

    @Override
    public WorkflowRun completeHumanTask(String taskId, Map<String, Object> payload) {
        HumanTask task = humanTaskLedger.complete(taskId, payload);
        WorkflowHumanTaskBinding binding = stateRepository.findHumanTaskBinding(taskId)
                .orElse(new WorkflowHumanTaskBinding(taskId, task.runId(), task.nodeId()));
        String runId = binding.runId();
        String nodeId = binding.nodeId();
        publish(task.workflowId(), runId, nodeId, WorkflowEventType.HUMAN_TASK_COMPLETED, task.payload());
        return recordNodeResult(runId, new WorkflowNodeResult(
                nodeId,
                WorkflowNodeStatus.SUCCEEDED,
                task.payload(),
                null));
    }

    @Override
    public WorkflowRun complete(String runId) {
        WorkflowRun current = requireRun(runId);
        if (!allNodesTerminal(runId)) {
            throw new IllegalStateException("WorkflowRun 仍有未完成节点: " + runId);
        }
        WorkflowRun completed = new WorkflowRun(
                current.workflowId(), current.runId(), WorkflowRunStatus.SUCCEEDED, current.input());
        stateRepository.saveRun(completed);
        publish(current.workflowId(), runId, null, WorkflowEventType.RUN_COMPLETED, Map.of());
        return completed;
    }

    private void saveInitialNodeExecutions(String runId, WorkflowDefinition definition) {
        definition.nodes().forEach(node -> stateRepository.saveNode(
                runId,
                new WorkflowNodeExecution(node.nodeId(), WorkflowNodeStatus.PENDING, Map.of(), null)));
    }

    private void startReadyNodes(String runId, List<WorkflowNodeDefinition> nodes) {
        for (WorkflowNodeDefinition node : nodes) {
            startNode(runId, node);
        }
    }

    private void startNode(String runId, WorkflowNodeDefinition node) {
        WorkflowRun run = requireRun(runId);
        WorkflowNodeExecution current = stateRepository.findNode(runId, node.nodeId()).orElse(null);
        if (current == null || current.status() != WorkflowNodeStatus.PENDING) {
            return;
        }

        stateRepository.saveNode(runId, new WorkflowNodeExecution(
                node.nodeId(), WorkflowNodeStatus.RUNNING, Map.of(), null));
        publish(run.workflowId(), runId, node.nodeId(), WorkflowEventType.NODE_STARTED, node.config());

        if (HUMAN_NODE_TYPE.equals(node.nodeType())) {
            createHumanTask(requireDefinition(runId), runId, node);
            stateRepository.saveNode(runId, new WorkflowNodeExecution(
                    node.nodeId(), WorkflowNodeStatus.WAITING_FOR_HUMAN, Map.of(), null));
            return;
        }

        recordNodeResult(runId, new WorkflowNodeResult(
                node.nodeId(),
                WorkflowNodeStatus.SUCCEEDED,
                Map.of(),
                null));
    }

    private void createHumanTask(WorkflowDefinition definition, String runId, WorkflowNodeDefinition node) {
        HumanTask task = new HumanTask(
                UUID.randomUUID().toString(),
                definition.workflowId(),
                runId,
                node.nodeId(),
                null,
                null,
                node.config());
        humanTaskLedger.create(task);
        stateRepository.bindHumanTask(task.taskId(), runId, node.nodeId());
        publish(definition.workflowId(), runId, node.nodeId(), WorkflowEventType.HUMAN_TASK_CREATED, task.payload());
    }

    private List<WorkflowNodeDefinition> entryNodes(WorkflowDefinition definition) {
        Set<String> referenced = new HashSet<>();
        definition.nodes().forEach(node -> referenced.addAll(node.nextNodeIds()));
        List<WorkflowNodeDefinition> entries = definition.nodes().stream()
                .filter(node -> !referenced.contains(node.nodeId()))
                .toList();
        return entries.isEmpty() ? definition.nodes() : entries;
    }

    private List<WorkflowNodeDefinition> nextNodes(String runId, String nodeId) {
        WorkflowDefinition definition = requireDefinition(runId);
        Set<String> nextIds = definition.nodes().stream()
                .filter(node -> node.nodeId().equals(nodeId))
                .findFirst()
                .<Set<String>>map(node -> new HashSet<>(node.nextNodeIds()))
                .orElseGet(Set::of);
        if (nextIds.isEmpty()) {
            return List.of();
        }
        return definition.nodes().stream()
                .filter(node -> nextIds.contains(node.nodeId()))
                .toList();
    }

    private void completeIfFinished(String runId) {
        WorkflowRun run = requireRun(runId);
        if (run.status() != WorkflowRunStatus.RUNNING || !allNodesTerminal(runId)) {
            return;
        }
        complete(runId);
    }

    private boolean allNodesTerminal(String runId) {
        List<WorkflowNodeExecution> executions = stateRepository.listNodes(runId);
        return !executions.isEmpty() && executions.stream().allMatch(WorkflowNodeExecution::terminal);
    }

    private WorkflowRun failRun(WorkflowRun run, String errorMessage) {
        WorkflowRun failed = new WorkflowRun(run.workflowId(), run.runId(), WorkflowRunStatus.FAILED, run.input());
        stateRepository.saveRun(failed);
        Map<String, Object> payload = errorMessage == null ? Map.of() : Map.of("errorMessage", errorMessage);
        publish(run.workflowId(), run.runId(), null, WorkflowEventType.RUN_FAILED, payload);
        return failed;
    }

    private WorkflowRun requireRun(String runId) {
        return stateRepository.findRun(runId)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowRun 不存在: " + runId));
    }

    private WorkflowDefinition requireDefinition(String runId) {
        return stateRepository.findDefinition(runId)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowDefinition 不存在: " + runId));
    }

    private void publish(
            String workflowId,
            String runId,
            String nodeId,
            WorkflowEventType eventType,
            Map<String, Object> payload) {
        if (eventSink != null) {
            eventSink.publish(new WorkflowEvent(null, workflowId, runId, nodeId, eventType, null, payload));
        }
    }
}
