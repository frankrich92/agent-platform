package com.htam.agent.worker.spi;

/**
 * Worker 执行入口。
 * 上层编排只提交标准 WorkerTask，具体执行可由本地、远程、CLI 或沙箱实现承担。
 */
public interface WorkerRuntime {

    WorkerTaskResult submit(WorkerTask task);
}
