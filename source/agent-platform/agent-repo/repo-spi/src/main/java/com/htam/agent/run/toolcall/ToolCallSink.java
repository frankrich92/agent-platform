package com.htam.agent.run.toolcall;

import com.htam.agent.runtime.ToolCall;

public interface ToolCallSink {

    void append(ToolCall toolCall);
}
