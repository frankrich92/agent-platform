package com.htam.agent.worker.spi;

public interface WorkerRuntime {

    WorkerTaskResult submit(WorkerTask task);
}
