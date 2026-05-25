package com.htam.agent.workflow.event;

public enum WorkflowEventType {
    RUN_STARTED,
    NODE_STARTED,
    NODE_COMPLETED,
    NODE_FAILED,
    HUMAN_TASK_CREATED,
    HUMAN_TASK_COMPLETED,
    RUN_COMPLETED,
    RUN_FAILED
}
