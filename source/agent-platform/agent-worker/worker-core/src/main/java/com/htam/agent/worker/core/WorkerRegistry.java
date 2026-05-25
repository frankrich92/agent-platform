package com.htam.agent.worker.core;

import com.htam.agent.worker.spi.WorkerTaskType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorkerRegistry {

    WorkerNode register(WorkerNode node);

    WorkerNode heartbeat(String workerId, Map<String, Object> metadata);

    Optional<WorkerNode> findById(String workerId);

    List<WorkerNode> listAvailable(WorkerTaskType taskType, Duration heartbeatTtl);

    WorkerNode lease(String workerId);

    WorkerNode release(String workerId);
}
