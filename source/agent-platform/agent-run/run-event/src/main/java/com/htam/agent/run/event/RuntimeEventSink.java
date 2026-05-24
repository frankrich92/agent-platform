package com.htam.agent.run.event;

import com.htam.agent.runtime.RuntimeEvent;

public interface RuntimeEventSink {

    void append(RuntimeEvent event);
}
