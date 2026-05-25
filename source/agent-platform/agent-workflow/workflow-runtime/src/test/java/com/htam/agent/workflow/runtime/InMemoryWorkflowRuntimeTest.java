package com.htam.agent.workflow.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.htam.agent.workflow.definition.WorkflowDefinition;
import com.htam.agent.workflow.definition.WorkflowNodeDefinition;
import com.htam.agent.workflow.event.InMemoryWorkflowEventSink;
import com.htam.agent.workflow.event.WorkflowEventType;
import com.htam.agent.workflow.human.InMemoryHumanTaskLedger;
import com.htam.agent.workflow.node.WorkflowNodeResult;
import com.htam.agent.workflow.node.WorkflowNodeStatus;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryWorkflowRuntimeTest {

    @Test
    void startCreatesRunEventsAndHumanTasks() {
        InMemoryWorkflowEventSink events = new InMemoryWorkflowEventSink();
        InMemoryHumanTaskLedger humanTasks = new InMemoryHumanTaskLedger();
        InMemoryWorkflowRuntime runtime = new InMemoryWorkflowRuntime(events, humanTasks);
        WorkflowDefinition definition = new WorkflowDefinition(
                "wf-1",
                "approval flow",
                List.of(new WorkflowNodeDefinition("review", "human-task", List.of(), Map.of("assignee", "ops"))),
                Map.of());

        WorkflowRun run = runtime.start(definition, Map.of("subject", "deploy"));

        assertEquals(WorkflowRunStatus.RUNNING, run.status());
        assertEquals(1, humanTasks.listPendingByRunId(run.runId()).size());
        assertEquals(3, events.listByRunId(run.runId()).size());
        assertEquals(WorkflowNodeStatus.WAITING_FOR_HUMAN,
                runtime.findNode(run.runId(), "review").orElseThrow().status());

        String taskId = humanTasks.listPendingByRunId(run.runId()).getFirst().taskId();
        WorkflowRun completed = runtime.completeHumanTask(taskId, Map.of("approved", true));

        assertEquals(WorkflowRunStatus.SUCCEEDED, completed.status());
        assertEquals(0, humanTasks.listPendingByRunId(run.runId()).size());
        assertEquals(WorkflowNodeStatus.SUCCEEDED,
                runtime.findNode(run.runId(), "review").orElseThrow().status());
        assertEquals(WorkflowEventType.RUN_COMPLETED, events.listByRunId(run.runId()).getLast().eventType());
    }

    @Test
    void startAutoCompletesNonHumanNodeGraph() {
        InMemoryWorkflowEventSink events = new InMemoryWorkflowEventSink();
        InMemoryWorkflowRuntime runtime = new InMemoryWorkflowRuntime(events, new InMemoryHumanTaskLedger());
        WorkflowDefinition definition = new WorkflowDefinition(
                "wf-2",
                "auto flow",
                List.of(
                        new WorkflowNodeDefinition("prepare", "task", List.of("finish"), Map.of()),
                        new WorkflowNodeDefinition("finish", "task", List.of(), Map.of())),
                Map.of());

        WorkflowRun run = runtime.start(definition, Map.of());

        assertEquals(WorkflowRunStatus.SUCCEEDED, run.status());
        assertEquals(WorkflowNodeStatus.SUCCEEDED,
                runtime.findNode(run.runId(), "prepare").orElseThrow().status());
        assertEquals(WorkflowNodeStatus.SUCCEEDED,
                runtime.findNode(run.runId(), "finish").orElseThrow().status());
        assertEquals(WorkflowEventType.RUN_COMPLETED, events.listByRunId(run.runId()).getLast().eventType());
    }

    @Test
    void failedNodeFailsRun() {
        InMemoryWorkflowEventSink events = new InMemoryWorkflowEventSink();
        InMemoryWorkflowRuntime runtime = new InMemoryWorkflowRuntime(events, new InMemoryHumanTaskLedger());
        WorkflowDefinition definition = new WorkflowDefinition(
                "wf-3",
                "manual result flow",
                List.of(new WorkflowNodeDefinition("review", "human-task", List.of(), Map.of())),
                Map.of());

        WorkflowRun run = runtime.start(definition, Map.of());
        WorkflowRun failed = runtime.recordNodeResult(run.runId(), new WorkflowNodeResult(
                "review",
                WorkflowNodeStatus.FAILED,
                Map.of(),
                "rejected"));

        assertEquals(WorkflowRunStatus.FAILED, failed.status());
        assertEquals(WorkflowEventType.RUN_FAILED, events.listByRunId(run.runId()).getLast().eventType());
    }
}
