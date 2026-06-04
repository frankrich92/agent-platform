package com.htam.agent.workflow.runtime;

import com.htam.agent.workflow.definition.WorkflowDefinition;
import com.htam.agent.workflow.node.WorkflowNodeResult;
import java.util.Map;
import java.util.Optional;

/**
 * 工作流运行时门面。
 * 定义工作流启动、节点结果记录、人审任务回填和最终完成的状态流转入口。
 */
public interface WorkflowRuntime {

    WorkflowRun start(WorkflowDefinition definition, Map<String, Object> input);

    Optional<WorkflowRun> findById(String runId);

    Optional<WorkflowNodeExecution> findNode(String runId, String nodeId);

    WorkflowRun recordNodeResult(String runId, WorkflowNodeResult result);

    WorkflowRun completeHumanTask(String taskId, Map<String, Object> payload);

    WorkflowRun complete(String runId);
}
