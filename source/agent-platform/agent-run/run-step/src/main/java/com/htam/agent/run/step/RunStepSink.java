package com.htam.agent.run.step;

import com.htam.agent.runtime.RunStep;

public interface RunStepSink {

    void append(RunStep step);
}
