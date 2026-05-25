package com.htam.agent.workflow.runtime;

import com.htam.agent.workflow.definition.WorkflowDefinition;
import java.util.List;
import java.util.Optional;

public interface WorkflowStateRepository {

    WorkflowRun saveRun(WorkflowRun run);

    Optional<WorkflowRun> findRun(String runId);

    List<WorkflowRun> listRunsByStatus(WorkflowRunStatus status);

    WorkflowDefinition saveDefinition(String runId, WorkflowDefinition definition);

    Optional<WorkflowDefinition> findDefinition(String runId);

    WorkflowNodeExecution saveNode(String runId, WorkflowNodeExecution execution);

    Optional<WorkflowNodeExecution> findNode(String runId, String nodeId);

    List<WorkflowNodeExecution> listNodes(String runId);

    WorkflowHumanTaskBinding bindHumanTask(String taskId, String runId, String nodeId);

    Optional<WorkflowHumanTaskBinding> findHumanTaskBinding(String taskId);
}
