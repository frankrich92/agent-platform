package com.htam.agent.workflow.runtime;

import com.htam.agent.workflow.definition.WorkflowDefinition;
import com.htam.agent.workflow.node.WorkflowNodeResult;
import java.util.Map;
import java.util.Optional;

public interface WorkflowRuntime {

    WorkflowRun start(WorkflowDefinition definition, Map<String, Object> input);

    Optional<WorkflowRun> findById(String runId);

    Optional<WorkflowNodeExecution> findNode(String runId, String nodeId);

    WorkflowRun recordNodeResult(String runId, WorkflowNodeResult result);

    WorkflowRun completeHumanTask(String taskId, Map<String, Object> payload);

    WorkflowRun complete(String runId);
}
