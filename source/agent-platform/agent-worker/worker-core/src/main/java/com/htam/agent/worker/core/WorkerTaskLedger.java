package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import java.util.List;
import java.util.Optional;

public interface WorkerTaskLedger {

    WorkerTask appendTask(WorkerTask task);

    WorkerTaskResult appendResult(WorkerTaskResult result);

    Optional<WorkerTask> findTask(String taskId);

    Optional<WorkerTaskResult> findResult(String taskId);

    List<WorkerTask> listByWorkspace(String workspaceRef);
}
