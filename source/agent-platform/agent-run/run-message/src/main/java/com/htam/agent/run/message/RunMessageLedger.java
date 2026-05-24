package com.htam.agent.run.message;

import java.util.List;

public interface RunMessageLedger {

    RunMessageRecord append(RunMessageRecord message);

    List<RunMessageRecord> listBySessionId(String sessionId);

    List<RunMessageRecord> listByRunId(String runId);
}
