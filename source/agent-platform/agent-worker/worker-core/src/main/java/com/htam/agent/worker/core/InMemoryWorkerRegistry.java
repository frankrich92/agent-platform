package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerTaskType;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryWorkerRegistry implements WorkerRegistry {

    private final ConcurrentMap<String, WorkerNode> nodes = new ConcurrentHashMap<>();

    @Override
    public WorkerNode register(WorkerNode node) {
        nodes.put(node.workerId(), node);
        return node;
    }

    @Override
    public WorkerNode heartbeat(String workerId, Map<String, Object> metadata) {
        return nodes.compute(workerId, (id, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("WorkerNode 不存在: " + workerId);
            }
            return current.heartbeat(metadata);
        });
    }

    @Override
    public Optional<WorkerNode> findById(String workerId) {
        return Optional.ofNullable(nodes.get(workerId));
    }

    @Override
    public List<WorkerNode> listAvailable(WorkerTaskType taskType, Duration heartbeatTtl) {
        Instant deadline = Instant.now().minus(heartbeatTtl == null ? Duration.ofSeconds(30) : heartbeatTtl);
        return nodes.values().stream()
                .filter(node -> node.accepts(taskType, deadline))
                .sorted(Comparator.comparingInt(WorkerNode::activeTasks)
                        .thenComparing(WorkerNode::workerId))
                .toList();
    }

    @Override
    public WorkerNode lease(String workerId) {
        return nodes.compute(workerId, (id, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("WorkerNode 不存在: " + workerId);
            }
            return current.leaseOne();
        });
    }

    @Override
    public WorkerNode release(String workerId) {
        return nodes.compute(workerId, (id, current) -> {
            if (current == null) {
                throw new IllegalArgumentException("WorkerNode 不存在: " + workerId);
            }
            return current.releaseOne();
        });
    }
}
