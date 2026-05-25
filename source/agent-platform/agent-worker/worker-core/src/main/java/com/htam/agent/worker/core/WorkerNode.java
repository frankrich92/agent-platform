package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerTaskType;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record WorkerNode(
        String workerId,
        String endpoint,
        List<WorkerTaskType> capabilities,
        int maxConcurrentTasks,
        int activeTasks,
        Instant lastHeartbeatAt,
        Map<String, Object> metadata) {

    public WorkerNode {
        if (workerId == null || workerId.isBlank()) {
            throw new IllegalArgumentException("workerId 不能为空");
        }
        endpoint = endpoint == null ? "" : endpoint;
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        maxConcurrentTasks = maxConcurrentTasks <= 0 ? 1 : maxConcurrentTasks;
        activeTasks = Math.max(0, activeTasks);
        lastHeartbeatAt = lastHeartbeatAt == null ? Instant.now() : lastHeartbeatAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean accepts(WorkerTaskType taskType, Instant heartbeatDeadline) {
        return capabilities.contains(taskType)
                && activeTasks < maxConcurrentTasks
                && !lastHeartbeatAt.isBefore(heartbeatDeadline);
    }

    public WorkerNode heartbeat(Map<String, Object> newMetadata) {
        return new WorkerNode(workerId, endpoint, capabilities, maxConcurrentTasks, activeTasks,
                Instant.now(), merge(newMetadata));
    }

    public WorkerNode leaseOne() {
        return new WorkerNode(workerId, endpoint, capabilities, maxConcurrentTasks, activeTasks + 1,
                lastHeartbeatAt, metadata);
    }

    public WorkerNode releaseOne() {
        return new WorkerNode(workerId, endpoint, capabilities, maxConcurrentTasks, Math.max(0, activeTasks - 1),
                Instant.now(), metadata);
    }

    private Map<String, Object> merge(Map<String, Object> newMetadata) {
        if (newMetadata == null || newMetadata.isEmpty()) {
            return metadata;
        }
        java.util.LinkedHashMap<String, Object> merged = new java.util.LinkedHashMap<>(metadata);
        merged.putAll(newMetadata);
        return Map.copyOf(merged);
    }
}
