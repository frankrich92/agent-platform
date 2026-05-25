package com.htam.agent.workflow.human;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryHumanTaskLedger implements HumanTaskLedger {

    private final ConcurrentMap<String, HumanTask> tasks = new ConcurrentHashMap<>();

    @Override
    public HumanTask create(HumanTask task) {
        tasks.put(task.taskId(), task);
        return task;
    }

    @Override
    public HumanTask complete(String taskId, Map<String, Object> payload) {
        HumanTask current = tasks.get(taskId);
        if (current == null) {
            throw new IllegalArgumentException("HumanTask 不存在: " + taskId);
        }
        Map<String, Object> merged = new LinkedHashMap<>(current.payload());
        if (payload != null) {
            merged.putAll(payload);
        }
        HumanTask completed = new HumanTask(
                current.taskId(),
                current.workflowId(),
                current.runId(),
                current.nodeId(),
                HumanTaskStatus.COMPLETED,
                current.createdAt(),
                merged);
        tasks.put(taskId, completed);
        return completed;
    }

    @Override
    public Optional<HumanTask> findById(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public List<HumanTask> listPendingByRunId(String runId) {
        return tasks.values().stream()
                .filter(task -> runId == null || runId.equals(task.runId()))
                .filter(task -> task.status() == HumanTaskStatus.PENDING)
                .toList();
    }
}
