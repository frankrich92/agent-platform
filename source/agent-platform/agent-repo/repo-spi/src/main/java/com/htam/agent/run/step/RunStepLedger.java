package com.htam.agent.run.step;

import com.htam.agent.runtime.RunStep;
import java.util.List;

public interface RunStepLedger extends RunStepSink {

    List<RunStep> listByRunId(String runId);
}
