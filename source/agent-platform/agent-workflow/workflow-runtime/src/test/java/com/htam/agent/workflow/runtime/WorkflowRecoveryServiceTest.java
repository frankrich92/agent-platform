package com.htam.agent.workflow.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.htam.agent.workflow.definition.WorkflowDefinition;
import com.htam.agent.workflow.definition.WorkflowNodeDefinition;
import com.htam.agent.workflow.event.InMemoryWorkflowEventSink;
import com.htam.agent.workflow.human.InMemoryHumanTaskLedger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WorkflowRecoveryServiceTest {

    @Test
    void recoversRunningRunsFromStateRepository() {
        InMemoryWorkflowStateRepository stateRepository = new InMemoryWorkflowStateRepository();
        InMemoryWorkflowRuntime runtime = new InMemoryWorkflowRuntime(
                new InMemoryWorkflowEventSink(),
                new InMemoryHumanTaskLedger(),
                stateRepository);
        WorkflowDefinition definition = new WorkflowDefinition(
                "wf-recover",
                "recoverable flow",
                List.of(new WorkflowNodeDefinition("review", "human-task", List.of(), Map.of())),
                Map.of());

        WorkflowRun run = runtime.start(definition, Map.of());

        assertEquals(WorkflowRunStatus.RUNNING, run.status());
        assertEquals(List.of(run), new WorkflowRecoveryService(stateRepository).recoverRunningRuns());
    }
}
