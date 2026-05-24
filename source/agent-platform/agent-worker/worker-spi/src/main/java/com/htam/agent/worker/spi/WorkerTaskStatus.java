package com.htam.agent.worker.spi;

public enum WorkerTaskStatus {
    ACCEPTED,
    APPROVAL_REQUIRED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    DENIED
}
