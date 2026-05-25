package com.htam.agent.run.event;

import com.htam.agent.runtime.RuntimeEvent;
import java.util.List;

public interface RuntimeEventLedger extends RuntimeEventSink {

    List<RuntimeEvent> listByRunId(String runId);
}
