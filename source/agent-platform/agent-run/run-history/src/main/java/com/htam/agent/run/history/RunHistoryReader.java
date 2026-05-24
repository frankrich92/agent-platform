package com.htam.agent.run.history;

public interface RunHistoryReader {

    RunHistoryRecord read(String runId);
}
