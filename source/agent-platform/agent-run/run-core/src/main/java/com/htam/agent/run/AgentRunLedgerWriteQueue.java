package com.htam.agent.run;

public interface AgentRunLedgerWriteQueue extends AutoCloseable {

    void submit(String description, Runnable write);

    void flush();

    @Override
    void close();

    static AgentRunLedgerWriteQueue direct() {
        return new AgentRunLedgerWriteQueue() {
            @Override
            public void submit(String description, Runnable write) {
                if (write != null) {
                    write.run();
                }
            }

            @Override
            public void flush() {
                // Direct writes are already visible to readers.
            }

            @Override
            public void close() {
                // Direct writes do not hold resources.
            }
        };
    }
}
