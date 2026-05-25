package com.htam.agent.workflow.event;

import java.util.List;

public interface WorkflowEventSink {

    WorkflowEvent publish(WorkflowEvent event);

    List<WorkflowEvent> listByRunId(String runId);
}
