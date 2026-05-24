package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerTask;
import com.htam.agent.worker.spi.WorkerTaskResult;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryWorkerTaskLedger implements WorkerTaskLedger {

    private final ConcurrentMap<String, WorkerTask> tasks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, WorkerTaskResult> results = new ConcurrentHashMap<>();

    @Override
    public WorkerTask appendTask(WorkerTask task) {
        tasks.put(task.taskId(), task);
        return task;
    }

    @Override
    public WorkerTaskResult appendResult(WorkerTaskResult result) {
        results.put(result.taskId(), result);
        return result;
    }

    @Override
    public Optional<WorkerTask> findTask(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public Optional<WorkerTaskResult> findResult(String taskId) {
        return Optional.ofNullable(results.get(taskId));
    }

    @Override
    public List<WorkerTask> listByWorkspace(String workspaceRef) {
        return tasks.values().stream()
                .filter(task -> workspaceRef == null || workspaceRef.equals(task.workspaceRef()))
                .toList();
    }
}
